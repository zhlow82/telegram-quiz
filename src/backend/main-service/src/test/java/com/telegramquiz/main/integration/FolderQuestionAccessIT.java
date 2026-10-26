package com.telegramquiz.main.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import com.telegramquiz.main.entity.Question;
import com.telegramquiz.main.repository.FolderMemberRepository;
import com.telegramquiz.main.repository.FolderRepository;
import com.telegramquiz.main.repository.QuestionRepository;

@DisplayName("folders-sharing / Folder membership grants question access")
class FolderQuestionAccessIT extends FolderIntegrationTestBase {

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private FolderMemberRepository memberRepository;

    @Autowired
    private QuestionRepository questionRepository;

    private Folder folder;
    private Question question;

    @BeforeEach
    void setUp() {
        questionRepository.deleteAll();
        memberRepository.deleteAll();
        folderRepository.deleteAll();

        folder = Folder.builder()
                .name("Team Folder")
                .createdBy("alice")
                .sortOrder(0)
                .build();
        folderRepository.save(folder);

        question = Question.builder()
                .folderId(folder.getId())
                .createdBy("alice")
                .orderIndex(0)
                .questionBlocks(List.of())
                .options(List.of("A", "B"))
                .answer("A")
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
    }

    @Test
    @DisplayName("Co-owner sees folder questions")
    void coOwnerSeesFolderQuestions() throws Exception {
        FolderMember coOwner = FolderMember.builder()
                .folderId(folder.getId())
                .username("bob")
                .role(FolderRole.CO_OWNER)
                .status(FolderMemberStatus.ACCEPTED)
                .invitedBy("alice")
                .build();
        memberRepository.save(coOwner);

        mockMvc.perform(get("/api/questions").with(asUser("bob")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].folderId").value(folder.getId()));
    }

    @Test
    @DisplayName("Membership revoked")
    void removedMemberCannotSeeQuestions() throws Exception {
        FolderMember coOwner = FolderMember.builder()
                .folderId(folder.getId())
                .username("bob")
                .role(FolderRole.CO_OWNER)
                .status(FolderMemberStatus.ACCEPTED)
                .invitedBy("alice")
                .build();
        memberRepository.save(coOwner);

        mockMvc.perform(delete("/api/folders/" + folder.getId() + "/members/bob").with(asUser("bob")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/questions/" + question.getId()).with(asUser("bob")))
                .andExpect(status().isNotFound());
    }
}
