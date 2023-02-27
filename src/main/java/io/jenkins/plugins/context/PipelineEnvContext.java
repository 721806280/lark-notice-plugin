package io.jenkins.plugins.context;

import hudson.EnvVars;

/**
 * 管道环境上下文
 *
 * @author xm.z
 */
public class PipelineEnvContext {

    private static final ThreadLocal<EnvVars> STORE = new ThreadLocal<>();

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

    public static EnvVars get() {
        EnvVars current = STORE.get();
        return current == null ? new EnvVars() : current;
    }

    public static void reset() {
        STORE.remove();
    }
}