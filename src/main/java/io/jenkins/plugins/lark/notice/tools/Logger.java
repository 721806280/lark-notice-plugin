package io.jenkins.plugins.lark.notice.tools;

import hudson.model.TaskListener;
import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;

import java.io.PrintStream;
import java.util.StringJoiner;

/**
 * Provides logging utilities for error and debug message handling within the application.
 * This class is designed to standardize the format of log messages and centralize the handling
 * of logging operations, particularly for interactions with the Lark platform. It supports
 * formatting messages, logging errors, and conditional debug logging based on global configuration settings.
 *
 * @author xm.z
 */
public class Logger {

    private static final String PREFIX = "[Lark]";

    /**
     * Formats a message with given arguments. Prepends a standard error prefix to the message.
     *
     * @param msg  The message template containing placeholders.
     * @param args Arguments that will be replaced in the message template.
     * @return A formatted string with the specified message and arguments, including a standard prefix.
     */
    public static String format(String msg, Object... args) {
        return String.format("%s error: %s", PREFIX, String.format(msg, args));
    }

    /**
     * Logs an error message through the specified task listener. The message is formatted and
     * prefixed to indicate it is an error related to Lark operations.
     *
     * @param listener The task listener through which the error message will be logged.
     * @param msg      The error message template.
     * @param args     Arguments for the message template.
     */
    public static void error(TaskListener listener, String msg, Object... args) {
        if (listener == null) {
            return;
        }
        listener.error(PREFIX + " error: %s", String.format(msg, args));
    }

    /**
     * Logs a debug message through the specified task listener. The message is printed directly
     * to the listener's logger, allowing for real-time debugging information to be output.
     *
     * @param listener The task listener through which the debug message will be logged.
     * @param msg      The debug message template.
     * @param args     Arguments for the message template.
     */
    public static void debug(TaskListener listener, String msg, Object... args) {
        if (listener == null) {
            return;
        }
        PrintStream logger = listener.getLogger();
        logger.printf(msg + "%n", args);
    }

    /**
     * Logs a message through the specified task listener, depending on the global verbose configuration.
     * If verbose logging is enabled, the message is logged as a debug message; otherwise, it is ignored.
     * This allows for conditional logging based on the application's current configuration settings.
     *
     * @param listener The task listener through which the message will be logged if verbose logging is enabled.
     * @param msg      The message template.
     * @param args     Arguments for the message template.
     */
    public static void log(TaskListener listener, String msg, Object... args) {
        if (isVerboseEnabled()) {
            Logger.debug(listener, PREFIX + " " + msg, args);
        }
    }

    /**
     * Logs a structured event when verbose mode is enabled.
     *
     * @param listener task listener
     * @param event    short event name such as "notify.prepare"
     * @param pairs    key/value pairs, e.g. "job", "demo", "run", "#12"
     */
    public static void event(TaskListener listener, String event, Object... pairs) {
        if (!isVerboseEnabled()) {
            return;
        }
        Logger.debug(listener, "%s [%s] %s", PREFIX, event, toKvString(pairs));
    }

    /**
     * Safely truncates long text for verbose logs.
     */
    public static String clip(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        if (maxLength <= 3) {
            return value.substring(0, Math.max(maxLength, 0));
        }
        return value.substring(0, maxLength - 3) + "...";
    }

    private static boolean isVerboseEnabled() {
        LarkGlobalConfig globalConfig = LarkGlobalConfig.getInstance();
        return globalConfig != null && globalConfig.isVerbose();
    }

    private static String toKvString(Object... pairs) {
        if (pairs == null || pairs.length == 0) {
            return "";
        }
        StringJoiner joiner = new StringJoiner(" ");
        for (int i = 0; i < pairs.length; i += 2) {
            Object key = pairs[i];
            Object value = i + 1 < pairs.length ? pairs[i + 1] : "<missing>";
            joiner.add(String.format("%s=%s", key, value));
        }
        return joiner.toString();
    }

}
