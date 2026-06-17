package com.telegramquiz.main.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.telegramquiz.main.dto.ExportedQuestionDto;
import com.telegramquiz.main.dto.ExportRequestDto;
import com.telegramquiz.main.dto.ExportResponseDto;
import com.telegramquiz.main.dto.ImportQuestionDto;
import com.telegramquiz.main.dto.ImportRequestDto;
import com.telegramquiz.main.dto.ImportResultDto;
import com.telegramquiz.main.dto.QuestionRequestDto;
import com.telegramquiz.main.dto.QuestionResponseDto;
import com.telegramquiz.main.entity.FolderAccessLevel;
import com.telegramquiz.main.entity.FolderMemberStatus;
import com.telegramquiz.main.entity.Question;
import com.telegramquiz.main.model.ContentBlock;
import com.telegramquiz.main.repository.QuestionRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final FolderService folderService;
    private final ImageBlobService imageBlobService;

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
                : questionRepository.findMaxOrderIndexByCreatedBy(username).orElse(-1) + 1;

        Question question = Question.builder()
                .createdBy(username)
                .questionBlocks(dto.questionBlocks() != null ? dto.questionBlocks() : new ArrayList<>())
                .orderIndex(nextOrder)
                .options(dto.options() != null ? dto.options() : new ArrayList<>())
                .answer(dto.answer())
                .expectPhoto(Boolean.TRUE.equals(dto.expectPhoto()))
                .isBriefing(Boolean.TRUE.equals(dto.isBriefing()))
                .expectsTextInput(Boolean.TRUE.equals(dto.expectsTextInput()))
                .briefingPrimaryButtonText(normalizeBriefingButtonText(dto.briefingPrimaryButtonText(), "READY"))
                .showBriefingPrimaryButton(resolveBriefingButtonVisibility(dto.showBriefingPrimaryButton(), true))
                .briefingSecondaryButtonText(normalizeBriefingButtonText(dto.briefingSecondaryButtonText(), "Start Timer"))
                .showBriefingSecondaryButton(resolveBriefingButtonVisibility(dto.showBriefingSecondaryButton(), true))
                .afterAnswerButtonText(normalizeOptionalButtonText(dto.afterAnswerButtonText(), "Next Question"))
                .showAfterAnswerButton(resolveBriefingButtonVisibility(dto.showAfterAnswerButton(), true))
                .hintBlocks(dto.hintBlocks() != null ? dto.hintBlocks() : new ArrayList<>())
                .explanationBlocks(dto.explanationBlocks() != null ? dto.explanationBlocks() : new ArrayList<>())
                .mark(dto.mark())
                .folderId(dto.folderId())
                .updatedBy(username)
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
        question.setExpectsTextInput(Boolean.TRUE.equals(dto.expectsTextInput()));
        question.setBriefingPrimaryButtonText(normalizeBriefingButtonText(dto.briefingPrimaryButtonText(), "READY"));
        question.setShowBriefingPrimaryButton(resolveBriefingButtonVisibility(dto.showBriefingPrimaryButton(), true));
        question.setBriefingSecondaryButtonText(normalizeBriefingButtonText(dto.briefingSecondaryButtonText(), "Start Timer"));
        question.setShowBriefingSecondaryButton(resolveBriefingButtonVisibility(dto.showBriefingSecondaryButton(), true));
        question.setAfterAnswerButtonText(normalizeOptionalButtonText(dto.afterAnswerButtonText(), "Next Question"));
        question.setShowAfterAnswerButton(resolveBriefingButtonVisibility(dto.showAfterAnswerButton(), true));
        question.setHintBlocks(dto.hintBlocks() != null ? dto.hintBlocks() : new ArrayList<>());
        question.setExplanationBlocks(dto.explanationBlocks() != null ? dto.explanationBlocks() : new ArrayList<>());
        question.setMark(dto.mark());
        question.setFolderId(dto.folderId());
        question.setUpdatedBy(username);
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

    public ExportResponseDto exportQuestions(ExportRequestDto dto, String username) {
        List<Question> questions = questionRepository.findAllAccessibleByIds(
                dto.questionIds(), username, FolderMemberStatus.ACCEPTED);

        List<ExportedQuestionDto> exported = questions.stream()
                .map(q -> toExportedDto(q, dto.includeImages()))
                .toList();

        return new ExportResponseDto(
                "1.0",
                LocalDateTime.now().toString(),
                username,
                "telegram-quiz",
                exported
        );
    }

    @Transactional
    public ImportResultDto importQuestions(ImportRequestDto dto, String username) {
        List<String> errors = new ArrayList<>();
        int imported = 0;

        for (int i = 0; i < dto.questions().size(); i++) {
            ImportQuestionDto qDto = dto.questions().get(i);
            try {
                List<ContentBlock> questionBlocks = processImageBlocks(qDto.questionBlocks());
                List<ContentBlock> hintBlocks = processImageBlocks(qDto.hintBlocks());
                List<ContentBlock> explanationBlocks = processImageBlocks(qDto.explanationBlocks());

                int nextOrder = questionRepository.findMaxOrderIndexByCreatedBy(username).orElse(-1) + 1;

                Question question = Question.builder()
                        .createdBy(username)
                        .questionBlocks(questionBlocks != null ? questionBlocks : new ArrayList<>())
                        .orderIndex(nextOrder + i)
                        .options(qDto.options() != null ? qDto.options() : new ArrayList<>())
                        .answer(qDto.answer())
                        .expectPhoto(Boolean.TRUE.equals(qDto.expectPhoto()))
                        .isBriefing(Boolean.TRUE.equals(qDto.isBriefing()))
                        .expectsTextInput(Boolean.TRUE.equals(qDto.expectsTextInput()))
                        .briefingPrimaryButtonText(normalizeBriefingButtonText(qDto.briefingPrimaryButtonText(), "READY"))
                        .showBriefingPrimaryButton(resolveBriefingButtonVisibility(qDto.showBriefingPrimaryButton(), true))
                        .briefingSecondaryButtonText(normalizeBriefingButtonText(qDto.briefingSecondaryButtonText(), "Start Timer"))
                        .showBriefingSecondaryButton(resolveBriefingButtonVisibility(qDto.showBriefingSecondaryButton(), true))
                        .afterAnswerButtonText(normalizeOptionalButtonText(qDto.afterAnswerButtonText(), "Next Question"))
                        .showAfterAnswerButton(resolveBriefingButtonVisibility(qDto.showAfterAnswerButton(), true))
                        .hintBlocks(hintBlocks != null ? hintBlocks : new ArrayList<>())
                        .explanationBlocks(explanationBlocks != null ? explanationBlocks : new ArrayList<>())
                        .mark(qDto.mark())
                        .folderId(null)
                        .updatedBy(username)
                        .build();

                questionRepository.save(question);
                imported++;
            } catch (Exception e) {
                errors.add("Question " + (i + 1) + ": " + e.getMessage());
            }
        }

        return new ImportResultDto(imported, errors);
    }

    private ExportedQuestionDto toExportedDto(Question q, boolean includeImages) {
        return new ExportedQuestionDto(
                convertBlocks(q.getQuestionBlocks(), includeImages),
                q.getOptions(),
                q.getAnswer(),
                q.isExpectPhoto(),
                q.isBriefing(),
                q.isExpectsTextInput(),
                q.getBriefingPrimaryButtonText(),
                q.isShowBriefingPrimaryButton(),
                q.getBriefingSecondaryButtonText(),
                q.isShowBriefingSecondaryButton(),
                q.getAfterAnswerButtonText(),
                q.isShowAfterAnswerButton(),
                convertBlocks(q.getHintBlocks(), includeImages),
                convertBlocks(q.getExplanationBlocks(), includeImages),
                q.getMark()
        );
    }

    private List<ContentBlock> convertBlocks(List<ContentBlock> blocks, boolean includeImages) {
        if (blocks == null) return new ArrayList<>();
        return blocks.stream()
                .map(b -> {
                    if ("image".equals(b.type()) && includeImages) {
                        try {
                            Long blobId = Long.parseLong(b.content());
                            String dataUri = imageBlobService.toBase64DataUri(blobId);
                            return new ContentBlock(b.type(), dataUri);
                        } catch (Exception e) {
                            return b;
                        }
                    }
                    return b;
                })
                .toList();
    }

    private List<ContentBlock> processImageBlocks(List<ContentBlock> blocks) {
        if (blocks == null) return new ArrayList<>();
        return blocks.stream()
                .map(b -> {
                    if ("image".equals(b.type()) && b.content() != null && b.content().startsWith("data:")) {
                        Long newId = imageBlobService.storeFromBase64DataUri(b.content());
                        return new ContentBlock(b.type(), newId.toString());
                    }
                    return b;
                })
                .toList();
    }

    private Question getOrThrowForView(Long id, String username) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Question not found: " + id));
        if (!canAccess(q, username)) {
            throw new EntityNotFoundException("Question not found: " + id);
        }
        return q;
    }

    boolean canAccess(Question q, String username) {
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
                q.isExpectsTextInput(),
                q.getBriefingPrimaryButtonText(),
                q.isShowBriefingPrimaryButton(),
                q.getBriefingSecondaryButtonText(),
                q.isShowBriefingSecondaryButton(),
                q.getAfterAnswerButtonText(),
                q.isShowAfterAnswerButton(),
                q.getHintBlocks(),
                q.getExplanationBlocks(),
                q.getMark(),
                q.getCreatedAt(),
                q.getUpdatedAt(),
                q.getUpdatedBy(),
                q.getFolderId()
        );
    }

    private String normalizeBriefingButtonText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private boolean resolveBriefingButtonVisibility(Boolean value, boolean fallback) {
        return value != null ? value : fallback;
    }

    private String normalizeOptionalButtonText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}
