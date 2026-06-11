package com.telegramquiz.auth.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.telegramquiz.auth.model.User;
import com.telegramquiz.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        var existing = userRepository.findByUsername("localadmin");
        if (existing.isEmpty()) {
            User user = User.builder()
                    .username("localadmin")
                    .password(passwordEncoder.encode("szR.ir=-:Un~}RYyxZ0c"))
                    .firstName("localadmin")
                    .lastName("localadmin")
                    .roles(new HashSet<>(Set.of("ROLE_ADMIN")))
                    .build();
            userRepository.save(user);
            log.info("Default user 'localadmin' created successfully");
        } else {
            User user = existing.get();
            if (!user.getRoles().contains("ROLE_ADMIN")) {
                user.getRoles().clear();
                user.getRoles().add("ROLE_ADMIN");
                userRepository.save(user);
                log.info("Default user 'localadmin' role upgraded to ROLE_ADMIN");
            } else {
                log.info("Default user 'localadmin' already exists");
            }
        }
    }
}
