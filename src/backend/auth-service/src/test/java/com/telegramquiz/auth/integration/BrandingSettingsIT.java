package com.telegramquiz.auth.integration;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;
import com.telegramquiz.auth.repository.AppSettingRepository;
import com.telegramquiz.auth.support.TestContainers;

/**
 * Ordered: the branding defaults must be observed before the administrator
 * write, since both scenarios read the same global settings.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BrandingSettingsIT {

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainers.register(registry);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppSettingRepository appSettingRepository;

    @Test
    @Order(1)
    @DisplayName("auth / Public branding settings / No branding configured")
    void defaultsAreServedWithoutAuthentication() throws Exception {
        appSettingRepository.deleteById("app_name");
        appSettingRepository.deleteById("login_welcome_text");
        appSettingRepository.deleteById("app_logo_blob_id");

        mockMvc.perform(get("/auth/settings/branding"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appName").value("Telegram Quiz"))
                .andExpect(jsonPath("$.loginWelcomeText").value(""))
                .andExpect(jsonPath("$.appLogoUrl").value(nullValue()));
    }

    @Test
    @Order(2)
    @DisplayName("auth / Public branding settings / Custom logo")
    void configuredLogoIsExposedAsPublicFileUrl() throws Exception {
        String body = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"localadmin\",\"password\":\"password88\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String adminAccess = JsonPath.read(body, "$.accessToken");

        mockMvc.perform(put("/auth/admin/settings/branding")
                        .header("Authorization", "Bearer " + adminAccess)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appName\":\"My Quiz\",\"loginWelcomeText\":\"Welcome!\",\"appLogoBlobId\":\"blob-42\"}"))
                .andExpect(status().isNoContent());

        // Public endpoint, no authentication
        mockMvc.perform(get("/auth/settings/branding"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appName").value("My Quiz"))
                .andExpect(jsonPath("$.loginWelcomeText").value("Welcome!"))
                .andExpect(jsonPath("$.appLogoUrl").value("/api/files/blob-42"));
    }
}
