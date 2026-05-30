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
                .questionBlocks(dto.questionBlocks() != null ? dto.questionBlocks() : new ArrayList<>())
                .orderIndex(nextOrder)
                .options(dto.options() != null ? dto.options() : new ArrayList<>())
                .answer(dto.answer())
                .expectPhoto(Boolean.TRUE.equals(dto.expectPhoto()))
                .isBriefing(Boolean.TRUE.equals(dto.isBriefing()))
                .hintBlocks(dto.hintBlocks() != null ? dto.hintBlocks() : new ArrayList<>())
                .explanationBlocks(dto.explanationBlocks() != null ? dto.explanationBlocks() : new ArrayList<>())
                .mark(dto.mark())
                .build();

        return toDto(questionRepository.save(question));
    }

    @Transactional
    public QuestionResponseDto update(Long id, QuestionRequestDto dto) {
        Question question = getOrThrow(id);
        question.setQuestionBlocks(dto.questionBlocks() != null ? dto.questionBlocks() : new ArrayList<>());
        if (dto.orderIndex() != null) question.setOrderIndex(dto.orderIndex());
        question.setOptions(dto.options() != null ? dto.options() : new ArrayList<>());
        question.setAnswer(dto.answer());
        question.setExpectPhoto(Boolean.TRUE.equals(dto.expectPhoto()));
        question.setBriefing(Boolean.TRUE.equals(dto.isBriefing()));
        question.setHintBlocks(dto.hintBlocks() != null ? dto.hintBlocks() : new ArrayList<>());
        question.setExplanationBlocks(dto.explanationBlocks() != null ? dto.explanationBlocks() : new ArrayList<>());
        question.setMark(dto.mark());
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

    QuestionResponseDto toDto(Question q) {
        return new QuestionResponseDto(
                q.getId(),
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
                q.getUpdatedAt()
        );
    }
}
