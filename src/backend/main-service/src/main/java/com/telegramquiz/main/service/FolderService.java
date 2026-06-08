package com.telegramquiz.main.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.telegramquiz.main.dto.FolderInviteRequestDto;
import com.telegramquiz.main.dto.FolderMemberDto;
import com.telegramquiz.main.dto.FolderRequestDto;
import com.telegramquiz.main.dto.FolderResponseDto;
import com.telegramquiz.main.dto.FolderRoleUpdateDto;
import com.telegramquiz.main.entity.Folder;
import com.telegramquiz.main.entity.FolderAccessLevel;
import com.telegramquiz.main.entity.FolderMember;
import com.telegramquiz.main.entity.FolderMemberStatus;
import com.telegramquiz.main.entity.FolderRole;
import com.telegramquiz.main.entity.Question;
import com.telegramquiz.main.repository.FolderMemberRepository;
import com.telegramquiz.main.repository.FolderRepository;
import com.telegramquiz.main.repository.QuestionRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final QuestionRepository questionRepository;
    private final FolderMemberRepository memberRepository;

    // ── Read ──────────────────────────────────────────────────────────────────

    public List<FolderResponseDto> findAll(String username) {
        List<FolderResponseDto> owned = folderRepository
                .findAllByCreatedByOrderBySortOrderAscCreatedAtAsc(username)
                .stream()
                .map(f -> toDto(f, "OWNER"))
                .toList();

        List<FolderResponseDto> shared = folderRepository
                .findSharedWithUser(username, FolderMemberStatus.ACCEPTED)
                .stream()
                .map(f -> {
                    FolderMember m = memberRepository.findByFolderIdAndUsername(f.getId(), username).orElseThrow();
                    return toDto(f, m.getRole().name());
                })
                .toList();

        return Stream.concat(owned.stream(), shared.stream()).toList();
    }

    public List<FolderMemberDto> getMembers(Long folderId, String username) {
        requireMinAccess(folderId, username, FolderAccessLevel.CONTRIBUTOR);
        return memberRepository.findAllByFolderId(folderId)
                .stream()
                .map(m -> toMemberDto(m, null))
                .toList();
    }

    public List<FolderMemberDto> getPendingInvitations(String username) {
        return memberRepository.findAllByUsernameAndStatus(username, FolderMemberStatus.PENDING)
                .stream()
                .map(m -> {
                    String folderName = folderRepository.findById(m.getFolderId())
                            .map(Folder::getName).orElse("(deleted)");
                    return toMemberDto(m, folderName);
                })
                .toList();
    }

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public FolderResponseDto create(FolderRequestDto dto, String username) {
        int sortOrder = folderRepository.findAllByCreatedByOrderBySortOrderAscCreatedAtAsc(username).size();
        Folder folder = Folder.builder()
                .name(dto.name())
                .createdBy(username)
                .sortOrder(sortOrder)
                .build();
        return toDto(folderRepository.save(folder), "OWNER");
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Transactional
    public void reorder(List<Long> orderedIds, String username) {
        Map<Long, Folder> byId = folderRepository
                .findAllByCreatedByOrderBySortOrderAscCreatedAtAsc(username)
                .stream()
                .collect(Collectors.toMap(Folder::getId, f -> f));
        for (int i = 0; i < orderedIds.size(); i++) {
            Folder f = byId.get(orderedIds.get(i));
            if (f != null) f.setSortOrder(i);
        }
        folderRepository.saveAll(byId.values());
    }

    @Transactional
    public FolderResponseDto rename(Long id, FolderRequestDto dto, String username) {
        requireMinAccess(id, username, FolderAccessLevel.CO_OWNER);
        Folder folder = folderRepository.findById(id).orElseThrow();
        folder.setName(dto.name());
        return toDto(folderRepository.save(folder), getAccessLevel(id, username).name());
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Transactional
    public void delete(Long id, String username) {
        requireMinAccess(id, username, FolderAccessLevel.CO_OWNER);
        Folder folder = folderRepository.findById(id).orElseThrow();
        List<Question> questions = questionRepository.findAllByFolderIdOrderByOrderIndexAsc(id);
        questions.forEach(q -> q.setFolderId(null));
        questionRepository.saveAll(questions);
        memberRepository.deleteAllByFolderId(id);
        folderRepository.delete(folder);
    }

    // ── Member management ─────────────────────────────────────────────────────

    @Transactional
    public FolderMemberDto invite(Long folderId, FolderInviteRequestDto dto, String username) {
        requireMinAccess(folderId, username, FolderAccessLevel.CO_OWNER);
        if (username.equals(dto.username())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot invite yourself");
        }
        Folder folder = folderRepository.findById(folderId).orElseThrow();
        if (folder.getCreatedBy().equals(dto.username())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already the folder owner");
        }
        memberRepository.findByFolderIdAndUsername(folderId, dto.username()).ifPresent(m -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already has a pending or active invitation");
        });
        FolderRole role;
        try {
            role = FolderRole.valueOf(dto.role().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role: " + dto.role());
        }
        FolderMember member = FolderMember.builder()
                .folderId(folderId)
                .username(dto.username())
                .role(role)
                .invitedBy(username)
                .build();
        return toMemberDto(memberRepository.save(member), folder.getName());
    }

    @Transactional
    public void removeMember(Long folderId, String targetUsername, String requestingUser) {
        requireMinAccess(folderId, requestingUser, FolderAccessLevel.CO_OWNER);
        FolderMember member = memberRepository.findByFolderIdAndUsername(folderId, targetUsername)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));
        memberRepository.delete(member);
    }

    @Transactional
    public FolderMemberDto updateMemberRole(Long folderId, String targetUsername, FolderRoleUpdateDto dto, String requestingUser) {
        requireMinAccess(folderId, requestingUser, FolderAccessLevel.CO_OWNER);
        FolderMember member = memberRepository.findByFolderIdAndUsername(folderId, targetUsername)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));
        try {
            member.setRole(FolderRole.valueOf(dto.role().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role: " + dto.role());
        }
        return toMemberDto(memberRepository.save(member), null);
    }

    @Transactional
    public void acceptInvitation(Long invitationId, String username) {
        FolderMember member = memberRepository.findById(invitationId)
                .orElseThrow(() -> new EntityNotFoundException("Invitation not found"));
        if (!member.getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your invitation");
        }
        if (member.getStatus() != FolderMemberStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invitation already processed");
        }
        member.setStatus(FolderMemberStatus.ACCEPTED);
        memberRepository.save(member);
    }

    @Transactional
    public void declineInvitation(Long invitationId, String username) {
        FolderMember member = memberRepository.findById(invitationId)
                .orElseThrow(() -> new EntityNotFoundException("Invitation not found"));
        if (!member.getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your invitation");
        }
        member.setStatus(FolderMemberStatus.DECLINED);
        memberRepository.save(member);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    FolderAccessLevel getAccessLevel(Long folderId, String username) {
        Folder folder = folderRepository.findById(folderId).orElse(null);
        if (folder == null) return FolderAccessLevel.NONE;
        if (folder.getCreatedBy().equals(username)) return FolderAccessLevel.OWNER;
        return memberRepository.findByFolderIdAndUsername(folderId, username)
                .filter(m -> m.getStatus() == FolderMemberStatus.ACCEPTED)
                .map(m -> m.getRole() == FolderRole.CO_OWNER ? FolderAccessLevel.CO_OWNER : FolderAccessLevel.CONTRIBUTOR)
                .orElse(FolderAccessLevel.NONE);
    }

    private void requireMinAccess(Long folderId, String username, FolderAccessLevel minimum) {
        FolderAccessLevel actual = getAccessLevel(folderId, username);
        if (actual == FolderAccessLevel.NONE) {
            throw new EntityNotFoundException("Folder not found: " + folderId);
        }
        if (actual.ordinal() > minimum.ordinal()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient folder access");
        }
    }

    private FolderResponseDto toDto(Folder f, String role) {
        return new FolderResponseDto(f.getId(), f.getName(), f.getCreatedBy(), f.getCreatedAt(), role);
    }

    private FolderMemberDto toMemberDto(FolderMember m, String folderName) {
        return new FolderMemberDto(
                m.getId(), m.getFolderId(), folderName,
                m.getUsername(), m.getRole().name(),
                m.getStatus().name(), m.getInvitedBy(), m.getCreatedAt());
    }
}
