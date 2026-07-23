package io.jenkins.plugins.lark.notice.context;

import hudson.EnvVars;
import hudson.model.InvisibleAction;
import hudson.model.Run;

/**
 * Stores environment variables collected from Pipeline step contexts for one Jenkins build.
 *
 * <p>The state is attached to the owning {@link Run}, rather than to a worker thread. Jenkins
 * may resume Pipeline steps on another thread, and one thread may process multiple builds.</p>
 *
 * @author xm.z
 */
public final class PipelineEnvContext {

    private PipelineEnvContext() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Merges variables collected from a Pipeline step into the specified build context.
     *
     * @param run   build that owns the variables
     * @param value variables collected from the Pipeline step
     */
    public static void merge(Run<?, ?> run, EnvVars value) {
        if (run == null || value == null) {
            return;
        }
        getOrCreate(run).merge(value);
    }

    /**
     * Returns a defensive copy of the variables collected for a build.
     *
     * @param run build whose variables should be read
     * @return a snapshot, never {@code null}
     */
    public static EnvVars get(Run<?, ?> run) {
        PipelineEnvAction action = run == null ? null : run.getAction(PipelineEnvAction.class);
        return action == null ? new EnvVars() : action.snapshot();
    }

    /**
     * Removes the transient Pipeline context attached to a build.
     *
     * @param run build whose context should be removed
     */
    public static void reset(Run<?, ?> run) {
        if (run == null) {
            return;
        }
        synchronized (run) {
            PipelineEnvAction action = run.getAction(PipelineEnvAction.class);
            if (action != null) {
                run.removeAction(action);
            }
        }
    }

    /**
     * Finds or atomically attaches the transient action for a build.
     */
    private static PipelineEnvAction getOrCreate(Run<?, ?> run) {
        synchronized (run) {
            PipelineEnvAction action = run.getAction(PipelineEnvAction.class);
            if (action == null) {
                action = new PipelineEnvAction();
                run.addAction(action);
            }
            return action;
        }
    }

    /**
     * Runtime-only action; environment values must not be persisted into {@code build.xml}.
     */
    private static final class PipelineEnvAction extends InvisibleAction {

        private transient EnvVars envVars;

        private synchronized void merge(EnvVars value) {
            values().overrideAll(value);
        }

        private synchronized EnvVars snapshot() {
            return new EnvVars(values());
        }

        private EnvVars values() {
            if (envVars == null) {
                envVars = new EnvVars();
            }
            return envVars;
        }
    }
}
