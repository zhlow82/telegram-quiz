package com.telegramquiz.main.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.telegramquiz.main.entity.Folder;
import com.telegramquiz.main.entity.FolderAccessLevel;
import com.telegramquiz.main.entity.FolderMember;
import com.telegramquiz.main.entity.FolderMemberStatus;
import com.telegramquiz.main.entity.FolderRole;
import com.telegramquiz.main.repository.FolderMemberRepository;
import com.telegramquiz.main.repository.FolderRepository;
import com.telegramquiz.main.repository.QuestionRepository;

@ExtendWith(MockitoExtension.class)
class FolderAccessTest {

    private static final long FOLDER_ID = 10L;

    @Mock
    private FolderRepository folderRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private FolderMemberRepository memberRepository;

    private FolderService service;
    private Folder folder;

    @BeforeEach
    void setUp() {
        service = new FolderService(folderRepository, questionRepository, memberRepository);
        folder = Folder.builder()
                .id(FOLDER_ID)
                .name("Team")
                .createdBy("alice")
                .sortOrder(0)
                .build();
    }

    @Test
    @DisplayName("folders-sharing / Folder roles and access levels / Owner has OWNER access")
    void ownerHasOwnerAccess() {
        when(folderRepository.findById(FOLDER_ID)).thenReturn(Optional.of(folder));

        assertThat(service.getAccessLevel(FOLDER_ID, "alice")).isEqualTo(FolderAccessLevel.OWNER);
    }

    @Test
    @DisplayName("folders-sharing / Folder roles and access levels / Accepted co-owner has CO_OWNER access")
    void acceptedCoOwnerHasCoOwnerAccess() {
        when(folderRepository.findById(FOLDER_ID)).thenReturn(Optional.of(folder));
        when(memberRepository.findByFolderIdAndUsername(FOLDER_ID, "bob"))
                .thenReturn(Optional.of(member("bob", FolderRole.CO_OWNER, FolderMemberStatus.ACCEPTED)));

        assertThat(service.getAccessLevel(FOLDER_ID, "bob")).isEqualTo(FolderAccessLevel.CO_OWNER);
    }

    @Test
    @DisplayName("folders-sharing / Folder roles and access levels / Accepted contributor has CONTRIBUTOR access")
    void acceptedContributorHasContributorAccess() {
        when(folderRepository.findById(FOLDER_ID)).thenReturn(Optional.of(folder));
        when(memberRepository.findByFolderIdAndUsername(FOLDER_ID, "carol"))
                .thenReturn(Optional.of(member("carol", FolderRole.CONTRIBUTOR, FolderMemberStatus.ACCEPTED)));

        assertThat(service.getAccessLevel(FOLDER_ID, "carol")).isEqualTo(FolderAccessLevel.CONTRIBUTOR);
    }

    @Test
    @DisplayName("folders-sharing / Folder roles and access levels / Pending membership grants no access")
    void pendingMembershipGrantsNoAccess() {
        when(folderRepository.findById(FOLDER_ID)).thenReturn(Optional.of(folder));
        when(memberRepository.findByFolderIdAndUsername(FOLDER_ID, "dave"))
                .thenReturn(Optional.of(member("dave", FolderRole.CONTRIBUTOR, FolderMemberStatus.PENDING)));

        assertThat(service.getAccessLevel(FOLDER_ID, "dave")).isEqualTo(FolderAccessLevel.NONE);
    }

    @Test
    @DisplayName("folders-sharing / Access error semantics / Missing folder is NONE")
    void missingFolderIsNone() {
        when(folderRepository.findById(FOLDER_ID)).thenReturn(Optional.empty());

        assertThat(service.getAccessLevel(FOLDER_ID, "alice")).isEqualTo(FolderAccessLevel.NONE);
    }

    private FolderMember member(String username, FolderRole role, FolderMemberStatus status) {
        return FolderMember.builder()
                .folderId(FOLDER_ID)
                .username(username)
                .role(role)
                .status(status)
                .invitedBy("alice")
                .build();
    }
}
