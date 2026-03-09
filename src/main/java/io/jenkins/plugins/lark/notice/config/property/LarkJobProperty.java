package io.jenkins.plugins.lark.notice.config.property;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.config.LarkNotifierConfig;
import io.jenkins.plugins.lark.notice.config.NotifierConfigListUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;

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
        this.larkNotifierConfigs = NotifierConfigListUtils.copyOrNull(notifierConfigs);
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
            return Messages.plugin_name();
        }

        /**
         * Gets the default list of Lark notifier configurations from global settings.
         * <p>
         * Used to pre-fill UI forms with globally configured notifiers.
         *
         * @return a list of Lark notifier configurations initialized from global settings
         */
        public List<LarkNotifierConfig> getDefaultNotifierConfigs() {
            return NotifierConfigListUtils.fromGlobalRobots();
        }
    }
}
