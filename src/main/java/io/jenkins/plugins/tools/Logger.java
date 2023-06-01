package io.jenkins.plugins.tools;

import hudson.model.TaskListener;
import io.jenkins.plugins.config.FeiShuTalkGlobalConfig;
import org.apache.commons.lang.StringUtils;

import java.io.PrintStream;

/**
 * @author xm.z
 */
public class Logger {

    /**
     * 日志 icon
     */
    private static final String LOG_ICON = "\uD83C\uDF4F";

    /**
     * 日志连接线符号
     */
    private static final String LOG_LINE_SYMBOL = "\uD83C\uDF2B";

    /**
     * 连接线单边的长度
     */
    private static final short LOG_LINE_HALF_COUNT = 12;

    /**
     * 单条连接线
     */
    private static final String LOG_HALF_LINE = StringUtils
            .repeat(LOG_LINE_SYMBOL, LOG_LINE_HALF_COUNT);

    /**
     * 日志标题
     */
    private static final String LOG_TITLE = "飞书机器人调试日志信息开始";

    /**
     * 标题占位符
     */
    private static final String LOG_TITLE_PLACEHOLDER = StringUtils
            .repeat(LOG_LINE_SYMBOL, LOG_TITLE.length());

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
        boolean verbose = globalConfig.isVerbose();
        if (verbose) {
            Logger.debug(listener, "[飞书插件]" + msg, args);
        }
    }

    /**
     * 分割线
     *
     * @param listener 任务监听器
     * @param lineType 线的类型
     */
    public static void line(TaskListener listener, LineType lineType) {
        PrintStream logger = listener.getLogger();
        if (LineType.START.equals(lineType)) {
            logger.println();
            logger.println(LOG_ICON + LOG_HALF_LINE + LOG_TITLE + LOG_HALF_LINE + LOG_ICON);
        } else {
            logger.println(LOG_ICON + LOG_HALF_LINE + LOG_TITLE_PLACEHOLDER + LOG_HALF_LINE + LOG_ICON);
            logger.println();
        }
    }

    public enum LineType {
        /**
         * 开始
         */
        START,
        /**
         * 结束
         */
        END
    }

}
