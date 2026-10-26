package com.telegramquiz.auth.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;
import com.telegramquiz.auth.model.User;
import com.telegramquiz.auth.repository.UserRepository;
import com.telegramquiz.auth.security.JwtUtil;
import com.telegramquiz.auth.support.TestContainers;

@SpringBootTest
@AutoConfigureMockMvc
class ProfileAndPasswordIT {

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainers.register(registry);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("auth / Password change / Changing a password")
    void changingPasswordReplacesTheOldOne() throws Exception {
        User user = seedLocalUser();
        String access = login(user.getUsername(), "secret123", 200);

        mockMvc.perform(post("/auth/change-password")
                        .header("Authorization", "Bearer " + access)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"secret123\",\"newPassword\":\"newsecret123\"}"))
                .andExpect(status().isNoContent());

        // The new password works, the old one no longer does
        login(user.getUsername(), "newsecret123", 200);
        login(user.getUsername(), "secret123", 401);
    }

    @Test
    @DisplayName("auth / Password change / Incorrect current password")
    void incorrectCurrentPasswordKeepsTheOldOne() throws Exception {
        User user = seedLocalUser();
        String access = login(user.getUsername(), "secret123", 200);

        mockMvc.perform(post("/auth/change-password")
                        .header("Authorization", "Bearer " + access)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"totally-wrong\",\"newPassword\":\"newsecret123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Current password is incorrect."));

        login(user.getUsername(), "secret123", 200);
    }

    @Test
    @DisplayName("auth / Password change / Google account changes password")
    void googleAccountCannotChangePassword() throws Exception {
        User user = seedGoogleUser();
        String access = jwtUtil.generateAccessToken(
                user.getUsername(), user.getId(), "ROLE_MEMBER", "google");

        mockMvc.perform(post("/auth/change-password")
                        .header("Authorization", "Bearer " + access)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"whatever\",\"newPassword\":\"newsecret123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Password change is not available for Google accounts."));
    }

    @Test
    @DisplayName("auth / Profile retrieval and update / Reading the profile")
    void profileReportsProviderAndFields() throws Exception {
        User local = seedLocalUser();
        String localAccess = login(local.getUsername(), "secret123", 200);

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + localAccess))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(local.getUsername()))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.email").value(local.getEmail()))
                .andExpect(jsonPath("$.provider").value("local"));

        // Google-linked account: provider is google, unset names/email report empty
        User google = seedGoogleUser();
        String googleAccess = jwtUtil.generateAccessToken(
                google.getUsername(), google.getId(), "ROLE_MEMBER", "google");

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + googleAccess))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(google.getUsername()))
                .andExpect(jsonPath("$.firstName").value(""))
                .andExpect(jsonPath("$.lastName").value(""))
                .andExpect(jsonPath("$.email").value(""))
                .andExpect(jsonPath("$.provider").value("google"));
    }

    @Test
    @DisplayName("auth / Profile retrieval and update / Clearing a name")
    void whitespaceNameIsStoredAsEmpty() throws Exception {
        User user = seedLocalUser();
        String access = login(user.getUsername(), "secret123", 200);

        mockMvc.perform(patch("/auth/profile")
                        .header("Authorization", "Bearer " + access)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"   \"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(""));
    }

    private User seedLocalUser() {
        String username = "user-" + UUID.randomUUID().toString().substring(0, 8);
        return userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode("secret123"))
                .firstName("Test")
                .lastName("User")
                .email(username + "@example.com")
                .roles(Set.of("ROLE_MEMBER"))
                .active(true)
                .build());
    }

    private User seedGoogleUser() {
        String username = "guser-" + UUID.randomUUID().toString().substring(0, 8);
        return userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .googleSub("g-" + UUID.randomUUID())
                .roles(Set.of("ROLE_MEMBER"))
                .active(true)
                .build());
    }

    private String login(String username, String password, int expectedStatus) throws Exception {
        String body = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().is(expectedStatus))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return expectedStatus == 200 ? JsonPath.read(body, "$.accessToken") : body;
    }
}
