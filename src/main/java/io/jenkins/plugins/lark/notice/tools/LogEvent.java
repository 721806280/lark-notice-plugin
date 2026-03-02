package io.jenkins.plugins.lark.notice.tools;

/**
 * Centralized event names for structured verbose logs.
 */
public final class LogEvent {

    private LogEvent() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static final String NOTIFY_PREPARE = "notify.prepare";
    public static final String NOTIFY_EXECUTOR = "notify.executor";
    public static final String NOTIFY_MATCH = "notify.match";
    public static final String NOTIFY_DISPATCH = "notify.dispatch";
    public static final String NOTIFY_RESULT = "notify.result";
    public static final String NOTIFY_MARK_BUILD_FAILURE = "notify.mark-build-failure";
    public static final String NOTIFY_EXCEPTION = "notify.exception";

    public static final String DISPATCHER_SEND_START = "dispatcher.send.start";
    public static final String DISPATCHER_SEND_END = "dispatcher.send.end";

    public static final String ENV_RESOLVE = "env.resolve";
    public static final String ENV_RESOLVE_FAILED = "env.resolve.failed";

    public static final String PIPELINE_STEP_START = "pipeline.step.start";
    public static final String PIPELINE_STEP_END = "pipeline.step.end";
    public static final String PIPELINE_STEP_EXCEPTION = "pipeline.step.exception";

    public static final String RUN_USER_MISSING_MOBILE = "run-user.missing-mobile";
    public static final String RUN_USER_MISSING_OPENID = "run-user.missing-openid";
}
