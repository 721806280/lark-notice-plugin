package io.jenkins.plugins.lark.notice.logging;

import hudson.model.TaskListener;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Centralized console logging for the plugin.
 */
public final class NoticeLog {

    private static final String PREFIX = "[Lark]";

    private NoticeLog() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static NoticeLogField field(NoticeLogKey key, Object value) {
        return NoticeLogField.of(key, value);
    }

    public static String failureMessage(String template, Object... args) {
        return String.format("%s %s", PREFIX, String.format(template, args));
    }

    public static void error(TaskListener listener, String template, Object... args) {
        if (listener == null) {
            return;
        }
        listener.error("%s %s", PREFIX, String.format(template, args));
    }

    public static void verbose(TaskListener listener, String template, Object... args) {
        if (listener == null || !NoticeLogSettings.isVerboseEnabled()) {
            return;
        }
        write(listener, "%s %s", PREFIX, String.format(template, args));
    }

    public static void trace(TaskListener listener, NoticeTrace trace, NoticeLogField... fields) {
        if (listener == null || !NoticeLogSettings.isVerboseEnabled()) {
            return;
        }
        String payload = Arrays.stream(fields == null ? new NoticeLogField[0] : fields)
                .map(NoticeLogField::render)
                .collect(Collectors.joining(" "));
        if (payload.isEmpty()) {
            write(listener, "%s [%s]", PREFIX, trace.code());
            return;
        }
        write(listener, "%s [%s] %s", PREFIX, trace.code(), payload);
    }

    public static String abbreviate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        if (maxLength <= 3) {
            return value.substring(0, Math.max(maxLength, 0));
        }
        return value.substring(0, maxLength - 3) + "...";
    }

    private static void write(TaskListener listener, String template, Object... args) {
        PrintStream logger = listener.getLogger();
        logger.printf(template + "%n", args);
    }
}
