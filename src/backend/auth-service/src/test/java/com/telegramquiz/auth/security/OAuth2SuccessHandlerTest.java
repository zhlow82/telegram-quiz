package com.telegramquiz.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import com.telegramquiz.auth.model.User;
import com.telegramquiz.auth.repository.UserRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@ExtendWith(MockitoExtension.class)
class OAuth2SuccessHandlerTest {

    private static final String SECRET =
            "dGVsZWdyYW1xdWl6LWp3dC1zZWNyZXQta2V5LWZvci1kZXZlbG9wbWVudC11c2Utb25seS0yMDI2";
    private static final String FRONTEND_URL = "http://localhost:5173";
    private static final String CALLBACK_PREFIX = FRONTEND_URL + "/oauth2/callback?token=";

    @Mock
    private UserRepository userRepository;

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    private JwtUtil jwtUtil;
    private OAuth2SuccessHandler handler;

    @BeforeEach
    void setUp() {
        jwtUtil = newJwtUtil(86_400_000L);
        handler = new OAuth2SuccessHandler(userRepository, jwtUtil, redis);
        ReflectionTestUtils.setField(handler, "frontendUrl", FRONTEND_URL);
    }

    @Test
    @DisplayName("auth / Google sign-in for registered accounts / Returning Google user")
    void returningGoogleUserIsRedirectedWithAccessToken() throws Exception {
        User user = User.builder()
                .id(5L)
                .username("guser")
                .password("unused")
                .googleSub("g-sub-1")
                .roles(Set.of("ROLE_MEMBER"))
                .build();
        when(userRepository.findByGoogleSub("g-sub-1")).thenReturn(Optional.of(user));

        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(new MockHttpServletRequest(), response, googleAuth("g-sub-1"));

        String redirect = response.getRedirectedUrl();
        assertThat(redirect).startsWith(CALLBACK_PREFIX);
        String token = redirect.substring(CALLBACK_PREFIX.length());

        assertThat(jwtUtil.validateToken(token)).isTrue();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("guser");
        assertThat(jwtUtil.extractType(token)).isEqualTo("access");
        assertThat(jwtUtil.extractRole(token)).isEqualTo("ROLE_MEMBER");
        assertThat(providerClaim(token)).isEqualTo("google");
        // Google sign-in issues no refresh token and writes no pending state
        verifyNoInteractions(redis);
    }

    @Test
    @DisplayName("auth / Google sign-in for registered accounts / Session lifetime for Google users")
    void googleSessionEndsWhenAccessTokenExpires() throws Exception {
        User user = User.builder()
                .id(6L)
                .username("guser2")
                .password("unused")
                .googleSub("g-sub-2")
                .roles(Set.of("ROLE_ADMIN"))
                .build();
        when(userRepository.findByGoogleSub("g-sub-2")).thenReturn(Optional.of(user));

        // Same handler flow, but the issued token is already past its 24h lifetime
        OAuth2SuccessHandler expiredHandler =
                new OAuth2SuccessHandler(userRepository, newJwtUtil(-1_000L), redis);
        ReflectionTestUtils.setField(expiredHandler, "frontendUrl", FRONTEND_URL);

        MockHttpServletResponse response = new MockHttpServletResponse();
        expiredHandler.onAuthenticationSuccess(new MockHttpServletRequest(), response, googleAuth("g-sub-2"));

        String redirect = response.getRedirectedUrl();
        assertThat(redirect).startsWith(CALLBACK_PREFIX);
        String token = redirect.substring(CALLBACK_PREFIX.length());

        // The expired token is rejected and there is no refresh path — the user
        // must sign in with Google again
        assertThat(jwtUtil.validateToken(token)).isFalse();
        assertThat(redirect).doesNotContain("refreshToken");
    }

    @Test
    @DisplayName("auth / Invitation-gated Google registration / Unknown profile is held for ten minutes")
    void unknownGoogleUserIsHeldInRedisForRegistration() throws Exception {
        when(userRepository.findByGoogleSub("g-sub-new")).thenReturn(Optional.empty());
        when(redis.<Object, Object>opsForHash()).thenReturn(hashOperations);

        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(new MockHttpServletRequest(), response, googleAuth("g-sub-new"));

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<Object, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(hashOperations).putAll(keyCaptor.capture(), mapCaptor.capture());
        verify(redis).expire(startsWith("oauth2:pending:"), eq(Duration.ofMinutes(10)));

        String key = keyCaptor.getValue();
        assertThat(key).startsWith("oauth2:pending:");
        assertThat(mapCaptor.getValue())
                .containsEntry("sub", "g-sub-new")
                // attributes Google did not send are stored as empty strings
                .containsEntry("email", "")
                .containsEntry("firstName", "")
                .containsEntry("lastName", "");

        String state = key.substring("oauth2:pending:".length());
        assertThat(response.getRedirectedUrl())
                .isEqualTo(FRONTEND_URL + "/oauth2/register?state=" + state);
    }

    private OAuth2AuthenticationToken googleAuth(String sub) {
        DefaultOAuth2User principal = new DefaultOAuth2User(List.of(), Map.of("sub", sub), "sub");
        return new OAuth2AuthenticationToken(principal, List.of(), "google");
    }

    private JwtUtil newJwtUtil(long expiration) {
        JwtUtil util = new JwtUtil();
        ReflectionTestUtils.setField(util, "secret", SECRET);
        ReflectionTestUtils.setField(util, "expiration", expiration);
        ReflectionTestUtils.setField(util, "refreshExpiration", 604_800_000L);
        return util;
    }

    private Object providerClaim(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("provider");
    }
}
