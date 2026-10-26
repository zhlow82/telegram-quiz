package com.telegramquiz.main.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.telegramquiz.main.entity.Folder;
import com.telegramquiz.main.entity.FolderMember;
import com.telegramquiz.main.entity.FolderMemberStatus;
import com.telegramquiz.main.entity.FolderRole;
import com.telegramquiz.main.repository.FolderMemberRepository;
import com.telegramquiz.main.repository.FolderRepository;

@DisplayName("folders-sharing / Folder listing")
class FolderListingIT extends FolderIntegrationTestBase {

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private FolderMemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
        folderRepository.deleteAll();
    }

    @Test
    @DisplayName("Owner listing")
    void ownerSeesOwnFolders() throws Exception {
        Folder folder = Folder.builder()
                .name("My Folder")
                .createdBy("alice")
                .sortOrder(0)
                .build();
        folderRepository.save(folder);

        mockMvc.perform(get("/api/folders").with(asUser("alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("My Folder"))
                .andExpect(jsonPath("$[0].role").value("OWNER"))
                .andExpect(jsonPath("$[0].createdBy").value("alice"));
    }

    @Test
    @DisplayName("Accepted share appears")
    void acceptedCoOwnerSeesSharedFolder() throws Exception {
        Folder folder = Folder.builder()
                .name("Shared Folder")
                .createdBy("alice")
                .sortOrder(0)
                .build();
        folderRepository.save(folder);

        FolderMember member = FolderMember.builder()
                .folderId(folder.getId())
                .username("bob")
                .role(FolderRole.CO_OWNER)
                .status(FolderMemberStatus.ACCEPTED)
                .invitedBy("alice")
                .build();
        memberRepository.save(member);

        mockMvc.perform(get("/api/folders").with(asUser("bob")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Shared Folder"))
                .andExpect(jsonPath("$[0].role").value("CO_OWNER"))
                .andExpect(jsonPath("$[0].createdBy").value("alice"));
    }

    @Test
    @DisplayName("Pending share is hidden")
    void pendingInvitationHiddenFromListing() throws Exception {
        Folder folder = Folder.builder()
                .name("Pending Folder")
                .createdBy("alice")
                .sortOrder(0)
                .build();
        folderRepository.save(folder);

        FolderMember member = FolderMember.builder()
                .folderId(folder.getId())
                .username("carol")
                .role(FolderRole.CONTRIBUTOR)
                .status(FolderMemberStatus.PENDING)
                .invitedBy("alice")
                .build();
        memberRepository.save(member);

        mockMvc.perform(get("/api/folders").with(asUser("carol")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
