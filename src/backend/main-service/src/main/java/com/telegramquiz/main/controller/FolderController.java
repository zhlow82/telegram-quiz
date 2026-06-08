package com.telegramquiz.main.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.telegramquiz.main.dto.FolderInviteRequestDto;
import com.telegramquiz.main.dto.FolderMemberDto;
import com.telegramquiz.main.dto.FolderReorderRequestDto;
import com.telegramquiz.main.dto.FolderRequestDto;
import com.telegramquiz.main.dto.FolderResponseDto;
import com.telegramquiz.main.dto.FolderRoleUpdateDto;
import com.telegramquiz.main.service.FolderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @GetMapping
    public ResponseEntity<List<FolderResponseDto>> list(@AuthenticationPrincipal String username) {
        return ResponseEntity.ok(folderService.findAll(username));
    }

    @PostMapping
    public ResponseEntity<FolderResponseDto> create(
            @Valid @RequestBody FolderRequestDto dto,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.status(HttpStatus.CREATED).body(folderService.create(dto, username));
    }

    @PatchMapping("/reorder")
    public ResponseEntity<Void> reorder(
            @RequestBody FolderReorderRequestDto dto,
            @AuthenticationPrincipal String username) {
        folderService.reorder(dto.orderedIds(), username);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FolderResponseDto> rename(
            @PathVariable Long id,
            @Valid @RequestBody FolderRequestDto dto,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(folderService.rename(id, dto, username));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal String username) {
        folderService.delete(id, username);
        return ResponseEntity.noContent().build();
    }

    // ── Member management ─────────────────────────────────────────────────────

    @PostMapping("/{id}/members")
    public ResponseEntity<FolderMemberDto> invite(
            @PathVariable Long id,
            @Valid @RequestBody FolderInviteRequestDto dto,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.status(HttpStatus.CREATED).body(folderService.invite(id, dto, username));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<FolderMemberDto>> getMembers(
            @PathVariable Long id,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(folderService.getMembers(id, username));
    }

    @DeleteMapping("/{id}/members/{targetUsername}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long id,
            @PathVariable String targetUsername,
            @AuthenticationPrincipal String username) {
        folderService.removeMember(id, targetUsername, username);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/members/{targetUsername}")
    public ResponseEntity<FolderMemberDto> updateMemberRole(
            @PathVariable Long id,
            @PathVariable String targetUsername,
            @Valid @RequestBody FolderRoleUpdateDto dto,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(folderService.updateMemberRole(id, targetUsername, dto, username));
    }

    // ── Invitations ───────────────────────────────────────────────────────────

    @GetMapping("/invitations")
    public ResponseEntity<List<FolderMemberDto>> getInvitations(@AuthenticationPrincipal String username) {
        return ResponseEntity.ok(folderService.getPendingInvitations(username));
    }

    @PostMapping("/invitations/{id}/accept")
    public ResponseEntity<Void> acceptInvitation(
            @PathVariable Long id,
            @AuthenticationPrincipal String username) {
        folderService.acceptInvitation(id, username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/invitations/{id}/decline")
    public ResponseEntity<Void> declineInvitation(
            @PathVariable Long id,
            @AuthenticationPrincipal String username) {
        folderService.declineInvitation(id, username);
        return ResponseEntity.noContent().build();
    }
}
