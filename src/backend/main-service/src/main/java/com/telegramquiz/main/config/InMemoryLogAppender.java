package com.telegramquiz.main.config;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class InMemoryLogAppender extends AppenderBase<ILoggingEvent> {

    private static final int MAX_ENTRIES = 1000;
    private static InMemoryLogAppender instance;

    private final LogEntry[] buffer = new LogEntry[MAX_ENTRIES];
    private int writeIndex = 0;
    private int count = 0;

    public InMemoryLogAppender() {
        instance = this;
    }

    public static InMemoryLogAppender getInstance() {
        return instance;
    }

    @Override
    protected void append(ILoggingEvent event) {
        LogEntry entry = new LogEntry(
                event.getTimeStamp(),
                event.getLevel().toString(),
                event.getLoggerName(),
                event.getFormattedMessage()
        );
        synchronized (buffer) {
            buffer[writeIndex] = entry;
            writeIndex = (writeIndex + 1) % MAX_ENTRIES;
            if (count < MAX_ENTRIES) {
                count++;
            }
        }
    }

    public List<LogEntry> getLogs(String level, String search, int limit) {
        synchronized (buffer) {
            List<LogEntry> result = new ArrayList<>();
            int idx = (writeIndex - count + MAX_ENTRIES) % MAX_ENTRIES;
            for (int i = 0; i < count; i++) {
                LogEntry entry = buffer[(idx + i) % MAX_ENTRIES];
                if (entry != null && matches(entry, level, search)) {
                    result.add(entry);
                    if (result.size() >= limit) break;
                }
            }
            return Collections.unmodifiableList(result);
        }
    }

    private boolean matches(LogEntry entry, String level, String search) {
        if (level != null && !level.isEmpty() && !entry.level().equals(level)) {
            return false;
        }
        if (search != null && !search.isEmpty()) {
            String s = search.toLowerCase();
            if (!entry.message().toLowerCase().contains(s)
                    && !entry.logger().toLowerCase().contains(s)) {
                return false;
            }
        }
        return true;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void register() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);

        // Remove existing IN_MEMORY appender if present
        AppenderBase<ILoggingEvent> existing = (AppenderBase<ILoggingEvent>) rootLogger.getAppender("IN_MEMORY");
        if (existing != null) {
            rootLogger.detachAppender(existing);
        }

        setName("IN_MEMORY");
        setContext(context);
        start();
        rootLogger.addAppender(this);
    }

    public record LogEntry(long timestamp, String level, String logger, String message) {
        public String formattedTimestamp() {
            return java.time.Instant.ofEpochMilli(timestamp).toString();
        }
    }
}
