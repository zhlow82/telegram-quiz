package com.telegramquiz.main.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BotTokenEncryptionServiceTest {

    private static final String KEY = "ZGV2S2V5Rm9yVGVsZWdyYW1RdWl6QXBwMTIzNDU2Nzg=";

    private BotTokenEncryptionService service;

    @BeforeEach
    void setUp() {
        service = new BotTokenEncryptionService(KEY);
    }

    @Test
    @DisplayName("quiz-authoring / Bot token secrecy / Encrypt and decrypt round-trip")
    void roundTrip() {
        String token = "123456789:AAH-example-token";

        String encrypted = service.encrypt(token);

        assertThat(encrypted).isNotEqualTo(token);
        assertThat(service.decrypt(encrypted)).isEqualTo(token);
    }

    @Test
    @DisplayName("quiz-authoring / Bot token secrecy / Same token encrypts with a fresh IV")
    void freshIvPerEncryption() {
        String token = "123456789:AAH-example-token";

        String first = service.encrypt(token);
        String second = service.encrypt(token);

        assertThat(first).isNotEqualTo(second);
        assertThat(service.decrypt(first)).isEqualTo(token);
        assertThat(service.decrypt(second)).isEqualTo(token);
    }

    @Test
    @DisplayName("quiz-authoring / Bot token secrecy / Tampered ciphertext is rejected")
    void tamperedCiphertextIsRejected() {
        String encrypted = service.encrypt("123456789:AAH-example-token");
        byte[] bytes = Base64.getDecoder().decode(encrypted);
        bytes[bytes.length - 1] ^= 0x01;

        String tampered = Base64.getEncoder().encodeToString(bytes);

        assertThatThrownBy(() -> service.decrypt(tampered))
                .isInstanceOf(RuntimeException.class);
    }
}
