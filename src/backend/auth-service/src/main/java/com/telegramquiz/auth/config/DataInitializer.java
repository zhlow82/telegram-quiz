package com.telegramquiz.auth.config;

import com.telegramquiz.auth.model.User;
import com.telegramquiz.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("zhlow").isEmpty()) {
            User user = User.builder()
                    .username("zhlow")
                    .password(passwordEncoder.encode("password88"))
                    .roles(Set.of("ROLE_USER"))
                    .build();
            userRepository.save(user);
            log.info("Default user 'zhlow' created successfully");
        } else {
            log.info("Default user 'zhlow' already exists");
        }
    }
}
