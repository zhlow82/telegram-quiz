package com.telegramquiz.main.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.telegramquiz.main.dto.QuestionRequestDto;
import com.telegramquiz.main.dto.QuestionResponseDto;
import com.telegramquiz.main.entity.FolderAccessLevel;
import com.telegramquiz.main.entity.FolderMemberStatus;
import com.telegramquiz.main.entity.Question;
import com.telegramquiz.main.repository.QuestionRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final FolderService folderService;

    public List<QuestionResponseDto> findAll(String username) {
        return questionRepository.findAllAccessible(username, FolderMemberStatus.ACCEPTED)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public QuestionResponseDto findById(Long id, String username) {
        return toDto(getOrThrowForView(id, username));
    }

    @Transactional
    public QuestionResponseDto create(QuestionRequestDto dto, String username) {
        // Verify folder access if folderId is provided
        if (dto.folderId() != null) {
            FolderAccessLevel access = folderService.getAccessLevel(dto.folderId(), username);
            if (access == FolderAccessLevel.NONE) {
                throw new EntityNotFoundException("Folder not found: " + dto.folderId());
            }
        }

        Integer orderIdx = dto.orderIndex();
        int nextOrder = orderIdx != null
                ? orderIdx
                : (int) questionRepository.countByCreatedBy(username);

        Question question = Question.builder()
                .createdBy(username)
                .questionBlocks(dto.questionBlocks() != null ? dto.questionBlocks() : new ArrayList<>())
                .orderIndex(nextOrder)
                .options(dto.options() != null ? dto.options() : new ArrayList<>())
                .answer(dto.answer())
                .expectPhoto(Boolean.TRUE.equals(dto.expectPhoto()))
                .isBriefing(Boolean.TRUE.equals(dto.isBriefing()))
                .hintBlocks(dto.hintBlocks() != null ? dto.hintBlocks() : new ArrayList<>())
                .explanationBlocks(dto.explanationBlocks() != null ? dto.explanationBlocks() : new ArrayList<>())
                .mark(dto.mark())
                .folderId(dto.folderId())
                .build();

        return toDto(questionRepository.save(question));
    }

    @Transactional
    public QuestionResponseDto update(Long id, QuestionRequestDto dto, String username) {
        Question question = getOrThrowForView(id, username);
        question.setQuestionBlocks(dto.questionBlocks() != null ? dto.questionBlocks() : new ArrayList<>());
        if (dto.orderIndex() != null) question.setOrderIndex(dto.orderIndex());
        question.setOptions(dto.options() != null ? dto.options() : new ArrayList<>());
        question.setAnswer(dto.answer());
        question.setExpectPhoto(Boolean.TRUE.equals(dto.expectPhoto()));
        question.setBriefing(Boolean.TRUE.equals(dto.isBriefing()));
        question.setHintBlocks(dto.hintBlocks() != null ? dto.hintBlocks() : new ArrayList<>());
        question.setExplanationBlocks(dto.explanationBlocks() != null ? dto.explanationBlocks() : new ArrayList<>());
        question.setMark(dto.mark());
        question.setFolderId(dto.folderId());
        return toDto(questionRepository.save(question));
    }

    @Transactional
    public QuestionResponseDto assignFolder(Long id, Long folderId, String username) {
        // Only the question creator can move it
        Question question = questionRepository.findByIdAndCreatedBy(id, username)
                .orElseThrow(() -> new EntityNotFoundException("Question not found: " + id));
        if (folderId != null) {
            FolderAccessLevel access = folderService.getAccessLevel(folderId, username);
            if (access == FolderAccessLevel.NONE) {
                throw new EntityNotFoundException("Folder not found: " + folderId);
            }
        }
        question.setFolderId(folderId);
        return toDto(questionRepository.save(question));
    }

    @Transactional
    public void delete(Long id, String username) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Question not found: " + id));

        if (!canAccess(q, username)) {
            throw new EntityNotFoundException("Question not found: " + id);
        }

        // Check delete permission: own questions always deletable; others' questions require OWNER or CO_OWNER
        if (!q.getCreatedBy().equals(username)) {
            if (q.getFolderId() == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete this question");
            }
            FolderAccessLevel access = folderService.getAccessLevel(q.getFolderId(), username);
            if (access != FolderAccessLevel.OWNER && access != FolderAccessLevel.CO_OWNER) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete this question");
            }
        }

        questionRepository.deleteById(id);
    }

    @Transactional
    public void reorder(List<Long> orderedIds, String username) {
        List<Question> questions = questionRepository.findAllAccessibleByIds(
                orderedIds, username, FolderMemberStatus.ACCEPTED);
        Map<Long, Question> byId = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));
        for (int i = 0; i < orderedIds.size(); i++) {
            Question q = byId.get(orderedIds.get(i));
            if (q != null) q.setOrderIndex(i);
        }
        questionRepository.saveAll(questions);
    }

    private Question getOrThrowForView(Long id, String username) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Question not found: " + id));
        if (!canAccess(q, username)) {
            throw new EntityNotFoundException("Question not found: " + id);
        }
        return q;
    }

    private boolean canAccess(Question q, String username) {
        if (q.getCreatedBy().equals(username)) return true;
        if (q.getFolderId() == null) return false;
        return folderService.getAccessLevel(q.getFolderId(), username) != FolderAccessLevel.NONE;
    }

    QuestionResponseDto toDto(Question q) {
        return new QuestionResponseDto(
                q.getId(),
                q.getCreatedBy(),
                q.getOrderIndex(),
                q.getQuestionBlocks(),
                q.getOptions(),
                q.getAnswer(),
                q.isExpectPhoto(),
                q.isBriefing(),
                q.getHintBlocks(),
                q.getExplanationBlocks(),
                q.getMark(),
                q.getCreatedAt(),
                q.getUpdatedAt(),
                q.getFolderId()
        );
    }
}
