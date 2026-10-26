package com.telegramquiz.auth.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;
import com.telegramquiz.auth.model.AppSetting;
import com.telegramquiz.auth.model.User;
import com.telegramquiz.auth.repository.AppSettingRepository;
import com.telegramquiz.auth.repository.InvitationCodeRepository;
import com.telegramquiz.auth.repository.UserRepository;
import com.telegramquiz.auth.support.TestContainers;

/**
 * Ordered: the "not configured" observation must happen before any Google
 * settings are stored, and the shared invitation code is created once and
 * reused by the registration scenarios that follow.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GoogleRegistrationIT {

    private static final String REDIS_PREFIX = "oauth2:pending:";

    private static String invitationCode;
    private static Long invitationCodeId;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainers.register(registry);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppSettingRepository appSettingRepository;

    @Autowired
    private InvitationCodeRepository invitationCodeRepository;

    @Autowired
    private StringRedisTemplate redis;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @Order(1)
    @DisplayName("auth / Google sign-in configuration / Google not configured")
    void googleIsNotConfiguredWhenSettingsAreMissing() throws Exception {
        appSettingRepository.deleteById("google_client_id");
        appSettingRepository.deleteById("google_client_secret");

        mockMvc.perform(get("/auth/oauth2/configured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configured").value(false));
    }

    @Test
    @Order(2)
    @DisplayName("auth / Google sign-in configuration / Reading Google settings as admin")
    void adminReadsSettingsWithoutSeeingTheSecret() throws Exception {
        appSettingRepository.save(new AppSetting("google_client_id", "cid-123"));
        appSettingRepository.save(new AppSetting("google_client_secret", "sup3r-secret-value"));

        String adminAccess = adminToken();
        String body = mockMvc.perform(get("/auth/admin/settings/google")
                        .header("Authorization", "Bearer " + adminAccess))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value("cid-123"))
                .andExpect(jsonPath("$.secretConfigured").value(true))
                .andReturn().getResponse().getContentAsString();

        assertThat(body).doesNotContain("sup3r-secret-value");
    }

    @Test
    @Order(3)
    @DisplayName("auth / Google sign-in configuration / Saving settings without a new secret")
    void blankSecretPreservesTheStoredOne() throws Exception {
        String adminAccess = adminToken();

        mockMvc.perform(put("/auth/admin/settings/google")
                        .header("Authorization", "Bearer " + adminAccess)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clientId\":\"cid-456\",\"clientSecret\":\"\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/auth/admin/settings/google")
                        .header("Authorization", "Bearer " + adminAccess))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value("cid-456"))
                .andExpect(jsonPath("$.secretConfigured").value(true));

        assertThat(appSettingRepository.findByKey("google_client_secret").orElseThrow().getValue())
                .isEqualTo("sup3r-secret-value");

        // Both values stored again — Google sign-in is offered
        mockMvc.perform(get("/auth/oauth2/configured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configured").value(true));
    }

    @Test
    @Order(4)
    @DisplayName("auth / Invitation code lifecycle / Generated code is 12-char uppercase and starts active")
    void generatedCodeHasTheDocumentedShape() throws Exception {
        String body = mockMvc.perform(post("/auth/admin/invitation-codes")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.createdBy").value("localadmin"))
                .andReturn().getResponse().getContentAsString();

        invitationCode = JsonPath.read(body, "$.code");
        invitationCodeId = ((Number) JsonPath.read(body, "$.id")).longValue();
        assertThat(invitationCode).matches("[0-9A-F]{12}");
    }

    @Test
    @Order(5)
    @DisplayName("auth / Invitation-gated Google registration / Completing registration")
    void pendingProfileWithActiveCodeCreatesTheAccount() throws Exception {
        String sub = "g-" + UUID.randomUUID();
        String firstName = "quiz" + UUID.randomUUID().toString().substring(0, 8);
        String email = firstName + "@example.com";
        String state = seedPending(sub, email, firstName, "User");

        String body = mockMvc.perform(post("/auth/oauth2/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeBody(state, invitationCode)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        String accessToken = JsonPath.read(body, "$.accessToken");

        User created = userRepository.findByGoogleSub(sub).orElseThrow();
        assertThat(created.getUsername()).isEqualTo(firstName);
        assertThat(created.getRoles()).containsExactly("ROLE_MEMBER");

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(firstName))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.firstName").value(firstName))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.provider").value("google"));

        // The pending state is consumed
        assertThat(redis.hasKey(REDIS_PREFIX + state)).isFalse();
    }

    @Test
    @Order(6)
    @DisplayName("auth / Invitation-gated Google registration / Expired registration state")
    void missingPendingStateIsRejected() throws Exception {
        mockMvc.perform(post("/auth/oauth2/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeBody("state-that-was-never-stored", invitationCode)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Session expired or invalid. Please sign in with Google again."));
    }

    @Test
    @Order(7)
    @DisplayName("auth / Invitation-gated Google registration / Invalid invitation code")
    void unknownCodeCreatesNoAccount() throws Exception {
        String sub = "g-" + UUID.randomUUID();
        String state = seedPending(sub, "unknown-code@example.com", "unknowncode", "User");

        mockMvc.perform(post("/auth/oauth2/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeBody(state, "000000000000")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Invalid or inactive invitation code."));

        assertThat(userRepository.findByGoogleSub(sub)).isEmpty();
    }

    @Test
    @Order(8)
    @DisplayName("auth / Invitation-gated Google registration / Already registered Google account")
    void registeredSubjectIsNotDuplicated() throws Exception {
        String sub = "g-" + UUID.randomUUID();
        String username = "guser-" + UUID.randomUUID().toString().substring(0, 8);
        User existing = userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .googleSub(sub)
                .roles(Set.of("ROLE_MEMBER"))
                .active(true)
                .build());

        String state = seedPending(sub, "already@example.com", "already", "User");

        mockMvc.perform(post("/auth/oauth2/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeBody(state, invitationCode)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This Google account is already registered."));

        User found = userRepository.findByGoogleSub(sub).orElseThrow();
        assertThat(found.getId()).isEqualTo(existing.getId());
    }

    @Test
    @Order(9)
    @DisplayName("auth / Invitation code lifecycle / Reusing a code")
    void sameActiveCodeServesTwoRegistrations() throws Exception {
        String subA = "g-" + UUID.randomUUID();
        String subB = "g-" + UUID.randomUUID();
        String stateA = seedPending(subA, "reuse-a@example.com",
                "reusea" + UUID.randomUUID().toString().substring(0, 4), "User");
        String stateB = seedPending(subB, "reuse-b@example.com",
                "reuseb" + UUID.randomUUID().toString().substring(0, 4), "User");

        mockMvc.perform(post("/auth/oauth2/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeBody(stateA, invitationCode)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());

        mockMvc.perform(post("/auth/oauth2/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeBody(stateB, invitationCode)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());

        // Registration does not consume the code
        assertThat(invitationCodeRepository.findByCodeAndActiveTrue(invitationCode)).isPresent();
    }

    @Test
    @Order(10)
    @DisplayName("auth / Invitation code lifecycle / Deactivated code")
    void deactivatedCodeIsRejected() throws Exception {
        String body = mockMvc.perform(post("/auth/admin/invitation-codes")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String code = JsonPath.read(body, "$.code");
        long id = ((Number) JsonPath.read(body, "$.id")).longValue();

        mockMvc.perform(delete("/auth/admin/invitation-codes/" + id)
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isNoContent());
        assertThat(invitationCodeRepository.findByCodeAndActiveTrue(code)).isEmpty();

        String sub = "g-" + UUID.randomUUID();
        String state = seedPending(sub, "deactivated@example.com", "deactivated", "User");

        mockMvc.perform(post("/auth/oauth2/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeBody(state, code)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Invalid or inactive invitation code."));

        assertThat(userRepository.findByGoogleSub(sub)).isEmpty();
    }

    private String seedPending(String sub, String email, String firstName, String lastName) {
        String state = UUID.randomUUID().toString();
        redis.opsForHash().putAll(REDIS_PREFIX + state, Map.of(
                "sub", sub,
                "email", email,
                "firstName", firstName,
                "lastName", lastName));
        return state;
    }

    private String completeBody(String state, String code) {
        return "{\"state\":\"" + state + "\",\"invitationCode\":\"" + code + "\"}";
    }

    private String adminToken() throws Exception {
        String body = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"localadmin\",\"password\":\"password88\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }
}
