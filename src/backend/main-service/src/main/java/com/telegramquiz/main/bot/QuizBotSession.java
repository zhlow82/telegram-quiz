package com.telegramquiz.main.bot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.telegramquiz.main.service.QuizSessionService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QuizBotSession extends TelegramLongPollingBot {

    private final QuizBotData quizData;
    private final QuizSessionService sessionService;
    private final ScheduledExecutorService scheduler;
    private final ConcurrentHashMap<Long, ChatQuizState> sessions = new ConcurrentHashMap<>();

    private static final String[] NUMBER_EMOJIS = {"1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣"};

    public QuizBotSession(QuizBotData quizData, QuizSessionService sessionService) {
        super(quizData.botToken());
        this.quizData = quizData;
        this.sessionService = sessionService;
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
            if (update.hasMessage() && update.getMessage().hasText()) {
                String text = update.getMessage().getText();
                if ("/start".equals(text) || text.startsWith("/start ")) {
                    startOrRestartSession(update.getMessage());
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
        }

        if (quizData.questions().isEmpty()) {
            sendText(chatId, "⚠️ This quiz has no questions yet.");
            return;
        }

        ChatQuizState state = new ChatQuizState(chatId, quizData.questions().size(),
                telegramUserId, telegramUsername, telegramFirstName);
        sessions.put(chatId, state);

        try {
            sessionService.recordStarted(quizData.quizId(), quizData.questions().size(),
                    telegramUserId, telegramUsername, telegramFirstName);
        } catch (Exception e) {
            log.warn("Could not record session start for quiz {}: {}", quizData.quizId(), e.getMessage());
        }

        sendText(chatId,
                "👋 Welcome to <b>" + escapeHtml(quizData.quizName()) + "</b>!\n\n" +
                "⏱ <b>" + quizData.timePerQuestionSeconds() + "s</b> per question\n" +
                "✅ Pass score: <b>" + quizData.passScorePercent() + "%</b>\n\n" +
                "Let's go! 🚀");

        sendCurrentQuestion(chatId, state);
    }

    private void sendCurrentQuestion(Long chatId, ChatQuizState state) {
        if (state.questionIndex >= quizData.questions().size()) {
            endQuiz(chatId, state);
            return;
        }

        QuizBotQuestion q = quizData.questions().get(state.questionIndex);

        if (q.isBriefing()) {
            StringBuilder sb = new StringBuilder();
            sb.append("📋 <b>Briefing ").append(state.questionIndex + 1).append("/").append(state.totalQuestions).append("</b>\n\n");
            String qText = q.questionBlocks() != null && !q.questionBlocks().isEmpty()
                    ? q.questionBlocks().stream()
                        .filter(b -> "text".equals(b.type()))
                        .map(b -> escapeHtml(b.content()))
                        .collect(Collectors.joining("\n\n"))
                    : "";
            sb.append(qText);

            sendMessageWithKeyboard(chatId, sb.toString(), buildBriefingKeyboard(), result -> {
                if (result != null) state.lastMessageId = result.getMessageId();
            });
            startBriefingTimeout(chatId, state);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("❓ <b>Question ").append(state.questionIndex + 1)
              .append("/").append(state.totalQuestions).append("</b>\n\n");
            String qText = q.questionBlocks() != null && !q.questionBlocks().isEmpty()
                    ? q.questionBlocks().stream()
                        .filter(b -> "text".equals(b.type()))
                        .map(b -> escapeHtml(b.content()))
                        .collect(Collectors.joining("\n\n"))
                    : "";
            sb.append(qText).append("\n\n");

            List<String> options = q.options();
            for (int i = 0; i < options.size(); i++) {
                String num = i < NUMBER_EMOJIS.length ? NUMBER_EMOJIS[i] : (i + 1) + ".";
                sb.append(num).append(" ").append(escapeHtml(options.get(i))).append("\n");
            }
            sb.append("\n⏱ ").append(quizData.timePerQuestionSeconds()).append("s");

            sendMessageWithKeyboard(chatId, sb.toString(), buildAnswerKeyboard(options), result -> {
                if (result != null) state.lastMessageId = result.getMessageId();
            });
            startQuestionTimeout(chatId, state);
        }
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

        if ("briefing:next".equals(data)) {
            state.cancelTimeout();
            removeKeyboard(chatId, msgId);
            state.questionIndex++;
            sendCurrentQuestion(chatId, state);
        } else if (data.startsWith("ans:")) {
            if (state.lastMessageId == null || !state.lastMessageId.equals(msgId)) {
                return; // stale button from a previous question
            }
            int selectedIndex = Integer.parseInt(data.substring(4));
            state.cancelTimeout();
            removeKeyboard(chatId, msgId);
            processAnswer(chatId, state, selectedIndex);
        }
    }

    private void processAnswer(Long chatId, ChatQuizState state, int selectedIndex) {
        QuizBotQuestion q = quizData.questions().get(state.questionIndex);
        List<String> options = q.options();

        boolean correct = false;
        String selectedText = null;
        if (selectedIndex >= 0 && selectedIndex < options.size()) {
            selectedText = options.get(selectedIndex);
            correct = q.answer() != null
                    && q.answer().trim().equalsIgnoreCase(selectedText.trim());
        }

        if (correct) {
            state.score++;
        }

        StringBuilder result = new StringBuilder();
        result.append(correct ? "✅ <b>Correct!</b>" : "❌ <b>Wrong!</b>");
        if (!correct && q.answer() != null) {
            result.append("\nCorrect answer: <b>").append(escapeHtml(q.answer())).append("</b>");
        }
        if (q.hintBlocks() != null && !q.hintBlocks().isEmpty()) {
            String hintText = q.hintBlocks().stream()
                    .filter(b -> "text".equals(b.type()))
                    .map(b -> escapeHtml(b.content()))
                    .collect(Collectors.joining("\n\n"));
            if (!hintText.isBlank()) {
                result.append("\n\n💡 ").append(hintText);
            }
        }
        if (q.explanationBlocks() != null && !q.explanationBlocks().isEmpty()) {
            String explanationText = q.explanationBlocks().stream()
                    .filter(b -> "text".equals(b.type()))
                    .map(b -> escapeHtml(b.content()))
                    .collect(Collectors.joining("\n\n"));
            if (!explanationText.isBlank()) {
                result.append("\n\n📖 ").append(explanationText);
            }
        }
        sendText(chatId, result.toString());

        state.questionIndex++;
        scheduler.schedule(() -> sendCurrentQuestion(chatId, state), 2, TimeUnit.SECONDS);
    }

    private void startQuestionTimeout(Long chatId, ChatQuizState state) {
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            ChatQuizState current = sessions.get(chatId);
            if (current != state) return;
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
        }, quizData.timePerQuestionSeconds(), TimeUnit.SECONDS);
        state.setTimeoutFuture(future);
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

    private InlineKeyboardMarkup buildAnswerKeyboard(List<String> options) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            currentRow.add(InlineKeyboardButton.builder()
                    .text(String.valueOf(i + 1))
                    .callbackData("ans:" + i)
                    .build());
            if (currentRow.size() == 2 || i == options.size() - 1) {
                rows.add(new ArrayList<>(currentRow));
                currentRow.clear();
            }
        }
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    private InlineKeyboardMarkup buildBriefingKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(List.of(
                        InlineKeyboardButton.builder().text("Next ▶").callbackData("briefing:next").build()
                )))
                .build();
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
        sessions.values().forEach(ChatQuizState::cancelTimeout);
        sessions.clear();
        scheduler.shutdownNow();
    }
}
