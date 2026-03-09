package io.jenkins.plugins.lark.notice.logging;

/**
 * Standard field names for structured plugin trace logs.
 *
 * @author xm.z
 */
public enum NoticeLogKey {
    SOURCE("source"),
    JOB("job"),
    RUN("run"),
    BUILD("build"),
    OCCASION("occasion"),
    CONFIG_TOTAL("configTotal"),
    MATCHED_CONFIG_TOTAL("matchedConfigTotal"),
    EXECUTOR("executor"),
    USER("user"),
    URL("url"),
    PROJECT("project"),
    STEP("step"),
    ROBOT("robot"),
    ROBOT_ID("robotId"),
    ROBOT_TYPE("robotType"),
    MESSAGE_TYPE("messageType"),
    RAW_MODE("rawMode"),
    AT_ALL("atAll"),
    AT_USER_TOTAL("atUserTotal"),
    HAS_MOBILE("hasMobile"),
    HAS_OPEN_ID("hasOpenId"),
    ENV_TOTAL("envTotal"),
    REQUEST_SIZE("requestSize"),
    SUCCESS("success"),
    RESULT_CODE("resultCode"),
    MESSAGE("message"),
    ERROR("error"),
    ERROR_TYPE("errorType");

    private final String externalName;

    /**
     * Creates one key enum entry bound to its rendered field name.
     *
     * @param externalName field name used in trace output
     */
    NoticeLogKey(String externalName) {
        this.externalName = externalName;
    }

    /**
     * Returns the rendered field name used in trace output.
     *
     * @return external field name
     */
    public String externalName() {
        return externalName;
    }
}
