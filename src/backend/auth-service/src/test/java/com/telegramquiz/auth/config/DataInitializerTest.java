package com.telegramquiz.auth.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.telegramquiz.auth.model.User;
import com.telegramquiz.auth.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private DataInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new DataInitializer(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("auth / Built-in administrator account / First startup")
    void firstStartupCreatesLocalAdmin() throws Exception {
        when(userRepository.findByUsername("localadmin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password88")).thenReturn("encoded-password88");

        initializer.run();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("localadmin");
        assertThat(saved.getPassword()).isEqualTo("encoded-password88");
        assertThat(saved.getRoles()).containsExactly("ROLE_ADMIN");
        assertThat(saved.isActive()).isTrue();
    }

    @Test
    @DisplayName("auth / Built-in administrator account / Role removed manually")
    void removedRoleIsRestoredWithoutTouchingPassword() throws Exception {
        User existing = User.builder()
                .username("localadmin")
                .password("existing-hash")
                .firstName("localadmin")
                .lastName("localadmin")
                .roles(new HashSet<>(Set.of("ROLE_MEMBER")))
                .build();
        when(userRepository.findByUsername("localadmin")).thenReturn(Optional.of(existing));

        initializer.run();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved).isSameAs(existing);
        assertThat(saved.getRoles()).containsExactly("ROLE_ADMIN");
        assertThat(saved.getPassword()).isEqualTo("existing-hash");
        verify(passwordEncoder, never()).encode(anyString());
    }
}
