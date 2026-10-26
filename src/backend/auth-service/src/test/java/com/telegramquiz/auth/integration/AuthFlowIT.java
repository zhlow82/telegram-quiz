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
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;
import com.telegramquiz.auth.model.User;
import com.telegramquiz.auth.repository.UserRepository;
import com.telegramquiz.auth.support.TestContainers;

@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIT {

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

    @Test
    @DisplayName("auth / Logout revokes the session / Login, refresh, logout, then refresh fails")
    void loginRefreshLogout() throws Exception {
        String body = login("localadmin", "password88", 200);
        String accessToken = JsonPath.read(body, "$.accessToken");
        String refreshToken = JsonPath.read(body, "$.refreshToken");

        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("localadmin"));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());

        mockMvc.perform(post("/auth/logout").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("auth / Refresh tokens are not API credentials / Refresh token as bearer is rejected")
    void refreshTokenIsNotAnApiCredential() throws Exception {
        String body = login("localadmin", "password88", 200);
        String refreshToken = JsonPath.read(body, "$.refreshToken");

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("auth / Password sign-in / Deactivated account is rejected")
    void deactivatedAccountCannotSignIn() throws Exception {
        String username = "inactive-" + UUID.randomUUID().toString().substring(0, 8);
        userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode("secret123"))
                .firstName("Inactive")
                .lastName("User")
                .roles(Set.of("ROLE_MEMBER"))
                .active(false)
                .build());

        login(username, "secret123", 401);
    }

    @Test
    @DisplayName("auth / Administrator authorization / Member is forbidden, admin is allowed")
    void adminRouteRequiresAdminRole() throws Exception {
        String username = "member-" + UUID.randomUUID().toString().substring(0, 8);
        userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode("secret123"))
                .firstName("Member")
                .lastName("User")
                .roles(Set.of("ROLE_MEMBER"))
                .active(true)
                .build());

        String memberBody = login(username, "secret123", 200);
        String memberAccess = JsonPath.read(memberBody, "$.accessToken");

        mockMvc.perform(get("/auth/admin/users").header("Authorization", "Bearer " + memberAccess))
                .andExpect(status().isForbidden());

        String adminBody = login("localadmin", "password88", 200);
        String adminAccess = JsonPath.read(adminBody, "$.accessToken");

        mockMvc.perform(get("/auth/admin/users").header("Authorization", "Bearer " + adminAccess))
                .andExpect(status().isOk());
    }

    private String login(String username, String password, int expectedStatus) throws Exception {
        return mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().is(expectedStatus))
                .andReturn()
                .getResponse()
                .getContentAsString();
    }
}
