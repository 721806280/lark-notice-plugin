package io.jenkins.plugins.lark.notice.service;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import io.jenkins.plugins.lark.notice.config.LarkNotifier;
import io.jenkins.plugins.lark.notice.config.LarkNotifierConfig;
import io.jenkins.plugins.lark.notice.config.LarkRobotConfig;
import io.jenkins.plugins.lark.notice.config.property.LarkJobProperty;
import io.jenkins.plugins.lark.notice.config.property.LarkNotifierProvider;
import io.jenkins.plugins.lark.notice.context.PipelineEnvContext;
import io.jenkins.plugins.lark.notice.enums.BuildStatusEnum;
import io.jenkins.plugins.lark.notice.enums.NoticeOccasionEnum;
import io.jenkins.plugins.lark.notice.sdk.MessageDispatcher;
import io.jenkins.plugins.lark.notice.sdk.MessageSenderRegistry;
import io.jenkins.plugins.lark.notice.sdk.model.SendResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for service-layer components introduced by notification refactoring.
 *
 * @author xm.z
 */
public class NotificationServiceLayerTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Before
    public void setUp() {
        LarkGlobalConfig.getInstance().setRobotConfigs(new ArrayList<>());
        MessageSenderRegistry.getInstance().clear();
        PipelineEnvContext.reset();
    }

    @Test
    public void providerNamedMethodsShouldReturnExpectedConfigs() {
        LarkRobotConfig robot1 = createRobot("robot-a");
        LarkRobotConfig robot2 = createRobot("robot-b");
        LarkGlobalConfig.getInstance().setRobotConfigs(new ArrayList<>(List.of(robot1, robot2)));

        List<LarkNotifierConfig> localConfigs = List.of(
                createNotifierConfig(robot1.getId(), true, false),
                createNotifierConfig(robot2.getId(), true, true)
        );
        LarkNotifierProvider provider = new TestNotifierProvider(localConfigs);

        assertEquals(2, provider.getMergedNotifierConfigs().size());
        assertEquals(2, provider.getEnabledNotifierConfigs().size());
        assertEquals(1, provider.getAvailableNotifierConfigs().size());
        assertEquals(robot1.getId(), provider.getAvailableNotifierConfigs().get(0).getRobotId());
    }

    @Test
    public void providerMergedConfigsShouldPreferFirstLocalConfigWhenRobotIdDuplicated() {
        LarkRobotConfig robot = createRobot("robot-dup");
        LarkGlobalConfig.getInstance().setRobotConfigs(new ArrayList<>(List.of(robot)));

        LarkNotifierConfig first = createNotifierConfig(robot.getId(), true, false);
        first.setTitle("first");
        LarkNotifierConfig second = createNotifierConfig(robot.getId(), false, true);
        second.setTitle("second");

        LarkNotifierProvider provider = new TestNotifierProvider(List.of(first, second));
        List<LarkNotifierConfig> merged = provider.getMergedNotifierConfigs();

        assertEquals(1, merged.size());
        assertEquals("first", merged.get(0).getTitle());
        assertTrue(merged.get(0).isChecked());
        assertFalse(merged.get(0).isDisabled());
    }

    @Test
    public void notifierConfigResolverShouldSkipWhenFreestylePostBuildNotifierExists() throws Exception {
        LarkRobotConfig robot = createRobot("robot-resolver");
        LarkGlobalConfig.getInstance().setRobotConfigs(new ArrayList<>(List.of(robot)));

        FreeStyleProject project = jenkins.createFreeStyleProject("resolver");
        project.addProperty(new LarkJobProperty(List.of(createNotifierConfig(robot.getId(), true, false))));

        List<LarkNotifierConfig> beforePublisher = NotifierConfigResolver.resolveForRunListener(project);
        assertEquals(1, beforePublisher.size());

        project.getPublishersList().add(new LarkNotifier(List.of(createNotifierConfig(robot.getId(), true, false))));
        List<LarkNotifierConfig> afterPublisher = NotifierConfigResolver.resolveForRunListener(project);
        assertTrue(afterPublisher.isEmpty());
    }

    @Test
    public void buildNotificationContextFactoryShouldCreateContextWithExpectedFields() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("context-factory");
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);

        BuildNotificationContext context = BuildNotificationContextFactory.create(
                build, TaskListener.NULL, NoticeOccasionEnum.SUCCESS);

        assertEquals(project.getFullDisplayName(), context.model().getProjectName());
        assertEquals(BuildStatusEnum.SUCCESS, context.model().getStatusType());
        assertEquals(project.getFullDisplayName(), context.envVars().get("PROJECT_NAME"));
        assertTrue(context.envVars().containsKey("JOB_URL"));
    }

    @Test
    public void orchestratorShouldResetPipelineEnvContextWhenNoConfigs() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("orchestrator-reset");
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);

        EnvVars envVars = new EnvVars();
        envVars.put("TEMP_FLAG", "1");
        PipelineEnvContext.merge(envVars);
        assertTrue(PipelineEnvContext.get().containsKey("TEMP_FLAG"));

        NotificationOrchestrator.notify(
                "test-source",
                build,
                TaskListener.NULL,
                NoticeOccasionEnum.SUCCESS,
                List.of(),
                MessageDispatcher.getInstance()
        );

        assertFalse(PipelineEnvContext.get().containsKey("TEMP_FLAG"));
        assertEquals(Result.SUCCESS, build.getResult());
    }

    @Test
    public void dispatchExecutorShouldMarkBuildFailureWhenSendResultIsNotOk() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("dispatch-failure");
        project.getBuildersList().add(new FailureResultBuilder());

        FreeStyleBuild build = jenkins.buildAndAssertStatus(Result.FAILURE, project);

        assertEquals(Result.FAILURE, build.getResult());
    }

    private static LarkRobotConfig createRobot(String id) {
        return new LarkRobotConfig(
                id,
                "Robot-" + id,
                "https://open.feishu.cn/open-apis/bot/v2/hook/" + id,
                List.of()
        );
    }

    private static LarkNotifierConfig createNotifierConfig(String robotId, boolean checked, boolean disabled) {
        return new LarkNotifierConfig(
                false,
                disabled,
                checked,
                robotId,
                "Robot-" + robotId,
                false,
                "",
                "title",
                "content",
                "",
                Set.of("START", "SUCCESS")
        );
    }

    private static final class TestNotifierProvider implements LarkNotifierProvider {

        private final List<LarkNotifierConfig> configs;

        private TestNotifierProvider(List<LarkNotifierConfig> configs) {
            this.configs = configs;
        }

        @Override
        public List<LarkNotifierConfig> getLarkNotifierConfigs() {
            return configs;
        }
    }

    private static final class FailureResultBuilder extends Builder {

        @Override
        public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
            NotificationDispatchExecutor.handleSendResult(
                    "test-source",
                    build,
                    listener,
                    NoticeOccasionEnum.SUCCESS,
                    "robot-fail",
                    SendResult.fail("mock failed"));
            return true;
        }
    }
}
