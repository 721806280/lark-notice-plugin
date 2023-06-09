package io.jenkins.plugins.feishu.notification;

import hudson.EnvVars;
import hudson.Extension;
import io.jenkins.plugins.feishu.notification.context.PipelineEnvContext;
import lombok.extern.slf4j.Slf4j;
import org.jenkinsci.plugins.workflow.flow.StepListener;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.springframework.lang.NonNull;

/**
 * 用于监听 Step 的执行过程，确保在执行 Step 之前将 Pipeline 环境变量与当前环境变量进行合并。
 *
 * @author xm.z
 */
@Slf4j
@Extension
public class FeiShuTalkStepListener implements StepListener {

    /**
     * 该方法会在一个新的 Step 开始之前被调用，将 Pipeline 环境变量与当前环境变量进行合并。
     *
     * @param step    新的 Step 实例。
     * @param context Step 上下文信息。
     */
    @Override
    public void notifyOfNewStep(@NonNull Step step, @NonNull StepContext context) {
        try {
            EnvVars vars = context.get(EnvVars.class);
            PipelineEnvContext.merge(vars);
        } catch (Exception e) {
            log.error("飞书插件在获取 pipeline 中的环境变量时异常", e);
        }
    }

}