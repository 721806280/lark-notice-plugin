package io.jenkins.plugins.lark.notice.config.property;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import io.jenkins.plugins.lark.notice.config.LarkNotifierConfig;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.collections.CollectionUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * LarkJobProperty is a job property that stores configurations for sending notifications via Lark
 * for specific Jenkins jobs. This allows each job to have its own set of Lark notifier configurations.
 *
 * @author xm.z
 */
@ToString
@NoArgsConstructor
public class LarkJobProperty extends JobProperty<Job<?, ?>> {

    private List<LarkNotifierConfig> notifierConfigs;

    /**
     * Data-bound constructor for setting up Lark notifier configurations.
     *
     * @param notifierConfigs A list of LarkNotifierConfig objects representing the configurations for Lark notifications.
     */
    @DataBoundConstructor
    public LarkJobProperty(List<LarkNotifierConfig> notifierConfigs) {
        this.notifierConfigs = notifierConfigs;
    }

    /**
     * Retrieves the list of Lark notifier configurations, merging the global configurations with any job-specific overrides.
     *
     * @return A list of LarkNotifierConfig objects.
     */
    public List<LarkNotifierConfig> getNotifierConfigs() {
        return LarkGlobalConfig.getInstance().getRobotConfigs()
                .stream()
                .map(robotConfig -> {
                    LarkNotifierConfig newNotifierConfig = new LarkNotifierConfig(robotConfig);
                    if (CollectionUtils.isNotEmpty(notifierConfigs)) {
                        notifierConfigs.stream()
                                .filter(notifierConfig -> robotConfig.getId().equals(notifierConfig.getRobotId()))
                                .findFirst()
                                .ifPresent(newNotifierConfig::copy);
                    }
                    return newNotifierConfig;
                })
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the list of Lark notifier configurations that are marked as checked/enabled by the user.
     *
     * @return A filtered list of LarkNotifierConfig objects that are enabled.
     */
    public List<LarkNotifierConfig> getCheckedNotifierConfigs() {
        return this.getNotifierConfigs().stream()
                .filter(LarkNotifierConfig::isChecked).collect(Collectors.toList());
    }

    /**
     * Retrieves the list of available (checked and not disabled) Lark notifier configurations.
     *
     * @return A filtered list of LarkNotifierConfig objects that are available for use.
     */
    public List<LarkNotifierConfig> getAvailableNotifierConfigs() {
        return this.getNotifierConfigs().stream()
                .filter(config -> config.isChecked() && !config.isDisabled())
                .collect(Collectors.toList());
    }

    /**
     * Descriptor for {@link LarkJobProperty}. This descriptor is used by Jenkins to manage the job property,
     * including presenting configuration options in the UI and handling data binding.
     */
    @Extension
    public static class LarkJobPropertyDescriptor extends JobPropertyDescriptor {

        /**
         * Determines whether this job property is applicable to the given job type.
         *
         * @param jobType The class of the job to check.
         * @return true if the job property is applicable, false otherwise.
         */
        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            // By default, applicable to all job types. Override if specific job types should be excluded.
            return super.isApplicable(jobType);
        }

        /**
         * Provides a default list of Lark notifier configurations, based on the global settings.
         *
         * @return A list of default LarkNotifierConfig objects.
         */
        public List<LarkNotifierConfig> getDefaultNotifierConfigs() {
            return LarkGlobalConfig.getInstance().getRobotConfigs().stream().map(LarkNotifierConfig::new)
                    .collect(Collectors.toList());
        }
    }
}
