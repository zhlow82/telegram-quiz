package com.telegramquiz.auth.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class AdminUserManagementIT {

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
    @DisplayName("auth / Administrator user management / Deleting own account")
    void adminCannotDeleteOwnAccount() throws Exception {
        String adminAccess = adminToken();
        Long adminId = userRepository.findByUsername("localadmin").orElseThrow().getId();

        mockMvc.perform(delete("/auth/admin/users/" + adminId)
                        .header("Authorization", "Bearer " + adminAccess))
                .andExpect(status().isBadRequest());

        assertThat(userRepository.findByUsername("localadmin")).isPresent();
    }

    @Test
    @DisplayName("auth / Administrator user management / Resetting a Google account password")
    void googleAccountPasswordResetIsRejected() throws Exception {
        User googleUser = seedGoogleUser();
        String hashBefore = googleUser.getPassword();
        String adminAccess = adminToken();

        mockMvc.perform(patch("/auth/admin/users/" + googleUser.getId() + "/password")
                        .header("Authorization", "Bearer " + adminAccess)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\":\"newpass123\"}"))
                .andExpect(status().isConflict());

        User reloaded = userRepository.findByUsername(googleUser.getUsername()).orElseThrow();
        assertThat(reloaded.getPassword()).isEqualTo(hashBefore);
    }

    @Test
    @DisplayName("auth / Administrator user management / Deactivating an account")
    void deactivatedAccountCannotSignInOrRefresh() throws Exception {
        User user = seedLocalUser();
        String body = login(user.getUsername(), "secret123");
        String refreshToken = JsonPath.read(body, "$.refreshToken");
        String adminAccess = adminToken();

        mockMvc.perform(patch("/auth/admin/users/" + user.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + adminAccess))
                .andExpect(status().isNoContent());

        // Sign-in is rejected...
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(user.getUsername(), "secret123")))
                .andExpect(status().isUnauthorized());

        // ...and the previously stored refresh token no longer works
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("auth / Refresh token rotation and storage / Refreshing an access token")
    void refreshReflectsCurrentRoleAfterPromotion() throws Exception {
        User user = seedLocalUser();
        String body = login(user.getUsername(), "secret123");
        String refreshToken = JsonPath.read(body, "$.refreshToken");
        String adminAccess = adminToken();

        mockMvc.perform(patch("/auth/admin/users/" + user.getId() + "/role")
                        .header("Authorization", "Bearer " + adminAccess)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ROLE_ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));

        // The refreshed access token carries the account's current role...
        String refreshBody = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        String newAccess = JsonPath.read(refreshBody, "$.accessToken");
        assertThat(jwtUtil.extractRole(newAccess)).isEqualTo("ROLE_ADMIN");

        // ...and unlocks admin routes
        mockMvc.perform(get("/auth/admin/users")
                        .header("Authorization", "Bearer " + newAccess))
                .andExpect(status().isOk());
    }

    private User seedLocalUser() {
        String username = "user-" + UUID.randomUUID().toString().substring(0, 8);
        return userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode("secret123"))
                .firstName("Test")
                .lastName("User")
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

    private String loginBody(String username, String password) {
        return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
    }

    private String login(String username, String password) throws Exception {
        return mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(username, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private String adminToken() throws Exception {
        String body = login("localadmin", "password88");
        return JsonPath.read(body, "$.accessToken");
    }
}
