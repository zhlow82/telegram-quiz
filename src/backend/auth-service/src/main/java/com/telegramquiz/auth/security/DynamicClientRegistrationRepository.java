package com.telegramquiz.auth.security;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.stereotype.Component;

import com.telegramquiz.auth.repository.AppSettingRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DynamicClientRegistrationRepository
        implements ClientRegistrationRepository, Iterable<ClientRegistration> {

    private final AppSettingRepository settingsRepository;

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        if (!"google".equals(registrationId)) return null;
        return buildRegistration().orElse(null);
    }

    @Override
    public Iterator<ClientRegistration> iterator() {
        return buildRegistration()
                .map(r -> Collections.singletonList(r).iterator())
                .orElse(Collections.<ClientRegistration>emptyList().iterator());
    }

    public boolean isConfigured() {
        return settingsRepository.findByKey("google_client_id").isPresent()
                && settingsRepository.findByKey("google_client_secret").isPresent();
    }

    private Optional<ClientRegistration> buildRegistration() {
        Optional<String> clientId     = settingsRepository.findByKey("google_client_id").map(s -> s.getValue());
        Optional<String> clientSecret = settingsRepository.findByKey("google_client_secret").map(s -> s.getValue());

        if (clientId.isEmpty() || clientSecret.isEmpty()) return Optional.empty();

        return Optional.of(ClientRegistration.withRegistrationId("google")
                .clientId(clientId.get())
                .clientSecret(clientSecret.get())
                .scope("openid", "profile", "email")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .clientName("Google")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .build());
    }
}
