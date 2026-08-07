package com.telegramquiz.main.bot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.telegramquiz.main.entity.ImageBlob;
import com.telegramquiz.main.model.ContentBlock;
import com.telegramquiz.main.service.QuizSessionService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QuizBotSession extends TelegramLongPollingBot {

    private final QuizBotData quizData;
    private final QuizSessionService sessionService;
    private final com.telegramquiz.main.service.ImageBlobService imageBlobService;
    private final ScheduledExecutorService scheduler;
    private final ConcurrentHashMap<Long, ChatQuizState> sessions = new ConcurrentHashMap<>();

    private static final String BRIEFING_START_TIMER = "briefing:start-timer";
    private static final String BRIEFING_READY = "briefing:ready";
    private static final String AFTER_ANSWER_READY = "after-answer:ready";
    private static final String HINT_CALLBACK = "hint";
    private static final String HINT_BUTTON_TEXT = "💡 Hint";
    private static final Pattern TEXT_INPUT_PLACEHOLDER_PATTERN = Pattern.compile("\\{+\\s*player_input\\s*\\}+", Pattern.CASE_INSENSITIVE);

    public QuizBotSession(QuizBotData quizData, QuizSessionService sessionService,
                          com.telegramquiz.main.service.ImageBlobService imageBlobService) {
        super(quizData.botToken());
        this.quizData = quizData;
        this.sessionService = sessionService;
        this.imageBlobService = imageBlobService;
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "quiz-bot-" + quizData.quizId() + "-timer");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public String getBotUsername() {
        return quizData.botUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                Message message = update.getMessage();
                if (message.hasText()) {
                    String text = message.getText();
                    if ("/start".equals(text) || text.startsWith("/start ")) {
                        startOrRestartSession(message);
                    } else {
                        handleTextMessage(message);
                    }
                } else if (message.hasPhoto()) {
                    handlePhotoMessage(message);
                }
            } else if (update.hasCallbackQuery()) {
                handleCallback(update.getCallbackQuery());
            }
        } catch (Exception e) {
            log.error("Error handling Telegram update for quiz {}: {}", quizData.quizId(), e.getMessage(), e);
        }
    }

    private void startOrRestartSession(Message message) {
        Long chatId = message.getChatId();
        var from = message.getFrom();
        long telegramUserId = from != null ? from.getId() : chatId;
        String telegramUsername = from != null ? from.getUserName() : null;
        String telegramFirstName = from != null ? from.getFirstName() : "Unknown";

        ChatQuizState existing = sessions.get(chatId);
        if (existing != null) {
            existing.cancelTimeout();
            existing.cancelDeadline();
        }

        if (quizData.questions().isEmpty()) {
            sendText(chatId, "⚠️ This quiz has no questions yet.");
            return;
        }

        int scoredCount = (int) quizData.questions().stream()
                .filter(q -> !q.isBriefing() && !q.expectsTextInput())
                .count();

        ChatQuizState state = new ChatQuizState(chatId, scoredCount,
                telegramUserId, telegramUsername, telegramFirstName);
        sessions.put(chatId, state);

        if (quizData.totalTimeLimitSeconds() > 0) {
            state.deadline = LocalDateTime.now().plusSeconds(quizData.totalTimeLimitSeconds());
            startQuizDeadline(chatId, state);
        }

        try {
            Long sessionId = sessionService.recordStarted(quizData.quizId(), scoredCount,
                    telegramUserId, telegramUsername, telegramFirstName);
            state.sessionId = sessionId;
        } catch (Exception e) {
            log.warn("Could not record session start for quiz {}: {}", quizData.quizId(), e.getMessage());
        }

        sendText(chatId,
                "👋 Welcome to <b>" + escapeHtml(quizData.quizName()) + "</b>!\n\n" +
                "📝 Total questions: <b>" + scoredCount + "</b>\n" +
                (quizData.timePerQuestionSeconds() > 0
                    ? "⏱ <b>" + quizData.timePerQuestionSeconds() + "s</b> per question\n"
                    : "") +
                "✅ Pass score: <b>" + quizData.passScorePercent() + "%</b>\n\n" +
                "Let's go! 🚀");

        sendCurrentQuestion(chatId, state);
    }

    private void sendCurrentQuestion(Long chatId, ChatQuizState state) {
        if (state.questionIndex >= quizData.questions().size()) {
            endQuiz(chatId, state);
            return;
        }
        if (state.deadline != null && LocalDateTime.now().isAfter(state.deadline)) {
            endQuiz(chatId, state);
            return;
        }

        state.questionStartedAt = LocalDateTime.now();

        QuizBotQuestion q = quizData.questions().get(state.questionIndex);
        int imageBlockCount = q.questionBlocks() == null ? 0
            : (int) q.questionBlocks().stream().filter(b -> "image".equals(b.type())).count();
        log.info("Sending quiz {} question {} to chat {}: briefing={}, textInput={}, imageBlocks={}",
            quizData.quizId(), state.questionIndex + 1, chatId, q.isBriefing(), q.expectsTextInput(), imageBlockCount);
        sendImageBlocks(chatId, q.questionBlocks());

        if (q.expectsTextInput()) {
            state.waitingForTextInput = true;
            StringBuilder sb = new StringBuilder();
            sb.append("👥 <b>Team Input</b>\n\n");
            String qText = q.questionBlocks() != null && !q.questionBlocks().isEmpty()
                    ? q.questionBlocks().stream()
                        .filter(b -> "text".equals(b.type()))
                        .map(b -> renderTelegramHtml(b.content()))
                        .collect(Collectors.joining("\n\n"))
                    : "";
            sb.append(qText);
            sendText(chatId, sb.toString());
        } else if (q.isBriefing()) {
            state.waitingForTextInput = false;
            StringBuilder sb = new StringBuilder();
            sb.append("📋 <b>Briefing</b>\n\n");
            String qText = q.questionBlocks() != null && !q.questionBlocks().isEmpty()
                    ? q.questionBlocks().stream()
                        .filter(b -> "text".equals(b.type()))
                        .map(b -> renderTelegramHtml(b.content()))
                        .collect(Collectors.joining("\n\n"))
                    : "";
            sb.append(qText);

            sendMessageWithKeyboard(chatId, sb.toString(), buildBriefingKeyboard(q), result -> {
                if (result != null) state.lastMessageId = result.getMessageId();
            });
        } else {
            state.scoredQuestionNumber++;
            StringBuilder sb = new StringBuilder();
            sb.append("📝 <b>Q ").append(state.scoredQuestionNumber).append("/").append(state.totalQuestions).append("</b>\n\n");
            String qText = q.questionBlocks() != null && !q.questionBlocks().isEmpty()
                    ? q.questionBlocks().stream()
                        .filter(b -> "text".equals(b.type()))
                        .map(b -> renderTelegramHtml(b.content()))
                        .collect(Collectors.joining("\n\n"))
                    : "";
            sb.append(qText);

            if (q.expectPhoto()) {
                sendText(chatId, sb.toString());
            } else {
                List<String> options = q.options();
                boolean hasHint = q.hintBlocks() != null && !q.hintBlocks().isEmpty();
                sendMessageWithKeyboard(chatId, sb.toString(), buildAnswerKeyboard(options, hasHint), result -> {
                    if (result != null) state.lastMessageId = result.getMessageId();
                });
            }
            if (quizData.timePerQuestionSeconds() > 0) {
                startQuestionTimeout(chatId, state);
            }
        }
    }

    private void handleTextMessage(Message message) {
        Long chatId = message.getChatId();
        ChatQuizState state = sessions.get(chatId);
        if (state == null) {
            return;
        }
        if (state.questionIndex >= quizData.questions().size()) {
            return;
        }

        QuizBotQuestion q = quizData.questions().get(state.questionIndex);
        String text = message.getText() != null ? message.getText().trim() : "";

        if (!q.expectsTextInput() && !q.isBriefing()) {
            return;
        }

        if (!state.waitingForTextInput || !q.expectsTextInput()) {
            return;
        }
        if (text.isBlank()) {
            sendText(chatId, "Please enter a valid response.");
            return;
        }

        state.waitingForTextInput = false;
        sessionService.recordTeamName(quizData.quizId(), state.telegramUserId, text);

        StringBuilder result = new StringBuilder();
        result.append("✅ Team name saved: <b>").append(escapeHtml(text)).append("</b>");
        appendExplanationText(result, q, text);
        sendAfterAnswer(chatId, state, q, result.toString());
    }

    private void handlePhotoMessage(Message message) {
        Long chatId = message.getChatId();
        ChatQuizState state = sessions.get(chatId);
        if (state == null || state.sessionId == null) {
            return;
        }
        if (state.questionIndex >= quizData.questions().size()) {
            return;
        }

        QuizBotQuestion q = quizData.questions().get(state.questionIndex);
        if (!q.expectPhoto()) {
            return;
        }

        state.cancelTimeout();

        String fileId = message.getPhoto().get(message.getPhoto().size() - 1).getFileId();
        String caption = message.getCaption();

        Integer responseTimeMs = state.questionStartedAt != null
                ? (int) java.time.Duration.between(state.questionStartedAt, LocalDateTime.now()).toMillis()
                : null;

        try {
            sessionService.recordAnswer(state.sessionId, q.id(), null, true,
                    fileId, caption, responseTimeMs);
        } catch (Exception e) {
            log.warn("Could not record photo answer for session {}: {}", state.sessionId, e.getMessage());
        }

        state.score++;
        sendText(chatId, "📸 Photo received! Moving to next question...");
        state.questionIndex++;
        scheduler.schedule(() -> sendCurrentQuestion(chatId, state), 2, TimeUnit.SECONDS);
    }

    private void handleCallback(org.telegram.telegrambots.meta.api.objects.CallbackQuery callback) {
        String data = callback.getData();
        Long chatId = callback.getMessage().getChatId();
        Integer msgId = callback.getMessage().getMessageId();

        try {
            execute(AnswerCallbackQuery.builder().callbackQueryId(callback.getId()).build());
        } catch (TelegramApiException e) {
            log.debug("Could not answer callback: {}", e.getMessage());
        }

        ChatQuizState state = sessions.get(chatId);
        if (state == null) {
            sendText(chatId, "Send /start to begin the quiz.");
            return;
        }

        if (BRIEFING_START_TIMER.equals(data)) {
            state.cancelTimeout();
            removeKeyboard(chatId, msgId);
            startBriefingTimeout(chatId, state);
        } else if (BRIEFING_READY.equals(data)) {
            state.cancelTimeout();
            removeKeyboard(chatId, msgId);
            state.questionIndex++;
            sendCurrentQuestion(chatId, state);
        } else if (AFTER_ANSWER_READY.equals(data)) {
            state.cancelTimeout();
            removeKeyboard(chatId, msgId);
            state.questionIndex++;
            sendCurrentQuestion(chatId, state);
        } else if (data.startsWith("ans:")) {
            if (state.lastMessageId == null || !state.lastMessageId.equals(msgId)) {
                return; // stale button from a previous question
            }
            int selectedIndex;
            try {
                selectedIndex = Integer.parseInt(data.substring(4));
            } catch (NumberFormatException e) {
                log.warn("Invalid callback data for ans: {}", data);
                return;
            }
            state.cancelTimeout();
            removeKeyboard(chatId, msgId);
            processAnswer(chatId, state, selectedIndex);
        } else if (HINT_CALLBACK.equals(data)) {
            sendHint(chatId, quizData.questions().get(state.questionIndex));
        }
    }

    private void processAnswer(Long chatId, ChatQuizState state, int selectedIndex) {
        QuizBotQuestion q = quizData.questions().get(state.questionIndex);
        List<String> options = q.options();

        boolean correct = false;
        String selectedAnswer = null;
        if (selectedIndex >= 0 && selectedIndex < options.size()) {
            selectedAnswer = options.get(selectedIndex);
            correct = q.answer() != null
                && q.answer().trim().equalsIgnoreCase(selectedAnswer.trim());
        }

        if (correct) {
            state.score++;
        }

        Integer responseTimeMs = state.questionStartedAt != null
                ? (int) java.time.Duration.between(state.questionStartedAt, LocalDateTime.now()).toMillis()
                : null;

        try {
            sessionService.recordAnswer(state.sessionId, q.id(), selectedAnswer, correct,
                    null, null, responseTimeMs);
        } catch (Exception e) {
            log.warn("Could not record answer for session {}: {}", state.sessionId, e.getMessage());
        }

        StringBuilder result = new StringBuilder();
        result.append(correct ? "✅ <b>Correct!</b>" : "❌ <b>Wrong!</b>");
        if (!correct && q.answer() != null) {
            result.append("\nCorrect answer: <b>").append(escapeHtml(q.answer())).append("</b>");
        }
        if (q.hintBlocks() != null && !q.hintBlocks().isEmpty()) {
            String hintText = q.hintBlocks().stream()
                    .filter(b -> "text".equals(b.type()))
                    .map(b -> renderTelegramHtml(b.content()))
                    .collect(Collectors.joining("\n\n"));
            if (!hintText.isBlank()) {
                result.append("\n\n💡 ").append(hintText);
            }
        }
        appendExplanationText(result, q, null);
        sendAfterAnswer(chatId, state, q, result.toString());
    }

    private void startQuestionTimeout(Long chatId, ChatQuizState state) {
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            ChatQuizState current = sessions.get(chatId);
            if (current != state) return;

            QuizBotQuestion q = quizData.questions().get(state.questionIndex);
            Integer responseTimeMs = state.questionStartedAt != null
                    ? (int) java.time.Duration.between(state.questionStartedAt, LocalDateTime.now()).toMillis()
                    : null;

            try {
                sessionService.recordAnswer(state.sessionId, q.id(), null, false,
                        null, null, responseTimeMs);
            } catch (Exception e) {
                log.debug("Could not record timeout answer for session {}: {}", state.sessionId, e.getMessage());
            }

            Integer msgId = state.lastMessageId;
            if (msgId != null) removeKeyboard(chatId, msgId);
            sendText(chatId, "⏰ <b>Time's up!</b>");
            state.questionIndex++;
            sendCurrentQuestion(chatId, state);
        }, quizData.timePerQuestionSeconds(), TimeUnit.SECONDS);
        state.setTimeoutFuture(future);
    }

    private void startBriefingTimeout(Long chatId, ChatQuizState state) {
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            ChatQuizState current = sessions.get(chatId);
            if (current != state) return;
            Integer msgId = state.lastMessageId;
            if (msgId != null) removeKeyboard(chatId, msgId);
            state.questionIndex++;
            sendCurrentQuestion(chatId, state);
        }, 1, TimeUnit.SECONDS);
        state.setTimeoutFuture(future);
    }

    private void startQuizDeadline(Long chatId, ChatQuizState state) {
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            ChatQuizState current = sessions.get(chatId);
            if (current != state) return;

            state.cancelTimeout();

            if (state.questionIndex < quizData.questions().size()) {
                QuizBotQuestion q = quizData.questions().get(state.questionIndex);
                Integer responseTimeMs = state.questionStartedAt != null
                        ? (int) java.time.Duration.between(state.questionStartedAt, LocalDateTime.now()).toMillis()
                        : null;
                try {
                    sessionService.recordAnswer(state.sessionId, q.id(), null, false,
                            null, null, responseTimeMs);
                } catch (Exception e) {
                    log.debug("Could not record deadline answer for session {}: {}", state.sessionId, e.getMessage());
                }
                Integer msgId = state.lastMessageId;
                if (msgId != null) removeKeyboard(chatId, msgId);
            }
            sendText(chatId, "⏰ <b>Time's up!</b>");
            endQuiz(chatId, state);
        }, quizData.totalTimeLimitSeconds(), TimeUnit.SECONDS);
        state.setDeadlineFuture(future);
    }

    private void endQuiz(Long chatId, ChatQuizState state) {
        sessions.remove(chatId);
        int total = state.totalQuestions;
        int score = state.score;
        int percent = total > 0 ? (score * 100 / total) : 0;
        boolean passed = percent >= quizData.passScorePercent();

        try {
            sessionService.recordCompleted(quizData.quizId(), state.telegramUserId,
                    score, total, passed);
        } catch (Exception e) {
            log.warn("Could not record session complete for quiz {}: {}", quizData.quizId(), e.getMessage());
        }

        sendText(chatId,
                (passed ? "🎉" : "😔") + " <b>Quiz Complete!</b>\n\n" +
                "Score: <b>" + score + "/" + total + "</b> (" + percent + "%)\n" +
                "Result: " + (passed ? "<b>PASSED!</b> 🎊" : "<b>FAILED</b>") + "\n\n" +
                (passed ? "Congratulations! 🏆" : "Send /start to try again!"));
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private InlineKeyboardMarkup buildAnswerKeyboard(List<String> options, boolean hasHint) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            rows.add(List.of(InlineKeyboardButton.builder().text(options.get(i)).callbackData("ans:" + i).build()));
        }
        if (hasHint) {
            rows.add(List.of(InlineKeyboardButton.builder().text(HINT_BUTTON_TEXT).callbackData("hint").build()));
        }
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    private InlineKeyboardMarkup buildBriefingKeyboard(QuizBotQuestion question) {
        String secondaryText = normalizeBriefingButtonText(question.briefingSecondaryButtonText(), "Start Timer");
        String primaryText = normalizeBriefingButtonText(question.briefingPrimaryButtonText(), "READY");
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        if (question.showBriefingSecondaryButton()) {
            rows.add(List.of(InlineKeyboardButton.builder().text(secondaryText).callbackData(BRIEFING_START_TIMER).build()));
        }
        if (question.showBriefingPrimaryButton()) {
            rows.add(List.of(InlineKeyboardButton.builder().text(primaryText).callbackData(BRIEFING_READY).build()));
        }
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    private InlineKeyboardMarkup buildAfterAnswerKeyboard(QuizBotQuestion question) {
        if (!question.showAfterAnswerButton()) {
            return null;
        }
        String buttonText = normalizeBriefingButtonText(question.afterAnswerButtonText(), "Next Question");
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(List.of(InlineKeyboardButton.builder()
                        .text(buttonText)
                        .callbackData(AFTER_ANSWER_READY)
                        .build())))
                .build();
    }

    private void appendExplanationText(StringBuilder result, QuizBotQuestion q, String textInputValue) {
        if (q.explanationBlocks() == null || q.explanationBlocks().isEmpty()) {
            return;
        }
        String explanationText = q.explanationBlocks().stream()
                .filter(b -> "text".equals(b.type()))
                .map(b -> renderTemplateText(b.content(), textInputValue))
                .collect(Collectors.joining("\n\n"));
        if (!explanationText.isBlank()) {
            result.append("\n\n📖 ").append(explanationText);
        }
    }

    private String renderTemplateText(String text, String textInputValue) {
        String rendered = text == null ? "" : text;
        if (textInputValue != null && !textInputValue.isBlank()) {
            rendered = TEXT_INPUT_PLACEHOLDER_PATTERN.matcher(rendered).replaceAll(java.util.regex.Matcher.quoteReplacement(textInputValue));
        }
        return renderTelegramHtml(rendered);
    }

    private String renderTelegramHtml(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String escaped = escapeHtml(text);
        return escaped
            .replaceAll("(?is)&lt;b&gt;(.*?)&lt;/b&gt;", "<b>$1</b>")
            .replaceAll("(?is)&lt;strong&gt;(.*?)&lt;/strong&gt;", "<b>$1</b>")
            .replaceAll("(?is)&lt;i&gt;(.*?)&lt;/i&gt;", "<i>$1</i>")
            .replaceAll("(?is)&lt;em&gt;(.*?)&lt;/em&gt;", "<i>$1</i>")
            .replaceAll("(?is)&lt;u&gt;(.*?)&lt;/u&gt;", "<u>$1</u>")
            .replaceAll("(?is)&lt;s&gt;(.*?)&lt;/s&gt;", "<s>$1</s>")
            .replaceAll("(?is)&lt;del&gt;(.*?)&lt;/del&gt;", "<s>$1</s>")
            .replaceAll("(?is)&lt;code&gt;(.*?)&lt;/code&gt;", "<code>$1</code>")
                .replaceAll("(?i)&lt;br\s*/?&gt;", "\n");
    }

    private void sendAfterAnswer(Long chatId, ChatQuizState state, QuizBotQuestion question, String text) {
        boolean hasExplanation = question.explanationBlocks() != null && !question.explanationBlocks().isEmpty();
        if (hasExplanation) {
            sendImageBlocks(chatId, question.explanationBlocks());
        }
        if (hasExplanation && question.showAfterAnswerButton()) {
            sendMessageWithKeyboard(chatId, text, buildAfterAnswerKeyboard(question), result -> {
                if (result != null) state.lastMessageId = result.getMessageId();
            });
            return;
        }

        sendText(chatId, text);
        state.questionIndex++;
        scheduler.schedule(() -> sendCurrentQuestion(chatId, state), 2, TimeUnit.SECONDS);
    }

    private String normalizeBriefingButtonText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private void sendHint(Long chatId, QuizBotQuestion question) {
        if (question.hintBlocks() == null || question.hintBlocks().isEmpty()) {
            return;
        }
        sendImageBlocks(chatId, question.hintBlocks());
        String hintText = question.hintBlocks().stream()
                .filter(b -> "text".equals(b.type()))
                .map(b -> renderTelegramHtml(b.content()))
                .collect(Collectors.joining("\n\n"));
        if (!hintText.isBlank()) {
            sendText(chatId, "💡 " + hintText);
        }
    }

    private void sendImageBlocks(Long chatId, List<ContentBlock> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            log.debug("No content blocks to send as images for chat {}", chatId);
            return;
        }

        blocks.stream()
                .filter(b -> "image".equals(b.type()))
                .map(ContentBlock::content)
                .filter(path -> path != null && !path.isBlank())
                .forEach(path -> {
                    String imageId = path.trim();
                    log.info("Attempting to send image {} to chat {}", imageId, chatId);
                    sendImage(chatId, imageId);
                });
    }

    private void sendImage(Long chatId, String imageId) {
        try {
            Long id = Long.valueOf(imageId);
            ImageBlob blob = imageBlobService.findById(id);
            log.info("Loaded image {} for chat {} with contentType={} and bytes={}",
                    id, chatId, blob.getContentType(), blob.getData() != null ? blob.getData().length : 0);
            execute(SendPhoto.builder()
                    .chatId(chatId.toString())
                    .photo(new InputFile(new java.io.ByteArrayInputStream(blob.getData()), "image-" + id))
                    .build());
            log.info("Sent image {} to chat {}", id, chatId);
        } catch (Exception e) {
            log.warn("Could not send image {} to chat {}: {}", imageId, chatId, e.getMessage());
        }
    }

    private void sendText(Long chatId, String html) {
        try {
            execute(SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(html)
                    .parseMode("HTML")
                    .build());
        } catch (TelegramApiException e) {
            log.error("Could not send message to chat {}: {}", chatId, e.getMessage());
        }
    }

    private void sendMessageWithKeyboard(Long chatId, String html, InlineKeyboardMarkup keyboard,
                                          Consumer<Message> callback) {
        try {
            Message sent = execute(SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(html)
                    .parseMode("HTML")
                    .replyMarkup(keyboard)
                    .build());
            if (callback != null) callback.accept(sent);
        } catch (TelegramApiException e) {
            log.error("Could not send message with keyboard to chat {}: {}", chatId, e.getMessage());
            if (callback != null) callback.accept(null);
        }
    }

    private void removeKeyboard(Long chatId, Integer messageId) {
        try {
            execute(EditMessageReplyMarkup.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .replyMarkup(InlineKeyboardMarkup.builder().keyboard(List.of()).build())
                    .build());
        } catch (TelegramApiException e) {
            log.debug("Could not remove keyboard from message {}: {}", messageId, e.getMessage());
        }
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }

    public void shutdown() {
        sessions.values().forEach(state -> {
            state.cancelTimeout();
            state.cancelDeadline();
        });
        sessions.clear();
        scheduler.shutdownNow();
    }
}
