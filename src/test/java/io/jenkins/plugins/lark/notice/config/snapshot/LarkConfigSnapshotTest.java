package io.jenkins.plugins.lark.notice.config.snapshot;

import hudson.model.Descriptor.FormException;
import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import io.jenkins.plugins.lark.notice.config.LarkProxyConfig;
import io.jenkins.plugins.lark.notice.config.LarkRetryConfig;
import io.jenkins.plugins.lark.notice.config.LarkRobotConfig;
import io.jenkins.plugins.lark.notice.config.LarkSecurityPolicyConfig;
import org.junit.Test;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for snapshot import/export mapping and validation.
 *
 * @author xm.z
 */
public class LarkConfigSnapshotTest {

    @Test
    public void snapshotRoundTripShouldPreserveConfiguredValues() {
        LarkGlobalConfig globalConfig = new LarkGlobalConfig(
                createProxy(),
                true,
                Set.of("SUCCESS", "FAILURE"),
                new ArrayList<>(List.of(createRobot("robot-a")))
        );

        LarkConfigSnapshot snapshot = LarkConfigSnapshotMapper.toSnapshot(globalConfig, true);
        LarkConfigSnapshotMapper.ImportedGlobalConfig imported = LarkConfigSnapshotMapper.fromSnapshot(snapshot);

        assertTrue(snapshot.isSecretsIncluded());
        assertEquals(1, snapshot.getRobotConfigs().size());
        assertEquals("https://open.feishu.cn/open-apis/bot/v2/hook/robot-a", snapshot.getRobotConfigs().get(0).getWebhook());

        assertTrue(imported.isVerbose());
        assertEquals(Set.of("SUCCESS", "FAILURE"), imported.getNoticeOccasions());
        assertEquals(1, imported.getRobotConfigs().size());
        assertEquals("robot-a", imported.getRobotConfigs().get(0).getId());
        assertEquals("Robot robot-a", imported.getRobotConfigs().get(0).getName());
        assertEquals("https://open.feishu.cn/open-apis/bot/v2/hook/robot-a", imported.getRobotConfigs().get(0).getWebhook());
        assertTrue(imported.getRobotConfigs().get(0).getRetryConfig().isEnabled());
        assertEquals(Proxy.Type.HTTP, imported.getProxyConfig().getType());
    }

    @Test
    public void exportWithoutSecretsShouldRedactSensitiveValues() {
        LarkConfigSnapshot snapshot = LarkConfigSnapshotMapper.toSnapshot(
                new LarkGlobalConfig(null, false, Set.of("SUCCESS"), new ArrayList<>(List.of(createRobot("robot-b")))),
                false
        );

        RobotSnapshot robotSnapshot = snapshot.getRobotConfigs().get(0);
        String keyValue = robotSnapshot.getSecurityPolicyConfigs().stream()
                .filter(policy -> "KEY".equals(policy.getType()))
                .findFirst()
                .map(SecurityPolicySnapshot::getValue)
                .orElse(null);
        String noSslValue = robotSnapshot.getSecurityPolicyConfigs().stream()
                .filter(policy -> "NO_SSL".equals(policy.getType()))
                .findFirst()
                .map(SecurityPolicySnapshot::getValue)
                .orElse(null);
        assertFalse(snapshot.isSecretsIncluded());
        assertNull(robotSnapshot.getWebhook());
        assertNull(keyValue);
        assertEquals("true", noSslValue);
    }

    @Test
    public void validatorShouldRejectSnapshotsWithoutSecrets() {
        LarkConfigSnapshot snapshot = LarkConfigSnapshotMapper.toSnapshot(
                new LarkGlobalConfig(null, false, Set.of("SUCCESS"), new ArrayList<>(List.of(createRobot("robot-c")))),
                false
        );

        try {
            LarkConfigSnapshotValidator.validateForImport(snapshot);
        } catch (FormException ex) {
            assertTrue(ex.getMessage().contains("secrets"));
            return;
        }
        throw new AssertionError("Expected import validation to fail for redacted snapshots");
    }

    private static LarkProxyConfig createProxy() {
        LarkProxyConfig proxyConfig = new LarkProxyConfig();
        proxyConfig.setEnabled(true);
        proxyConfig.setType(Proxy.Type.HTTP);
        proxyConfig.setHost("proxy.internal");
        proxyConfig.setPort(8080);
        return proxyConfig;
    }

    private static LarkRobotConfig createRobot(String id) {
        LarkRobotConfig robotConfig = new LarkRobotConfig(
                id,
                "Robot " + id,
                "https://open.feishu.cn/open-apis/bot/v2/hook/" + id,
                List.of(
                        new LarkSecurityPolicyConfig("KEY", "keyword-" + id, "Keyword"),
                        new LarkSecurityPolicyConfig("NO_SSL", "true", "No SSL")
                )
        );
        robotConfig.setRetryConfig(new LarkRetryConfig(true, 3, 500, 5000, 2.0d, 0.2d));
        return robotConfig;
    }
}
