package io.jenkins.plugins.lark.notice;

import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.TaskListener;
import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import io.jenkins.plugins.lark.notice.config.LarkNotifier;
import io.jenkins.plugins.lark.notice.config.LarkNotifierConfig;
import io.jenkins.plugins.lark.notice.config.LarkRobotConfig;
import io.jenkins.plugins.lark.notice.config.property.LarkJobProperty;
import io.jenkins.plugins.lark.notice.sdk.MessageDispatcher;
import io.jenkins.plugins.lark.notice.sdk.MessageSenderRegistry;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for notifier behavior and job configuration compatibility.
 */
public class NotifierIntegrationTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Before
    public void setUp() {
        LarkGlobalConfig.getInstance().setRobotConfigs(new ArrayList<>());
        MessageSenderRegistry.getInstance().clear();
    }

    @Test
    public void freestyleNotifierConfigRoundTripShouldNotFailAfterDispatcherCacheWarmed() throws Exception {
        LarkRobotConfig robot = createRobot("robot-1");
        LarkGlobalConfig.getInstance().setRobotConfigs(new ArrayList<>(List.of(robot)));

        // Warm sender cache to simulate real runtime state before config submit.
        MessageDispatcher.getInstance().send(TaskListener.NULL, robot.getId(), null);

        FreeStyleProject project = jenkins.createFreeStyleProject("roundtrip");
        LarkNotifierConfig notifierConfig = createEnabledNotifierConfig(robot.getId(), "START");
        project.getPublishersList().add(new LarkNotifier(List.of(notifierConfig)));

        jenkins.configRoundtrip(project);

        assertNotNull(project.getPublishersList().get(LarkNotifier.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void runListenerShouldSkipWhenFreestylePostBuildNotifierExists() throws Exception {
        LarkRobotConfig robot = createRobot("robot-2");
        LarkGlobalConfig.getInstance().setRobotConfigs(new ArrayList<>(List.of(robot)));

        FreeStyleProject project = jenkins.createFreeStyleProject("dedup");
        project.addProperty(new LarkJobProperty(List.of(createEnabledNotifierConfig(robot.getId(), "START"))));

        LarkRunListener runListener = new LarkRunListener();
        Method method = LarkRunListener.class.getDeclaredMethod("getAvailableLarkNotifierConfigs", Job.class);
        method.setAccessible(true);

        List<LarkNotifierConfig> beforePublisher =
                (List<LarkNotifierConfig>) method.invoke(runListener, project);
        assertEquals(1, beforePublisher.size());

        project.getPublishersList().add(new LarkNotifier(List.of(createEnabledNotifierConfig(robot.getId(), "START"))));
        List<LarkNotifierConfig> afterPublisher =
                (List<LarkNotifierConfig>) method.invoke(runListener, project);
        assertTrue(afterPublisher.isEmpty());
    }

    private static LarkRobotConfig createRobot(String id) {
        return new LarkRobotConfig(
                id,
                "Robot-" + id,
                "https://open.feishu.cn/open-apis/bot/v2/hook/" + id,
                List.of()
        );
    }

    private static LarkNotifierConfig createEnabledNotifierConfig(String robotId, String occasion) {
        return new LarkNotifierConfig(
                false,
                false,
                true,
                robotId,
                "Robot-" + robotId,
                false,
                "",
                "title",
                "content",
                "",
                Set.of(occasion)
        );
    }
}
