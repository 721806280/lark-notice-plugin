package io.jenkins.plugins.lark.notice.logging;

/**
 * Named trace codes for verbose structured logs.
 */
public enum NoticeTrace {
    NOTIFICATION_PREPARE("notification.prepare"),
    NOTIFICATION_EXECUTOR("notification.executor"),
    NOTIFICATION_MATCH("notification.match"),
    NOTIFICATION_DISPATCH("notification.dispatch"),
    NOTIFICATION_RESULT("notification.result"),
    NOTIFICATION_MARK_BUILD_FAILURE("notification.mark-build-failure"),
    NOTIFICATION_EXCEPTION("notification.exception"),
    DISPATCHER_SEND_START("dispatcher.send.start"),
    DISPATCHER_SEND_FINISH("dispatcher.send.finish"),
    ENVIRONMENT_RESOLVE("environment.resolve"),
    ENVIRONMENT_RESOLVE_FAILURE("environment.resolve.failure"),
    PIPELINE_STEP_START("pipeline.step.start"),
    PIPELINE_STEP_FINISH("pipeline.step.finish"),
    PIPELINE_STEP_FAILURE("pipeline.step.failure"),
    RUN_USER_MOBILE_MISSING("run-user.mobile.missing"),
    RUN_USER_OPEN_ID_MISSING("run-user.open-id.missing");

    private final String code;

    NoticeTrace(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
