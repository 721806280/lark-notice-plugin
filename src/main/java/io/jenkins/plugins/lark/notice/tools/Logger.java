package io.jenkins.plugins.lark.notice.tools;

import hudson.model.TaskListener;
import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;

import java.io.PrintStream;

/**
 * Provides logging utilities for error and debug message handling within the application.
 * This class is designed to standardize the format of log messages and centralize the handling
 * of logging operations, particularly for interactions with the Lark platform. It supports
 * formatting messages, logging errors, and conditional debug logging based on global configuration settings.
 *
 * @author xm.z
 */
public class Logger {

    /**
     * Formats a message with given arguments. Prepends a standard error prefix to the message.
     *
     * @param msg  The message template containing placeholders.
     * @param args Arguments that will be replaced in the message template.
     * @return A formatted string with the specified message and arguments, including a standard prefix.
     */
    public static String format(String msg, Object... args) {
        return String.format("[Lark] error: %s", String.format(msg, args));
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
        listener.error("[Lark] error: %s", String.format(msg, args));
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
        PrintStream logger = listener.getLogger();
        logger.println(); // Ensure the message starts on a new line.
        logger.printf((msg) + "%n", args);
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
        LarkGlobalConfig globalConfig = LarkGlobalConfig.getInstance();
        if (globalConfig.isVerbose()) {
            Logger.debug(listener, "[Lark] " + msg, args);
        }
    }

}