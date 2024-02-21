package io.jenkins.plugins.lark.notice.context;

import hudson.EnvVars;

/**
 * A utility class for managing environment variables within a pipeline's execution context.
 * It leverages a {@link ThreadLocal} store to maintain environment variables specific to the current thread,
 * allowing for thread-safe modifications and access to the environment variables during a pipeline's execution.
 *
 * @author xm.z
 */
public class PipelineEnvContext {

    /**
     * Thread-local storage for environment variables. This ensures that each thread has its own
     * copy of environment variables, preventing conflicts in concurrent environments.
     */
    private static final ThreadLocal<EnvVars> STORE = new ThreadLocal<>();

    /**
     * Merges the given environment variables with the current thread's environment variables.
     * If the current thread does not have any environment variables set, it initializes them with the given value.
     * If environment variables are already present, it overrides them with the values from the given environment variables.
     *
     * @param value The environment variables to merge into the current thread's environment variables.
     */
    public static void merge(EnvVars value) {
        if (value == null) {
            return;
        }
        EnvVars current = STORE.get();
        if (current == null) {
            STORE.set(value);
        } else {
            current.overrideAll(value);
        }
    }

    /**
     * Retrieves the current thread's environment variables. If no environment variables are set for the current thread,
     * it returns an empty {@link EnvVars} instance.
     *
     * @return The current thread's environment variables, never {@code null}.
     */
    public static EnvVars get() {
        EnvVars current = STORE.get();
        return current == null ? new EnvVars() : current;
    }

    /**
     * Resets the environment variables for the current thread. This effectively clears any environment variables
     * that were previously set or merged into the thread's environment variable store.
     */
    public static void reset() {
        STORE.remove();
    }
}
