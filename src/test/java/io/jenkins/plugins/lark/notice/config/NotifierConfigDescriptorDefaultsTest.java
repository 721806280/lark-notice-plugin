package io.jenkins.plugins.lark.notice.config;

import io.jenkins.plugins.lark.notice.config.property.LarkBranchJobProperty;
import io.jenkins.plugins.lark.notice.config.property.LarkJobProperty;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

/**
 * Tests for default notifier config lists exposed by Jenkins descriptors.
 *
 * @author xm.z
 */
public class NotifierConfigDescriptorDefaultsTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Before
    public void setUp() {
        LarkGlobalConfig.getInstance().setRobotConfigs(new ArrayList<>());
    }

    @Test
    public void defaultNotifierConfigsShouldMirrorGlobalRobotsAndRemainDetached() {
        LarkRobotConfig robotA = createRobot("robot-a");
        LarkRobotConfig robotB = createRobot("robot-b");
        LarkGlobalConfig.getInstance().setRobotConfigs(new ArrayList<>(List.of(robotA, robotB)));

        List<LarkNotifierConfig> notifierDefaults =
                jenkins.jenkins.getDescriptorByType(LarkNotifier.DescriptorImpl.class).getDefaultNotifierConfigs();
        List<LarkNotifierConfig> jobDefaults =
                jenkins.jenkins.getDescriptorByType(LarkJobProperty.DescriptorImpl.class).getDefaultNotifierConfigs();
        List<LarkNotifierConfig> branchDefaults =
                jenkins.jenkins.getDescriptorByType(LarkBranchJobProperty.DescriptorImpl.class).getDefaultNotifierConfigs();

        assertEquals(List.of("robot-a", "robot-b"), extractRobotIds(notifierDefaults));
        assertEquals(List.of("robot-a", "robot-b"), extractRobotIds(jobDefaults));
        assertEquals(List.of("robot-a", "robot-b"), extractRobotIds(branchDefaults));

        assertNotSame(notifierDefaults, jobDefaults);
        assertNotSame(jobDefaults, branchDefaults);

        notifierDefaults.get(0).setTitle("custom-title");
        assertNull(jobDefaults.get(0).getTitle());
        assertEquals("Robot-robot-a", robotA.getName());
    }

    @Test
    public void mergeAndFilterHelpersShouldPreserveCurrentNotifierSemantics() {
        LarkRobotConfig robotA = createRobot("robot-a");
        LarkRobotConfig robotB = createRobot("robot-b");
        LarkGlobalConfig.getInstance().setRobotConfigs(new ArrayList<>(List.of(robotA, robotB)));

        LarkNotifierConfig firstLocal = createNotifierConfig(robotA.getId(), true, false, "first");
        LarkNotifierConfig duplicateLocal = createNotifierConfig(robotA.getId(), false, true, "second");
        LarkNotifierConfig disabledLocal = createNotifierConfig(robotB.getId(), true, true, "disabled");

        List<LarkNotifierConfig> merged = NotifierConfigListUtils.mergeWithGlobalRobots(
                List.of(firstLocal, duplicateLocal, disabledLocal)
        );

        assertEquals(List.of("robot-a", "robot-b"), extractRobotIds(merged));
        assertEquals("first", merged.get(0).getTitle());
        assertEquals("disabled", merged.get(1).getTitle());
        assertEquals(2, NotifierConfigListUtils.filterEnabled(merged).size());
        assertEquals(List.of("robot-a"), extractRobotIds(NotifierConfigListUtils.filterAvailable(merged)));
    }

    private static LarkRobotConfig createRobot(String id) {
        return new LarkRobotConfig(
                id,
                "Robot-" + id,
                "https://open.feishu.cn/open-apis/bot/v2/hook/" + id,
                List.of()
        );
    }

    private static LarkNotifierConfig createNotifierConfig(String robotId, boolean checked, boolean disabled, String title) {
        return new LarkNotifierConfig(
                false,
                disabled,
                checked,
                robotId,
                "Robot-" + robotId,
                false,
                "",
                title,
                "content",
                "",
                Set.of("START")
        );
    }

    private static List<String> extractRobotIds(List<LarkNotifierConfig> configs) {
        return configs.stream()
                .map(LarkNotifierConfig::getRobotId)
                .collect(Collectors.toList());
    }
}
