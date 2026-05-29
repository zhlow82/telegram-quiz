package com.telegramquiz.main.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.telegramquiz.main.dto.QuestionRequestDto;
import com.telegramquiz.main.dto.QuestionResponseDto;
import com.telegramquiz.main.entity.Question;
import com.telegramquiz.main.repository.QuestionRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    public List<QuestionResponseDto> findAll() {
        return questionRepository.findAllByOrderByOrderIndexAsc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public QuestionResponseDto findById(Long id) {
        return toDto(getOrThrow(id));
    }

    @Transactional
    public QuestionResponseDto create(QuestionRequestDto dto) {
        Integer orderIdx = dto.orderIndex();
        int nextOrder = orderIdx != null
                ? orderIdx
                : (int) questionRepository.count();

        Question question = Question.builder()
                .questionText(dto.questionText())
                .orderIndex(nextOrder)
                .intro(dto.intro())
                .introBlue(Boolean.TRUE.equals(dto.introBlue()))
                .questionImagePaths(dto.questionImagePaths() != null ? dto.questionImagePaths() : new ArrayList<>())
                .options(dto.options() != null ? dto.options() : new ArrayList<>())
                .answer(dto.answer())
                .expectPhoto(Boolean.TRUE.equals(dto.expectPhoto()))
                .isBriefing(Boolean.TRUE.equals(dto.isBriefing()))
                .hintText(dto.hintText())
                .hintImagePaths(dto.hintImagePaths() != null ? dto.hintImagePaths() : new ArrayList<>())
                .explanationTexts(dto.explanationTexts() != null ? dto.explanationTexts() : new ArrayList<>())
                .explanationImagePaths(dto.explanationImagePaths() != null ? dto.explanationImagePaths() : new ArrayList<>())
                .build();

        return toDto(questionRepository.save(question));
    }

    @Transactional
    public QuestionResponseDto update(Long id, QuestionRequestDto dto) {
        Question question = getOrThrow(id);
        question.setQuestionText(dto.questionText());
        if (dto.orderIndex() != null) question.setOrderIndex(dto.orderIndex());
        question.setIntro(dto.intro());
        question.setIntroBlue(Boolean.TRUE.equals(dto.introBlue()));
        question.setQuestionImagePaths(dto.questionImagePaths() != null ? dto.questionImagePaths() : new ArrayList<>());
        question.setOptions(dto.options() != null ? dto.options() : new ArrayList<>());
        question.setAnswer(dto.answer());
        question.setExpectPhoto(Boolean.TRUE.equals(dto.expectPhoto()));
        question.setBriefing(Boolean.TRUE.equals(dto.isBriefing()));
        question.setHintText(dto.hintText());
        question.setHintImagePaths(dto.hintImagePaths() != null ? dto.hintImagePaths() : new ArrayList<>());
        question.setExplanationTexts(dto.explanationTexts() != null ? dto.explanationTexts() : new ArrayList<>());
        question.setExplanationImagePaths(dto.explanationImagePaths() != null ? dto.explanationImagePaths() : new ArrayList<>());
        return toDto(questionRepository.save(question));
    }

    @Transactional
    public void delete(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new EntityNotFoundException("Question not found: " + id);
        }
        questionRepository.deleteById(id);
    }

    @Transactional
    public void reorder(List<Long> orderedIds) {
        List<Question> questions = questionRepository.findAllById(orderedIds);
        Map<Long, Question> byId = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));
        for (int i = 0; i < orderedIds.size(); i++) {
            Question q = byId.get(orderedIds.get(i));
            if (q != null) q.setOrderIndex(i);
        }
        questionRepository.saveAll(questions);
    }

    private Question getOrThrow(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Question not found: " + id));
    }

    private QuestionResponseDto toDto(Question q) {
        return new QuestionResponseDto(
                q.getId(),
                q.getOrderIndex(),
                q.getQuestionText(),
                q.getIntro(),
                q.isIntroBlue(),
                q.getQuestionImagePaths(),
                q.getOptions(),
                q.getAnswer(),
                q.isExpectPhoto(),
                q.isBriefing(),
                q.getHintText(),
                q.getHintImagePaths(),
                q.getExplanationTexts(),
                q.getExplanationImagePaths(),
                q.getCreatedAt(),
                q.getUpdatedAt()
        );
    }
}
