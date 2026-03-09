package io.jenkins.plugins.lark.notice.tools;

/**
 * Standard keys for structured plugin logs.
 *
 * @author xm.z
 */
public final class LogField {

    private LogField() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static final String SOURCE = "source";
    public static final String JOB = "job";
    public static final String RUN = "run";
    public static final String BUILD = "build";
    public static final String OCCASION = "occasion";
    public static final String CONFIG_COUNT = "configCount";
    public static final String MATCHED_CONFIG_COUNT = "matchedConfigCount";

    public static final String EXECUTOR = "executor";
    public static final String USER = "user";
    public static final String URL = "url";
    public static final String PROJECT = "project";
    public static final String STEP = "step";
    public static final String ROBOT = "robot";
    public static final String ROBOT_ID = "robotId";
    public static final String ROBOT_TYPE = "robotType";
    public static final String MSG_TYPE = "msgType";
    public static final String RAW = "raw";
    public static final String AT_ALL = "atAll";
    public static final String AT_USER_COUNT = "atUserCount";
    public static final String HAS_MOBILE = "hasMobile";
    public static final String HAS_OPEN_ID = "hasOpenId";
    public static final String ENV_COUNT = "envCount";
    public static final String REQUEST_SIZE = "requestSize";
    public static final String OK = "ok";
    public static final String CODE = "code";
    public static final String MSG = "msg";
    public static final String ERROR = "error";
    public static final String ERROR_TYPE = "errorType";
}
