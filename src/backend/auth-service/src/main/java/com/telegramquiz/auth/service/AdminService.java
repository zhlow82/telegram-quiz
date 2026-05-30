package com.telegramquiz.auth.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.telegramquiz.auth.dto.CreateUserRequest;
import com.telegramquiz.auth.dto.InvitationCodeResponse;
import com.telegramquiz.auth.dto.UpdateRoleRequest;
import com.telegramquiz.auth.dto.UserResponse;
import com.telegramquiz.auth.model.InvitationCode;
import com.telegramquiz.auth.model.User;
import com.telegramquiz.auth.repository.InvitationCodeRepository;
import com.telegramquiz.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final InvitationCodeRepository invitationCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    public List<InvitationCodeResponse> listCodes() {
        return invitationCodeRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(c -> new InvitationCodeResponse(
                        c.getId(), c.getCode(),
                        c.getCreatedBy().getUsername(),
                        c.getCreatedAt(), c.isActive()))
                .toList();
    }

    public InvitationCodeResponse generateCode(String adminUsername) {
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        InvitationCode ic = InvitationCode.builder()
                .code(code)
                .createdBy(admin)
                .active(true)
                .build();
        ic = invitationCodeRepository.save(ic);
        return new InvitationCodeResponse(ic.getId(), ic.getCode(), adminUsername, ic.getCreatedAt(), ic.isActive());
    }

    public void deactivateCode(Long id) {
        InvitationCode ic = invitationCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Code not found"));
        ic.setActive(false);
        invitationCodeRepository.save(ic);
    }

    public void deleteCode(Long id) {
        if (!invitationCodeRepository.existsById(id)) {
            throw new RuntimeException("Code not found");
        }
        invitationCodeRepository.deleteById(id);
    }

    public void activateCode(Long id) {
        InvitationCode ic = invitationCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Code not found"));
        ic.setActive(true);
        invitationCodeRepository.save(ic);
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getFirstName(), u.getLastName(), u.getEmail(), resolveProvider(u), resolveRole(u), u.isActive());
    }

    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        String name  = request.username();
        String first = (request.firstName() != null && !request.firstName().isBlank()) ? request.firstName().trim() : name;
        String last  = (request.lastName()  != null && !request.lastName().isBlank())  ? request.lastName().trim()  : name;
        User user = User.builder()
                .username(name)
                .password(passwordEncoder.encode(request.password()))
                .firstName(first)
                .lastName(last)
                .roles(new HashSet<>(Set.of("ROLE_MEMBER")))
                .active(true)
                .build();
        user = userRepository.save(user);
        return toResponse(user);
    }

    public UserResponse updateRole(Long id, UpdateRoleRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.getRoles().clear();
        user.getRoles().add(request.role());
        user = userRepository.save(user);
        return toResponse(user);
    }

    public void setUserActive(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(active);
        userRepository.save(user);
    }

    public void deleteUser(Long id, String requestingUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getUsername() != null && user.getUsername().equals(requestingUsername)) {
            throw new IllegalArgumentException("Cannot delete your own account");
        }
        // Invalidate any active refresh token in Redis
        String redisKey = "refresh:" + (user.getUsername() != null ? user.getUsername() : user.getEmail());
        redisTemplate.delete(redisKey);
        userRepository.delete(user);
    }

    public UserResponse updateProfile(Long id, com.telegramquiz.auth.dto.AdminUpdateProfileRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (request.firstName() != null) user.setFirstName(request.firstName().trim());
        if (request.lastName()  != null) user.setLastName(request.lastName().trim());
        user = userRepository.save(user);
        return toResponse(user);
    }

    public void resetPassword(Long id, com.telegramquiz.auth.dto.AdminResetPasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getGoogleSub() != null) {
            throw new IllegalStateException("Cannot set password for a Google account");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private String resolveRole(User u) {
        return u.getRoles().contains("ROLE_ADMIN") ? "ROLE_ADMIN" : "ROLE_MEMBER";
    }

    private String resolveProvider(User u) {
        return u.getGoogleSub() != null ? "google" : "local";
    }
}
