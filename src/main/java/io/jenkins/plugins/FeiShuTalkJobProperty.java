package io.jenkins.plugins;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 添加到 Jenkins 任务配置页面的飞书通知配置。
 *
 * <p>FeiShuTalkJobProperty 类用于在 Jenkins 任务配置页面中添加飞书通知配置，包括选择机器人、开启通知、
 * 选择通知场景等功能。</p>
 *
 * <p>FeiShuTalkJobProperty 继承 JobProperty 类，表示该配置信息应用于 Jenkins 中所有任务。</p>
 *
 * <p>FeiShuTalkJobProperty 中包含多个 FeiShuTalkNotifierConfig，表示该任务需要使用哪些机器人进行通知，
 * 并且可以单独针对每个机器人进行通知场景的选择和开关。</p>
 *
 * @author xm.z
 */
@ToString
@NoArgsConstructor
public class FeiShuTalkJobProperty extends JobProperty<Job<?, ?>> {

    /**
     * 任务配置页面中展示的机器人和通知配置信息列表。
     */
    private List<FeiShuTalkNotifierConfig> notifierConfigs;

    /**
     * FeiShuTalkJobProperty 的构造函数，用于创建实例并初始化各属性的值。
     *
     * @param notifierConfigs 任务配置页面中展示的机器人和通知配置信息列表。
     */
    @DataBoundConstructor
    public FeiShuTalkJobProperty(List<FeiShuTalkNotifierConfig> notifierConfigs) {
        this.notifierConfigs = notifierConfigs;
    }

    /**
     * 获取任务配置页面中展示的机器人和通知配置信息列表。该列表需要跟全局配置中的机器人信息同步。
     *
     * @return 任务配置页面中展示的机器人和通知配置信息列表。
     */
    public List<FeiShuTalkNotifierConfig> getNotifierConfigs() {
        return FeiShuTalkGlobalConfig.getInstance().getRobotConfigs()
                .stream()
                .map(robotConfig -> {
                    FeiShuTalkNotifierConfig newNotifierConfig = new FeiShuTalkNotifierConfig(robotConfig);
                    notifierConfigs.stream()
                            .filter(notifierConfig -> robotConfig.getId().equals(notifierConfig.getRobotId()))
                            .findFirst()
                            .ifPresent(newNotifierConfig::copy);
                    return newNotifierConfig;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取用户设置的通知配置。
     *
     * @return 用户设置的通知配置。
     */
    public List<FeiShuTalkNotifierConfig> getCheckedNotifierConfigs() {
        return this.getNotifierConfigs().stream()
                .filter(FeiShuTalkNotifierConfig::isChecked).collect(Collectors.toList());
    }

    /**
     * 获取启用的通知配置列表。
     *
     * @return 启用的通知配置列表。
     */
    public List<FeiShuTalkNotifierConfig> getAvailableNotifierConfigs() {
        return this.getNotifierConfigs().stream()
                .filter(config -> config.isChecked() && !config.isDisabled())
                .collect(Collectors.toList());
    }

    /**
     * 表示 FeiShuTalkJobProperty 的描述器。
     *
     * <p>FeiShuTalkJobPropertyDescriptor 用于提供描述器，表示该配置信息需要展示在 Jenkins 系统管理中心的那个
     * 页面，并提供默认的通知配置列表。</p>
     */
    @Extension
    public static class FeiShuTalkJobPropertyDescriptor extends JobPropertyDescriptor {

        /**
         * 判断 FeiShuTalkJobProperty 是否适用于指定类型的任务。
         *
         * @param jobType 要判断的任务类型。
         * @return true 如果该配置适用于指定类型的任务，false 否则。
         */
        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            return super.isApplicable(jobType);
        }

        /**
         * 获取默认的通知配置列表。默认列表中包含所有全局配置中的机器人。
         *
         * @return 默认的通知配置列表。
         */
        public List<FeiShuTalkNotifierConfig> getDefaultNotifierConfigs() {
            return FeiShuTalkGlobalConfig.getInstance().getRobotConfigs().stream().map(FeiShuTalkNotifierConfig::new)
                    .collect(Collectors.toList());
        }
    }
}