package com.telegramquiz.main.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telegramquiz.main.dto.FolderInviteRequestDto;
import com.telegramquiz.main.entity.Folder;
import com.telegramquiz.main.entity.FolderMember;
import com.telegramquiz.main.entity.FolderMemberStatus;
import com.telegramquiz.main.entity.FolderRole;
import com.telegramquiz.main.repository.FolderMemberRepository;
import com.telegramquiz.main.repository.FolderRepository;

@DisplayName("folders-sharing / Invitations")
class FolderInvitationIT extends FolderIntegrationTestBase {

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private FolderMemberRepository memberRepository;

    private Folder folder;
    private FolderMember owner;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
        folderRepository.deleteAll();

        folder = Folder.builder()
                .name("Team Folder")
                .createdBy("alice")
                .sortOrder(0)
                .build();
        folderRepository.save(folder);

        owner = FolderMember.builder()
                .folderId(folder.getId())
                .username("alice")
                .role(FolderRole.CO_OWNER)
                .status(FolderMemberStatus.ACCEPTED)
                .invitedBy("alice")
                .build();
        memberRepository.save(owner);
    }

    @Test
    @DisplayName("Successful invitation")
    void coOwnerCanInviteValidRole() throws Exception {
        mockMvc.perform(post("/api/folders/" + folder.getId() + "/members")
                        .with(asUser("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new FolderInviteRequestDto("bob", "CONTRIBUTOR"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("bob"))
                .andExpect(jsonPath("$.role").value("CONTRIBUTOR"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Inviting the owner")
    void inviteOwnerReturns400() throws Exception {
        mockMvc.perform(post("/api/folders/" + folder.getId() + "/members")
                        .with(asUser("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new FolderInviteRequestDto("alice", "CONTRIBUTOR"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Duplicate invitation")
    void duplicateInvitationReturns409() throws Exception {
        FolderMember existing = FolderMember.builder()
                .folderId(folder.getId())
                .username("bob")
                .role(FolderRole.CONTRIBUTOR)
                .status(FolderMemberStatus.PENDING)
                .invitedBy("alice")
                .build();
        memberRepository.save(existing);

        mockMvc.perform(post("/api/folders/" + folder.getId() + "/members")
                        .with(asUser("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new FolderInviteRequestDto("bob", "CONTRIBUTOR"))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Invalid role")
    void invalidRoleReturns400() throws Exception {
        mockMvc.perform(post("/api/folders/" + folder.getId() + "/members")
                        .with(asUser("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new FolderInviteRequestDto("bob", "INVALID_ROLE"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Accepting an invitation")
    void acceptInvitationMakesFolderVisible() throws Exception {
        FolderMember pending = FolderMember.builder()
                .folderId(folder.getId())
                .username("bob")
                .role(FolderRole.CONTRIBUTOR)
                .status(FolderMemberStatus.PENDING)
                .invitedBy("alice")
                .build();
        memberRepository.save(pending);

        mockMvc.perform(post("/api/folders/invitations/" + pending.getId() + "/accept")
                        .with(asUser("bob")))
                .andExpect(status().isNoContent());

        FolderMember accepted = memberRepository.findById(pending.getId()).orElseThrow();
        assert accepted.getStatus() == FolderMemberStatus.ACCEPTED;
    }

    @Test
    @DisplayName("Declining an invitation")
    void declineInvitationKeepsFolderHidden() throws Exception {
        FolderMember pending = FolderMember.builder()
                .folderId(folder.getId())
                .username("bob")
                .role(FolderRole.CONTRIBUTOR)
                .status(FolderMemberStatus.PENDING)
                .invitedBy("alice")
                .build();
        memberRepository.save(pending);

        mockMvc.perform(post("/api/folders/invitations/" + pending.getId() + "/decline")
                        .with(asUser("bob")))
                .andExpect(status().isNoContent());

        FolderMember declined = memberRepository.findById(pending.getId()).orElseThrow();
        assert declined.getStatus() == FolderMemberStatus.DECLINED;
    }

    @Test
    @DisplayName("Processing someone else's invitation")
    void acceptOthersInvitationReturns403() throws Exception {
        FolderMember pending = FolderMember.builder()
                .folderId(folder.getId())
                .username("bob")
                .role(FolderRole.CONTRIBUTOR)
                .status(FolderMemberStatus.PENDING)
                .invitedBy("alice")
                .build();
        memberRepository.save(pending);

        mockMvc.perform(post("/api/folders/invitations/" + pending.getId() + "/accept")
                        .with(asUser("carol")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Processing a resolved invitation")
    void acceptAlreadyAcceptedReturns400() throws Exception {
        FolderMember accepted = FolderMember.builder()
                .folderId(folder.getId())
                .username("bob")
                .role(FolderRole.CONTRIBUTOR)
                .status(FolderMemberStatus.ACCEPTED)
                .invitedBy("alice")
                .build();
        memberRepository.save(accepted);

        mockMvc.perform(post("/api/folders/invitations/" + accepted.getId() + "/accept")
                        .with(asUser("bob")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Declined user cannot be re-invited")
    void reInviteDeclinedUserReturns409() throws Exception {
        FolderMember declined = FolderMember.builder()
                .folderId(folder.getId())
                .username("bob")
                .role(FolderRole.CONTRIBUTOR)
                .status(FolderMemberStatus.DECLINED)
                .invitedBy("alice")
                .build();
        memberRepository.save(declined);

        mockMvc.perform(post("/api/folders/" + folder.getId() + "/members")
                        .with(asUser("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new FolderInviteRequestDto("bob", "CONTRIBUTOR"))))
                .andExpect(status().isConflict());
    }
}
