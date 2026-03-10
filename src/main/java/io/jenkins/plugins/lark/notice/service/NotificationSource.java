package io.jenkins.plugins.lark.notice.service;

/**
 * Logical notification sources used in structured logs.
 *
 * @author xm.z
 */
public final class NotificationSource {

    private NotificationSource() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Notification source emitted by the run listener.
     */
    public static final String RUN_LISTENER = "run-listener";

    /**
     * Notification source emitted by the post-build notifier.
     */
    public static final String POST_BUILD = "post-build";
}
