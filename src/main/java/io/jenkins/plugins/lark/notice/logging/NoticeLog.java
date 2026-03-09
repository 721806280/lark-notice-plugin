package io.jenkins.plugins.lark.notice.logging;

import hudson.model.TaskListener;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Centralized console logging for the plugin.
 *
 * @author xm.z
 */
public final class NoticeLog {

    private static final String PREFIX = "[Lark]";

    private NoticeLog() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Creates one structured trace field for later emission.
     *
     * @param key   field key
     * @param value field value
     * @return structured log field
     */
    public static NoticeLogField field(NoticeLogKey key, Object value) {
        return NoticeLogField.of(key, value);
    }

    /**
     * Formats one failure-facing message with the plugin prefix.
     *
     * @param template message template
     * @param args     template arguments
     * @return prefixed message ready for exceptions or user-facing failures
     */
    public static String failureMessage(String template, Object... args) {
        return String.format("%s %s", PREFIX, String.format(template, args));
    }

    /**
     * Emits an error line through the Jenkins task listener.
     *
     * @param listener target listener
     * @param template message template
     * @param args     template arguments
     */
    public static void error(TaskListener listener, String template, Object... args) {
        if (listener == null) {
            return;
        }
        listener.error("%s %s", PREFIX, String.format(template, args));
    }

    /**
     * Emits one verbose plain-text line when verbose logging is enabled.
     *
     * @param listener target listener
     * @param template message template
     * @param args     template arguments
     */
    public static void verbose(TaskListener listener, String template, Object... args) {
        if (listener == null || !NoticeLogSettings.isVerboseEnabled()) {
            return;
        }
        write(listener, "%s %s", PREFIX, String.format(template, args));
    }

    /**
     * Emits one structured trace line when verbose logging is enabled.
     *
     * @param listener target listener
     * @param trace    trace code describing the event
     * @param fields   structured context fields attached to the event
     */
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

    /**
     * Truncates long values for log safety and readability.
     *
     * @param value     source value
     * @param maxLength maximum output length
     * @return truncated value when necessary
     */
    public static String abbreviate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        if (maxLength <= 3) {
            return value.substring(0, Math.max(maxLength, 0));
        }
        return value.substring(0, maxLength - 3) + "...";
    }

    /**
     * Writes one formatted line to the underlying Jenkins listener stream.
     *
     * @param listener target listener
     * @param template output template
     * @param args     template arguments
     */
    private static void write(TaskListener listener, String template, Object... args) {
        PrintStream logger = listener.getLogger();
        logger.printf(template + "%n", args);
    }
}
