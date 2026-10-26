package com.telegramquiz.auth.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtUtilTest {

    private static final String SECRET =
            "dGVsZWdyYW1xdWl6LWp3dC1zZWNyZXQta2V5LWZvci1kZXZlbG9wbWVudC11c2Utb25seS0yMDI2";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86_400_000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", 604_800_000L);
    }

    @Test
    @DisplayName("auth / Access token contents and lifetime / Claims are signed and readable")
    void accessTokenCarriesExpectedClaims() {
        String token = jwtUtil.generateAccessToken("alice", 7L, "ROLE_ADMIN", "local");

        assertThat(jwtUtil.validateToken(token)).isTrue();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("alice");
        assertThat(jwtUtil.extractUserId(token)).isEqualTo(7L);
        assertThat(jwtUtil.extractRole(token)).isEqualTo("ROLE_ADMIN");
        assertThat(jwtUtil.extractType(token)).isEqualTo("access");
    }

    @Test
    @DisplayName("auth / Access token contents and lifetime / Expired access token is rejected")
    void expiredAccessTokenIsRejected() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1_000L);

        String token = jwtUtil.generateAccessToken("alice", 7L, "ROLE_MEMBER", "local");

        assertThat(jwtUtil.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("auth / Refresh token rotation and storage / Refresh token has the refresh type")
    void refreshTokenHasRefreshType() {
        String token = jwtUtil.generateRefreshToken("alice");

        assertThat(jwtUtil.validateToken(token)).isTrue();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("alice");
        assertThat(jwtUtil.extractType(token)).isEqualTo("refresh");
        assertThat(jwtUtil.extractRole(token)).isNull();
        assertThat(jwtUtil.extractUserId(token)).isNull();
    }

    @Test
    @DisplayName("auth / Access token contents and lifetime / Malformed token is rejected")
    void malformedTokenIsRejected() {
        assertThat(jwtUtil.validateToken("not-a-jwt")).isFalse();
    }
}
