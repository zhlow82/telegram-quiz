package com.telegramquiz.main.bot;

import java.util.concurrent.ScheduledFuture;

public class ChatQuizState {

    public final long chatId;
    public final int totalQuestions;
    public final long telegramUserId;
    public final String telegramUsername;
    public final String telegramFirstName;
    public volatile int questionIndex = 0;
    public volatile int score = 0;
    public volatile Integer lastMessageId = null;
    private volatile ScheduledFuture<?> timeoutFuture = null;

    public ChatQuizState(long chatId, int totalQuestions,
                         long telegramUserId, String telegramUsername, String telegramFirstName) {
        this.chatId = chatId;
        this.totalQuestions = totalQuestions;
        this.telegramUserId = telegramUserId;
        this.telegramUsername = telegramUsername;
        this.telegramFirstName = telegramFirstName;
    }

    public void setTimeoutFuture(ScheduledFuture<?> future) {
        this.timeoutFuture = future;
    }

    public void cancelTimeout() {
        ScheduledFuture<?> f = timeoutFuture;
        if (f != null) {
            f.cancel(false);
            timeoutFuture = null;
        }
    }
}
