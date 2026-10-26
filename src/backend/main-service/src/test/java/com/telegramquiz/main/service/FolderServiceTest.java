package com.telegramquiz.main.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.telegramquiz.main.entity.FolderMember;
import com.telegramquiz.main.entity.FolderMemberStatus;
import com.telegramquiz.main.entity.FolderRole;
import com.telegramquiz.main.repository.FolderMemberRepository;
import com.telegramquiz.main.repository.FolderRepository;
import com.telegramquiz.main.repository.QuestionRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class FolderServiceTest {

    private static final Long FOLDER_ID = 10L;
    private static final Long INVITATION_ID = 100L;

    @Mock
    private FolderRepository folderRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private FolderMemberRepository memberRepository;

    private FolderService service;

    @BeforeEach
    void setUp() {
        service = new FolderService(folderRepository, questionRepository, memberRepository);
    }

    @Test
    @DisplayName("folders-sharing / Accepting and declining invitations / Declining accepted invitation fails")
    void declineAcceptedInvitationFails() {
        FolderMember acceptedMember = FolderMember.builder()
                .id(INVITATION_ID)
                .folderId(FOLDER_ID)
                .username("bob")
                .role(FolderRole.CONTRIBUTOR)
                .status(FolderMemberStatus.ACCEPTED)
                .invitedBy("alice")
                .build();

        when(memberRepository.findById(INVITATION_ID)).thenReturn(Optional.of(acceptedMember));

        assertThatThrownBy(() -> service.declineInvitation(INVITATION_ID, "bob"))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);

        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("folders-sharing / Accepting and declining invitations / Declining pending invitation succeeds")
    void declinePendingInvitationSucceeds() {
        FolderMember pendingMember = FolderMember.builder()
                .id(INVITATION_ID)
                .folderId(FOLDER_ID)
                .username("bob")
                .role(FolderRole.CONTRIBUTOR)
                .status(FolderMemberStatus.PENDING)
                .invitedBy("alice")
                .build();

        when(memberRepository.findById(INVITATION_ID)).thenReturn(Optional.of(pendingMember));
        when(memberRepository.save(any(FolderMember.class))).thenReturn(pendingMember);

        service.declineInvitation(INVITATION_ID, "bob");

        verify(memberRepository).save(pendingMember);
    }

    @Test
    @DisplayName("folders-sharing / Accepting and declining invitations / Declining already declined invitation fails")
    void declineAlreadyDeclinedInvitationFails() {
        FolderMember declinedMember = FolderMember.builder()
                .id(INVITATION_ID)
                .folderId(FOLDER_ID)
                .username("bob")
                .role(FolderRole.CONTRIBUTOR)
                .status(FolderMemberStatus.DECLINED)
                .invitedBy("alice")
                .build();

        when(memberRepository.findById(INVITATION_ID)).thenReturn(Optional.of(declinedMember));

        assertThatThrownBy(() -> service.declineInvitation(INVITATION_ID, "bob"))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);

        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("folders-sharing / Accepting and declining invitations / Declining someone else's invitation fails")
    void declineOthersInvitationFails() {
        FolderMember member = FolderMember.builder()
                .id(INVITATION_ID)
                .folderId(FOLDER_ID)
                .username("bob")
                .role(FolderRole.CONTRIBUTOR)
                .status(FolderMemberStatus.PENDING)
                .invitedBy("alice")
                .build();

        when(memberRepository.findById(INVITATION_ID)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> service.declineInvitation(INVITATION_ID, "eve"))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN);

        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("folders-sharing / Accepting and declining invitations / Declining non-existent invitation fails")
    void declineNonExistentInvitationFails() {
        when(memberRepository.findById(INVITATION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.declineInvitation(INVITATION_ID, "bob"))
                .isInstanceOf(EntityNotFoundException.class);

        verify(memberRepository, never()).save(any());
    }
}
