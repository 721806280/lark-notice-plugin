package io.jenkins.plugins.lark.notice.config;

import io.jenkins.plugins.lark.notice.config.property.LarkBranchJobProperty;
import io.jenkins.plugins.lark.notice.config.property.LarkJobProperty;
import io.jenkins.plugins.lark.notice.enums.SecurityPolicyEnum;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests for notifier config list copy semantics.
 *
 * @author xm.z
 */
public class NotifierConfigListUtilsTest {

    @Test
    public void copyOrNullShouldKeepNull() {
        assertNull(NotifierConfigListUtils.copyOrNull(null));
    }

    @Test
    public void constructorsShouldDefensivelyCopyNotifierConfigs() {
        List<LarkNotifierConfig> source = new ArrayList<>();
        source.add(createNotifierConfig("robot-a"));

        LarkNotifier notifier = new LarkNotifier(source);
        LarkJobProperty jobProperty = new LarkJobProperty(source);
        LarkBranchJobProperty branchProperty = new LarkBranchJobProperty(source);

        source.clear();

        assertNotNull(notifier.getLarkNotifierConfigs());
        assertNotNull(jobProperty.getLarkNotifierConfigs());
        assertNotNull(branchProperty.getLarkNotifierConfigs());
        assertEquals(1, notifier.getLarkNotifierConfigs().size());
        assertEquals(1, jobProperty.getLarkNotifierConfigs().size());
        assertEquals(1, branchProperty.getLarkNotifierConfigs().size());
    }

    @Test
    public void robotConfigConstructorShouldDefensivelyCopySecurityPolicies() {
        List<LarkSecurityPolicyConfig> source = new ArrayList<>();
        source.add(new LarkSecurityPolicyConfig(SecurityPolicyEnum.NO_SSL.name(), "true", "No SSL"));

        LarkRobotConfig robotConfig = new LarkRobotConfig(
                "robot-a",
                "Robot-robot-a",
                "https://open.feishu.cn/open-apis/bot/v2/hook/robot-a",
                source
        );

        source.clear();

        assertEquals(
                "true",
                robotConfig.getSecurityPolicyConfigs().stream()
                        .filter(config -> SecurityPolicyEnum.NO_SSL.name().equals(config.getType()))
                        .findFirst()
                        .orElseThrow()
                        .getValue()
        );
    }

    private static LarkNotifierConfig createNotifierConfig(String robotId) {
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
                Set.of("START")
        );
    }
}
