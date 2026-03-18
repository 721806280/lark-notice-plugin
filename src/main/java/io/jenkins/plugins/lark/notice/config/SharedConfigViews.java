package io.jenkins.plugins.lark.notice.config;

/**
 * Marker class used as the stable resource owner for shared Jelly fragments.
 * Shared views must be resolved against a real class when the plugin runs from
 * the packaged HPI, otherwise Stapler may look them up relative to the current
 * rendering context (for example WorkflowJob) and fail to find them.
 */
public final class SharedConfigViews {

    private SharedConfigViews() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
