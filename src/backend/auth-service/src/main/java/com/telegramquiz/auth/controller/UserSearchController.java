package com.telegramquiz.auth.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.telegramquiz.auth.model.User;
import com.telegramquiz.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth/users")
@RequiredArgsConstructor
public class UserSearchController {

    private final UserRepository userRepository;

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, String>>> search(
            @RequestParam String q,
            @AuthenticationPrincipal String username) {
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<User> users = userRepository.searchUsers(
                q.trim(), username, PageRequest.of(0, 10));
        List<Map<String, String>> results = users.stream()
                .map(u -> Map.of(
                        "username", u.getUsername(),
                        "firstName", u.getFirstName() != null ? u.getFirstName() : "",
                        "lastName", u.getLastName() != null ? u.getLastName() : ""))
                .toList();
        return ResponseEntity.ok(results);
    }
}
