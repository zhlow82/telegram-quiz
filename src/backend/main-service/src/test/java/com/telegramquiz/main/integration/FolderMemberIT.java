package com.telegramquiz.main.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telegramquiz.main.dto.FolderInviteRequestDto;
import com.telegramquiz.main.dto.FolderRoleUpdateDto;
import com.telegramquiz.main.entity.Folder;
import com.telegramquiz.main.entity.FolderMember;
import com.telegramquiz.main.entity.FolderMemberStatus;
import com.telegramquiz.main.entity.FolderRole;
import com.telegramquiz.main.repository.FolderMemberRepository;
import com.telegramquiz.main.repository.FolderRepository;

@DisplayName("folders-sharing / Member management")
class FolderMemberIT extends FolderIntegrationTestBase {

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private FolderMemberRepository memberRepository;

    private Folder folder;
    private FolderMember owner;
    private FolderMember coOwner;
    private FolderMember contributor;
    private FolderMember pending;
    private FolderMember declined;

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

        pending = FolderMember.builder()
                .folderId(folder.getId())
                .username("dave")
                .role(FolderRole.CONTRIBUTOR)
                .status(FolderMemberStatus.PENDING)
                .invitedBy("alice")
                .build();
        memberRepository.save(pending);

        declined = FolderMember.builder()
                .folderId(folder.getId())
                .username("eve")
                .role(FolderRole.CONTRIBUTOR)
                .status(FolderMemberStatus.DECLINED)
                .invitedBy("alice")
                .build();
        memberRepository.save(declined);
    }

    @Test
    @DisplayName("Listing members")
    void listMembersIncludesPendingAndDeclined() throws Exception {
        mockMvc.perform(get("/api/folders/" + folder.getId() + "/members")
                        .with(asUser("carol")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    @DisplayName("Changing a role")
    void promoteContributorToCoOwner() throws Exception {
        mockMvc.perform(patch("/api/folders/" + folder.getId() + "/members/carol")
                        .with(asUser("bob"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new FolderRoleUpdateDto("CO_OWNER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("carol"))
                .andExpect(jsonPath("$.role").value("CO_OWNER"));
    }

    @Test
    @DisplayName("Removing a member")
    void removeMemberLosesAccess() throws Exception {
        mockMvc.perform(delete("/api/folders/" + folder.getId() + "/members/carol")
                        .with(asUser("bob")))
                .andExpect(status().isNoContent());

        List<FolderMember> remainingMembers = memberRepository.findAllByFolderId(folder.getId());
        assert remainingMembers.stream().noneMatch(m -> m.getUsername().equals("carol"));
    }
}
