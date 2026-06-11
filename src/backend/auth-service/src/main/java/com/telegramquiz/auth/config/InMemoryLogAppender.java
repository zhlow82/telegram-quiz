package com.telegramquiz.auth.config;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

@Component
public class InMemoryLogAppender extends AppenderBase<ILoggingEvent> {

    private static final int MAX_ENTRIES = 1000;

    private final LogEntry[] buffer = new LogEntry[MAX_ENTRIES];
    private int writeIndex = 0;
    private int count = 0;

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

    public record LogEntry(long timestamp, String level, String logger, String message) {
        public String formattedTimestamp() {
            return Instant.ofEpochMilli(timestamp).toString();
        }
    }
}
