package com.telegramquiz.main.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.telegramquiz.main.entity.Folder;
import com.telegramquiz.main.entity.FolderMember;
import com.telegramquiz.main.entity.FolderMemberStatus;
import com.telegramquiz.main.entity.FolderRole;
import com.telegramquiz.main.entity.Question;
import com.telegramquiz.main.model.ContentBlock;
import com.telegramquiz.main.support.TestContainers;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class QuestionAccessIT {

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainers.register(registry);
    }

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private FolderMemberRepository memberRepository;

    @Autowired
    private EntityManager entityManager;

    private Question sharedQuestion;

    @BeforeEach
    void seed() {
        questionRepository.save(Question.builder()
                .createdBy("alice")
                .orderIndex(0)
                .questionBlocks(List.of(new ContentBlock("text", "private")))
                .options(List.of())
                .build());

        Folder folder = folderRepository.save(Folder.builder()
                .name("Team")
                .createdBy("alice")
                .sortOrder(0)
                .build());

        sharedQuestion = questionRepository.save(Question.builder()
                .createdBy("alice")
                .orderIndex(1)
                .questionBlocks(List.of(new ContentBlock("text", "shared")))
                .options(List.of())
                .folderId(folder.getId())
                .build());

        memberRepository.save(FolderMember.builder()
                .folderId(folder.getId())
                .username("bob")
                .role(FolderRole.CONTRIBUTOR)
                .status(FolderMemberStatus.ACCEPTED)
                .invitedBy("alice")
                .build());

        memberRepository.save(FolderMember.builder()
                .folderId(folder.getId())
                .username("carol")
                .role(FolderRole.CONTRIBUTOR)
                .status(FolderMemberStatus.PENDING)
                .invitedBy("alice")
                .build());
    }

    @Test
    @DisplayName("question-bank / Question visibility / Owner sees own questions")
    void ownerSeesOwnQuestions() {
        List<Question> visible = questionRepository.findAllAccessible("alice", FolderMemberStatus.ACCEPTED);

        assertThat(visible).hasSize(2);
    }

    @Test
    @DisplayName("question-bank / Question visibility / Accepted folder member sees the shared question")
    void acceptedMemberSeesSharedQuestion() {
        List<Question> visible = questionRepository.findAllAccessible("bob", FolderMemberStatus.ACCEPTED);

        assertThat(visible).extracting(Question::getId).containsExactly(sharedQuestion.getId());
    }

    @Test
    @DisplayName("question-bank / Question visibility / Pending membership sees nothing")
    void pendingMemberSeesNothing() {
        List<Question> visible = questionRepository.findAllAccessible("carol", FolderMemberStatus.ACCEPTED);

        assertThat(visible).isEmpty();
    }

    @Test
    @DisplayName("question-bank / Question visibility / Unrelated user sees nothing")
    void unrelatedUserSeesNothing() {
        List<Question> visible = questionRepository.findAllAccessible("dave", FolderMemberStatus.ACCEPTED);

        assertThat(visible).isEmpty();
    }

    @Test
    @DisplayName("question-bank / Content blocks / JSONB blocks round-trip")
    void jsonbBlocksRoundTrip() {
        Question question = questionRepository.save(Question.builder()
                .createdBy("alice")
                .orderIndex(5)
                .questionBlocks(List.of(
                        new ContentBlock("text", "hello"),
                        new ContentBlock("image", "42")))
                .options(List.of())
                .build());

        entityManager.flush();
        entityManager.clear();

        Question reloaded = questionRepository.findById(question.getId()).orElseThrow();

        assertThat(reloaded.getQuestionBlocks()).containsExactly(
                new ContentBlock("text", "hello"),
                new ContentBlock("image", "42"));
    }
}
