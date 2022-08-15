package io.jenkins.plugins;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import io.jenkins.plugins.FeiShuTalkNotifierConfig.FeiShuTalkNotifierConfigDescriptor;
import jenkins.model.Jenkins;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务配置页面添加飞书配置
 *
 * @author xm.z
 */
@ToString
@NoArgsConstructor
public class FeiShuTalkJobProperty extends JobProperty<Job<?, ?>> {

    private ArrayList<FeiShuTalkNotifierConfig> notifierConfigs;

    @DataBoundConstructor
    public FeiShuTalkJobProperty(ArrayList<FeiShuTalkNotifierConfig> notifierConfigs) {
        this.notifierConfigs = notifierConfigs;
    }

    /**
     * 在配置页面展示的列表，需要跟 `全局配置` 同步机器人信息
     *
     * @return 机器人配置列表
     */
    public ArrayList<FeiShuTalkNotifierConfig> getNotifierConfigs() {

        ArrayList<FeiShuTalkNotifierConfig> notifierConfigsList = new ArrayList<>();
        ArrayList<FeiShuTalkRobotConfig> robotConfigs = FeiShuTalkGlobalConfig.getInstance().getRobotConfigs();

        for (FeiShuTalkRobotConfig robotConfig : robotConfigs) {
            String id = robotConfig.getId();
            FeiShuTalkNotifierConfig newNotifierConfig = new FeiShuTalkNotifierConfig(robotConfig);

            if (notifierConfigs != null && !notifierConfigs.isEmpty()) {
                for (FeiShuTalkNotifierConfig notifierConfig : notifierConfigs) {
                    String robotId = notifierConfig.getRobotId();
                    if (id.equals(robotId)) {
                        newNotifierConfig.copy(notifierConfig);
                    }
                }
            }

            notifierConfigsList.add(newNotifierConfig);
        }

        return notifierConfigsList;
    }

    /**
     * 获取用户设置的通知配置
     *
     * @return 用户设置的通知配置
     */
    public List<FeiShuTalkNotifierConfig> getCheckedNotifierConfigs() {
        ArrayList<FeiShuTalkNotifierConfig> notifierConfigs = this.getNotifierConfigs();

        return notifierConfigs.stream().filter(FeiShuTalkNotifierConfig::isChecked).collect(Collectors.toList());
    }

    @Extension
    public static class FeiShuTalkJobPropertyDescriptor extends JobPropertyDescriptor {

        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            return super.isApplicable(jobType);
        }

        /**
         * 通知配置页面
         */
        public FeiShuTalkNotifierConfigDescriptor getFeiShuTalkNotifierConfigDescriptor() {
            return Jenkins.get().getDescriptorByType(FeiShuTalkNotifierConfigDescriptor.class);
        }

        /**
         * 默认的配置项列表
         *
         * @return 默认的通知配置列表
         */
        public List<FeiShuTalkNotifierConfig> getDefaultNotifierConfigs() {
            return FeiShuTalkGlobalConfig.getInstance().getRobotConfigs().stream().map(FeiShuTalkNotifierConfig::new)
                    .collect(Collectors.toList());
        }
    }
}
