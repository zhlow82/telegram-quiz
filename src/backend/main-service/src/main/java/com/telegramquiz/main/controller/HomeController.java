package com.telegramquiz.main.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class HomeController {

    @GetMapping("/home")
    public ResponseEntity<Map<String, String>> home(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "message", "Welcome, " + authentication.getName() + "!",
                "status", "authenticated"
        ));
    }
}
