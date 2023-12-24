package io.jenkins.plugins.feishu.notification.tools;

import hudson.model.TaskListener;
import io.jenkins.plugins.feishu.notification.config.FeiShuTalkGlobalConfig;

import java.io.PrintStream;

/**
 * @author xm.z
 */
public class Logger {

    /**
     * 格式化信息
     *
     * @param msg  消息
     * @param args 参数数组
     */
    public static String format(String msg, Object... args) {
        return String.format("飞书机器人发生错误：%s", String.format(msg, args));
    }

    /**
     * 统一输出错误日志
     *
     * @param listener 任务监听器
     * @param msg      消息
     */
    public static void error(TaskListener listener, String msg, Object... args) {
        listener.error("飞书机器人发生错误：%s", String.format(msg, args));
    }

    /**
     * 统一输出调试日志
     *
     * @param listener 任务监听器
     * @param msg      消息
     */
    public static void debug(TaskListener listener, String msg, Object... args) {
        PrintStream logger = listener.getLogger();
        logger.println();
        logger.printf((msg) + "%n", args);
    }

    /**
     * 统一输出调试日志
     *
     * @param listener 任务监听器
     * @param msg      消息
     */
    public static void log(TaskListener listener, String msg, Object... args) {
        FeiShuTalkGlobalConfig globalConfig = FeiShuTalkGlobalConfig.getInstance();
        if (globalConfig.isVerbose()) {
            Logger.debug(listener, "[飞书插件]" + msg, args);
        }
    }

}
