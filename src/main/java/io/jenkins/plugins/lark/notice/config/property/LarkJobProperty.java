package io.jenkins.plugins.lark.notice.config.property;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import io.jenkins.plugins.lark.notice.config.LarkNotifierConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A job-level property that provides Lark notification configurations.
 * <p>
 * This class allows users to define custom Lark notifier settings at the job level,
 * which can override global defaults.
 *
 * @author xm.z
 */
@NoArgsConstructor
public class LarkJobProperty extends JobProperty<Job<?, ?>> implements LarkNotifierProvider {

    /**
     * List of Lark notifier configurations specific to this job.
     * <p>
     * These configurations will be merged with global settings to determine
     * which Lark robots should be triggered for notifications.
     */
    @Getter
    private List<LarkNotifierConfig> larkNotifierConfigs;

    /**
     * Constructs a new instance of {@link LarkJobProperty}.
     * <p>
     * This constructor is typically used by Jenkins UI and configuration system
     * to initialize job-level Lark notification settings.
     *
     * @param notifierConfigs A list of Lark notifier configurations defined at the job level.
     *                        If null or empty, only global configurations will be used.
     */
    @DataBoundConstructor
    public LarkJobProperty(List<LarkNotifierConfig> notifierConfigs) {
        this.larkNotifierConfigs = notifierConfigs == null ? null : new ArrayList<>(notifierConfigs);
    }

    /**
     * Descriptor for {@link LarkJobProperty}.
     * <p>
     * Provides metadata and UI-related behavior for configuring Lark notifications at the job level.
     */
    @Extension
    public static class DescriptorImpl extends JobPropertyDescriptor {

        /**
         * Returns the user-visible display name of this property.
         * <p>
         * Used in Jenkins UI when showing configuration options.
         *
         * @return localized display name
         */
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.plugin_display_name();
        }

        /**
         * Determines if this property is applicable to the given job type.
         * <p>
         * By default, it delegates to the parent implementation which allows all job types.
         * Override this method if you want to restrict applicability to certain job types.
         *
         * @param jobType The type of job to check applicability for.
         * @return true if this property is applicable; false otherwise
         */
        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            // By default, applicable to all job types. Override if specific job types should be excluded.
            return super.isApplicable(jobType);
        }

        /**
         * Gets the default list of Lark notifier configurations from global settings.
         * <p>
         * Used to pre-fill UI forms with globally configured notifiers.
         *
         * @return a list of Lark notifier configurations initialized from global settings
         */
        public List<LarkNotifierConfig> getDefaultNotifierConfigs() {
            return LarkGlobalConfig.getInstance().getRobotConfigs()
                    .stream()
                    .map(LarkNotifierConfig::new)
                    .collect(Collectors.toList());
        }
    }
}
