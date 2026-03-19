package io.jenkins.plugins.lark.notice.context;

/**
 * Shared environment variable keys used by message templates and rendering.
 */
public final class NoticeEnvVars {

    public static final String EXECUTOR_NAME = "EXECUTOR_NAME";
    public static final String EXECUTOR_MOBILE = "EXECUTOR_MOBILE";
    public static final String EXECUTOR_OPENID = "EXECUTOR_OPENID";
    public static final String PROJECT_NAME = "PROJECT_NAME";
    public static final String PROJECT_URL = "PROJECT_URL";
    public static final String JOB_NAME = "JOB_NAME";
    public static final String JOB_URL = "JOB_URL";
    public static final String JOB_DURATION = "JOB_DURATION";
    public static final String JOB_STATUS = "JOB_STATUS";

    private NoticeEnvVars() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Returns a placeholder token for template interpolation.
     *
     * @param key environment variable key
     * @return placeholder token in ${KEY} form
     */
    public static String placeholder(String key) {
        return "${" + key + "}";
    }
}
