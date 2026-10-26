package com.telegramquiz.auth.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;
import com.telegramquiz.auth.model.User;
import com.telegramquiz.auth.repository.UserRepository;
import com.telegramquiz.auth.security.JwtUtil;
import com.telegramquiz.auth.support.TestContainers;

@SpringBootTest
@AutoConfigureMockMvc
class AuthLoginIT {

    private static final String DEV_SECRET =
            "dGVsZWdyYW1xdWl6LWp3dC1zZWNyZXQta2V5LWZvci1kZXZlbG9wbWVudC11c2Utb25seS0yMDI2";

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
    @DisplayName("auth / Password sign-in / Successful sign-in")
    void successfulSignInReturnsBearerTokens() throws Exception {
        User user = seedLocalUser(true);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(user.getUsername(), "secret123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("auth / Password sign-in / Wrong password")
    void wrongPasswordIsRejectedWithoutTokens() throws Exception {
        User user = seedLocalUser(true);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(user.getUsername(), "wrong-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
    }

    @Test
    @DisplayName("auth / Access token contents and lifetime / Expired access token")
    void expiredAccessTokenIsTreatedAsUnauthenticated() throws Exception {
        User user = seedLocalUser(true);

        // Token signed with the same dev secret but already past its lifetime
        JwtUtil expiredIssuer = new JwtUtil();
        ReflectionTestUtils.setField(expiredIssuer, "secret", DEV_SECRET);
        ReflectionTestUtils.setField(expiredIssuer, "expiration", -1_000L);
        ReflectionTestUtils.setField(expiredIssuer, "refreshExpiration", 604_800_000L);
        String expiredToken = expiredIssuer.generateAccessToken(
                user.getUsername(), user.getId(), "ROLE_MEMBER", "local");

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("auth / Refresh token rotation and storage / Signing in on a second device")
    void secondSignInReplacesFirstRefreshToken() throws Exception {
        User user = seedLocalUser(true);

        String firstBody = login(user.getUsername(), "secret123", 200);
        String firstRefresh = JsonPath.read(firstBody, "$.refreshToken");

        String secondBody = login(user.getUsername(), "secret123", 200);
        String secondRefresh = JsonPath.read(secondBody, "$.refreshToken");
        assertThat(secondRefresh).isNotEqualTo(firstRefresh);

        // The first device's refresh token stopped working when the second signed in
        refresh(firstRefresh, 401);
        refresh(secondRefresh, 200);
    }

    @Test
    @DisplayName("auth / Refresh token rotation and storage / Revoked refresh token")
    void neverStoredRefreshTokenIsRejected() throws Exception {
        User user = seedLocalUser(true);

        // Valid signature and type=refresh, but the user never signed in, so no
        // token is stored in Redis for them
        String unstoredToken = jwtUtil.generateRefreshToken(user.getUsername());

        refresh(unstoredToken, 401);
    }

    @Test
    @DisplayName("auth / Logout revokes the session / Logout without a valid token")
    void logoutWithoutValidTokenStillReturns204() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/auth/logout").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isNoContent());
    }

    private User seedLocalUser(boolean active) {
        String username = "user-" + UUID.randomUUID().toString().substring(0, 8);
        return userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode("secret123"))
                .firstName("Test")
                .lastName("User")
                .roles(Set.of("ROLE_MEMBER"))
                .active(active)
                .build());
    }

    private String loginBody(String username, String password) {
        return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
    }

    private String login(String username, String password, int expectedStatus) throws Exception {
        return mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(username, password)))
                .andExpect(status().is(expectedStatus))
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private void refresh(String refreshToken, int expectedStatus) throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().is(expectedStatus));
    }
}
