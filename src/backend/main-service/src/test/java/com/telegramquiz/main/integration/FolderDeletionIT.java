package com.telegramquiz.main.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import com.telegramquiz.main.entity.Question;
import com.telegramquiz.main.model.ContentBlock;
import com.telegramquiz.main.repository.FolderMemberRepository;
import com.telegramquiz.main.repository.FolderRepository;
import com.telegramquiz.main.repository.QuestionRepository;

@DisplayName("folders-sharing / Folder rename and deletion")
class FolderDeletionIT extends FolderIntegrationTestBase {

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private FolderMemberRepository memberRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @BeforeEach
    void setUp() {
        questionRepository.deleteAll();
        memberRepository.deleteAll();
        folderRepository.deleteAll();
    }

    @Test
    @DisplayName("Deleting a folder with questions")
    void deleteFolderWithQuestionsKeepsQuestions() throws Exception {
        Folder folder = Folder.builder()
                .name("Folder with Questions")
                .createdBy("alice")
                .sortOrder(0)
                .build();
        folderRepository.save(folder);

        Question question = Question.builder()
                .folderId(folder.getId())
                .createdBy("alice")
                .orderIndex(0)
                .questionBlocks(List.of())
                .options(List.of("A", "B", "C"))
                .answer("B")
                .expectPhoto(false)
                .isBriefing(false)
                .expectsTextInput(false)
                .showBriefingPrimaryButton(true)
                .showBriefingSecondaryButton(true)
                .showAfterAnswerButton(true)
                .hintBlocks(List.of())
                .explanationBlocks(List.of())
                .build();
        questionRepository.save(question);

        mockMvc.perform(delete("/api/folders/" + folder.getId()).with(asUser("alice")))
                .andExpect(status().isNoContent());

        Question reloadedQuestion = questionRepository.findById(question.getId()).orElseThrow();
        assert reloadedQuestion.getFolderId() == null;
    }

    @Test
    @DisplayName("Deleting a folder removes shares")
    void deleteFolderRemovesShares() throws Exception {
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

        mockMvc.perform(delete("/api/folders/" + folder.getId()).with(asUser("alice")))
                .andExpect(status().isNoContent());

        List<FolderMember> remainingMembers = memberRepository.findAllByFolderId(folder.getId());
        assert remainingMembers.isEmpty();
    }
}
