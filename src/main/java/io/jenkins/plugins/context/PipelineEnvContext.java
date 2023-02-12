package io.jenkins.plugins.context;

import hudson.EnvVars;

public class PipelineEnvContext {

    private final static ThreadLocal<EnvVars> STORE = new ThreadLocal<>();

    public static void merge(EnvVars value) {
        EnvVars current = STORE.get();

        if (current == null) {
            STORE.set(value);
        } else {
            current.overrideAll(value);
        }
    }

    public static EnvVars get() {
        return STORE.get();
    }

}