package io.jenkins.plugins.lark.notice.config.property;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import io.jenkins.plugins.lark.notice.config.LarkNotifierConfig;
import jenkins.branch.BranchProperty;
import jenkins.branch.BranchPropertyDescriptor;
import jenkins.branch.JobDecorator;
import jenkins.branch.MultiBranchProjectDescriptor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A branch-level property that provides Lark notification configurations.
 * <p>
 * This class allows users to define custom Lark notifier settings at the branch level,
 * which can override global or job-level defaults.
 *
 * @author xm.z
 */
@ToString
@NoArgsConstructor
public class LarkBranchJobProperty extends BranchProperty implements LarkNotifierProvider {

    /**
     * List of Lark notifier configurations specific to this branch.
     * <p>
     * These configurations will be merged with global settings to determine
     * which Lark robots should be triggered for notifications.
     */
    @Getter
    private List<LarkNotifierConfig> larkNotifierConfigs;

    /**
     * Constructs a new instance of {@link LarkBranchJobProperty}.
     * <p>
     * This constructor is typically used by Jenkins UI and configuration system
     * to initialize branch-level Lark notification settings.
     *
     * @param notifierConfigs A list of Lark notifier configurations defined at the branch level.
     *                        If null or empty, only global configurations will be used.
     */
    @DataBoundConstructor
    public LarkBranchJobProperty(List<LarkNotifierConfig> notifierConfigs) {
        this.larkNotifierConfigs = notifierConfigs;
    }

    /**
     * Provides a job decorator for applying Lark notifications during job execution.
     * <p>
     * Currently returns null as no additional behavior is required during job decoration.
     *
     * @param clazz The job class type.
     * @return always null (no decoration logic applied)
     */
    @Override
    public <P extends Job<P, B>, B extends Run<P, B>> JobDecorator<P, B> jobDecorator(Class<P> clazz) {
        return null;
    }

    /**
     * Descriptor for {@link LarkBranchJobProperty}.
     * <p>
     * Provides metadata and UI-related behavior for configuring Lark notifications at the branch level.
     */
    @Extension
    public static class DescriptorImpl extends BranchPropertyDescriptor {

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
         * Determines if this property is applicable to the given multi-branch project descriptor.
         * <p>
         * Delegates to the parent implementation for standard applicability checks.
         *
         * @param projectDescriptor the descriptor of the multi-branch project
         * @return true if this property is applicable; false otherwise
         */
        @Override
        protected boolean isApplicable(@NonNull MultiBranchProjectDescriptor projectDescriptor) {
            return super.isApplicable(projectDescriptor);
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