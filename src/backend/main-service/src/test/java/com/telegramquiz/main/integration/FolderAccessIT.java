package com.telegramquiz.main.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telegramquiz.main.dto.FolderInviteRequestDto;
import com.telegramquiz.main.dto.FolderRequestDto;
import com.telegramquiz.main.dto.FolderRoleUpdateDto;
import com.telegramquiz.main.entity.Folder;
import com.telegramquiz.main.entity.FolderMember;
import com.telegramquiz.main.entity.FolderMemberStatus;
import com.telegramquiz.main.entity.FolderRole;
import com.telegramquiz.main.repository.FolderMemberRepository;
import com.telegramquiz.main.repository.FolderRepository;

@DisplayName("folders-sharing / Folder roles and access levels")
class FolderAccessIT extends FolderIntegrationTestBase {

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private FolderMemberRepository memberRepository;

    private Folder folder;
    private FolderMember owner;
    private FolderMember coOwner;
    private FolderMember contributor;

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

        coOwner = FolderMember.builder()
                .folderId(folder.getId())
                .username("bob")
                .role(FolderRole.CO_OWNER)
                .status(FolderMemberStatus.ACCEPTED)
                .invitedBy("alice")
                .build();
        memberRepository.save(coOwner);

        contributor = FolderMember.builder()
                .folderId(folder.getId())
                .username("carol")
                .role(FolderRole.CONTRIBUTOR)
                .status(FolderMemberStatus.ACCEPTED)
                .invitedBy("alice")
                .build();
        memberRepository.save(contributor);
    }

    @Test
    @DisplayName("Contributor permissions")
    void contributorCannotRenameFolder() throws Exception {
        mockMvc.perform(patch("/api/folders/" + folder.getId())
                        .with(asUser("carol"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new FolderRequestDto("New Name"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Contributor permissions")
    void contributorCannotManageMembers() throws Exception {
        mockMvc.perform(post("/api/folders/" + folder.getId() + "/members")
                        .with(asUser("carol"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new FolderInviteRequestDto("dave", "CONTRIBUTOR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Co-owner permissions")
    void coOwnerCanInviteMembers() throws Exception {
        mockMvc.perform(post("/api/folders/" + folder.getId() + "/members")
                        .with(asUser("bob"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new FolderInviteRequestDto("dave", "CONTRIBUTOR"))))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Co-owner permissions")
    void coOwnerCanRemoveMembers() throws Exception {
        mockMvc.perform(delete("/api/folders/" + folder.getId() + "/members/carol")
                        .with(asUser("bob")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Pending invitation holder reads content")
    void pendingInvitationHolderCannotReadContent() throws Exception {
        FolderMember pending = FolderMember.builder()
                .folderId(folder.getId())
                .username("dave")
                .role(FolderRole.CONTRIBUTOR)
                .status(FolderMemberStatus.PENDING)
                .invitedBy("alice")
                .build();
        memberRepository.save(pending);

        mockMvc.perform(get("/api/folders/" + folder.getId() + "/members")
                        .with(asUser("dave")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("No access")
    void noRelationshipGets404() throws Exception {
        mockMvc.perform(get("/api/folders/" + folder.getId() + "/members")
                        .with(asUser("eve")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Insufficient role")
    void contributorOwnerActionGets403() throws Exception {
        mockMvc.perform(delete("/api/folders/" + folder.getId())
                        .with(asUser("carol")))
                .andExpect(status().isForbidden());
    }
}
