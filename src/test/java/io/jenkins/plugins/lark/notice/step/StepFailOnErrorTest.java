package io.jenkins.plugins.lark.notice.step;

import hudson.model.Result;
import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import io.jenkins.plugins.lark.notice.config.LarkRobotConfig;
import io.jenkins.plugins.lark.notice.enums.RobotProtocolType;
import io.jenkins.plugins.lark.notice.enums.WebhookEndpointMode;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Verifies that the pipeline step {@code failOnError} parameter controls whether a send failure fails the build.
 *
 * @author xm.z
 */
public class StepFailOnErrorTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Before
    public void setUp() {
        LarkRobotConfig robot = new LarkRobotConfig(
                "robot-unreachable", "Unreachable", "http://127.0.0.1:1/open-apis/bot/v2/hook/x", List.of());
        robot.setProtocolType(RobotProtocolType.LARK_COMPATIBLE);
        robot.setEndpointMode(WebhookEndpointMode.FULL_WEBHOOK);
        LarkGlobalConfig.getInstance().setRobotConfigs(new ArrayList<>(List.of(robot)));
    }

    @Test
    public void stepShouldFailBuildByDefaultWhenSendFails() throws Exception {
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "lark-fail-default");
        job.setDefinition(new CpsFlowDefinition(
                "lark robot: 'robot-unreachable', type: 'TEXT', text: ['hi']", true));
        jenkins.buildAndAssertStatus(Result.FAILURE, job);
    }

    @Test
    public void stepShouldKeepBuildGreenWhenFailOnErrorIsFalse() throws Exception {
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "lark-keep-green");
        job.setDefinition(new CpsFlowDefinition(
                "lark robot: 'robot-unreachable', type: 'TEXT', text: ['hi'], failOnError: false", true));
        jenkins.buildAndAssertSuccess(job);
    }
}
