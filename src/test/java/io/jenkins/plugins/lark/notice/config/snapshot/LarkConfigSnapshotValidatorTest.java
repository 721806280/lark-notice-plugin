package io.jenkins.plugins.lark.notice.config.snapshot;

import hudson.model.Descriptor.FormException;
import io.jenkins.plugins.lark.notice.enums.RobotProtocolType;
import io.jenkins.plugins.lark.notice.enums.WebhookEndpointMode;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for import validation rules on configuration snapshots.
 */
public class LarkConfigSnapshotValidatorTest {

    @Test
    public void validateForImportShouldRejectNullSnapshot() {
        try {
            LarkConfigSnapshotValidator.validateForImport(null);
            fail("Expected FormException");
        } catch (FormException e) {
            assertEquals("payload", e.getFormField());
        }
    }

    @Test
    public void validateForImportShouldRejectUnsupportedSchemaVersion() {
        LarkConfigSnapshot snapshot = createValidSnapshot();
        snapshot.setSchemaVersion(LarkConfigSnapshot.CURRENT_SCHEMA_VERSION + 1);

        try {
            LarkConfigSnapshotValidator.validateForImport(snapshot);
            fail("Expected FormException");
        } catch (FormException e) {
            assertEquals("payload", e.getFormField());
        }
    }

    @Test
    public void validateForImportShouldRejectNewerPluginVersion() {
        LarkConfigSnapshot snapshot = createValidSnapshot();
        snapshot.setPluginVersion("2.1.0");

        try {
            LarkConfigSnapshotValidator.validateForImport(snapshot, "2.0.0");
            fail("Expected FormException");
        } catch (FormException e) {
            assertEquals("payload", e.getFormField());
        }
    }

    @Test
    public void validateForImportShouldRejectInvalidNoticeOccasion() {
        LarkConfigSnapshot snapshot = createValidSnapshot();
        snapshot.setNoticeOccasions(Set.of("UNKNOWN"));

        try {
            LarkConfigSnapshotValidator.validateForImport(snapshot);
            fail("Expected FormException");
        } catch (FormException e) {
            assertEquals("payload", e.getFormField());
        }
    }

    @Test
    public void validateForImportShouldRejectInvalidProxyConfig() {
        LarkConfigSnapshot snapshot = createValidSnapshot();
        ProxySnapshot proxySnapshot = new ProxySnapshot();
        proxySnapshot.setEnabled(true);
        proxySnapshot.setType("HTTP");
        proxySnapshot.setHost(" ");
        proxySnapshot.setPort(0);
        snapshot.setProxyConfig(proxySnapshot);

        try {
            LarkConfigSnapshotValidator.validateForImport(snapshot);
            fail("Expected FormException");
        } catch (FormException e) {
            assertEquals("payload", e.getFormField());
        }
    }

    @Test
    public void validateForImportShouldRejectInvalidRobotWebhookHost() {
        LarkConfigSnapshot snapshot = createValidSnapshot();
        snapshot.getRobotConfigs().get(0).setWebhook("https://example.com/webhook/robot-a");

        try {
            LarkConfigSnapshotValidator.validateForImport(snapshot);
            fail("Expected FormException");
        } catch (FormException e) {
            assertEquals("payload", e.getFormField());
        }
    }

    @Test
    public void validateForImportShouldAcceptCustomFeishuWebhookHost() throws Exception {
        LarkConfigSnapshot snapshot = createValidSnapshot();
        snapshot.getRobotConfigs().get(0).setWebhook("https://feishu.example.com/open-apis/bot/v2/hook/robot-a");
        snapshot.getRobotConfigs().get(0).setProtocolType(RobotProtocolType.LARK_COMPATIBLE);
        snapshot.getRobotConfigs().get(0).setEndpointMode(WebhookEndpointMode.BASE_URL_AND_TOKEN);

        LarkConfigSnapshotValidator.validateForImport(snapshot);
    }

    @Test
    public void validateForImportShouldAcceptValidSnapshot() throws Exception {
        LarkConfigSnapshotValidator.validateForImport(createValidSnapshot());
    }

    @Test
    public void validateForImportShouldAcceptOlderPluginVersion() throws Exception {
        LarkConfigSnapshot snapshot = createValidSnapshot();
        snapshot.setSchemaVersion(1);
        snapshot.setPluginVersion("1.9.9");

        LarkConfigSnapshotValidator.validateForImport(snapshot, "2.0.0");
    }

    @Test
    public void validateForImportShouldRejectInvalidRetryConfig() {
        LarkConfigSnapshot snapshot = createValidSnapshot();
        RetrySnapshot retrySnapshot = new RetrySnapshot();
        retrySnapshot.setEnabled(true);
        retrySnapshot.setMaxAttempts(0);
        retrySnapshot.setInitialDelayMs(100);
        retrySnapshot.setMaxDelayMs(200);
        retrySnapshot.setBackoffMultiplier(1.0d);
        retrySnapshot.setJitterRatio(0.1d);
        snapshot.getRobotConfigs().get(0).setRetryConfig(retrySnapshot);

        try {
            LarkConfigSnapshotValidator.validateForImport(snapshot);
            fail("Expected FormException");
        } catch (FormException e) {
            assertEquals("payload", e.getFormField());
        }
    }

    @Test
    public void validateForImportShouldRejectDuplicateSecurityPolicies() {
        LarkConfigSnapshot snapshot = createValidSnapshot();
        SecurityPolicySnapshot policy = new SecurityPolicySnapshot();
        policy.setType("KEY");
        policy.setValue("keyword");
        policy.setDesc("Keyword");
        snapshot.getRobotConfigs().get(0).setSecurityPolicyConfigs(List.of(policy, policy));

        try {
            LarkConfigSnapshotValidator.validateForImport(snapshot);
            fail("Expected FormException");
        } catch (FormException e) {
            assertEquals("payload", e.getFormField());
        }
    }

    private static LarkConfigSnapshot createValidSnapshot() {
        LarkConfigSnapshot snapshot = new LarkConfigSnapshot();
        snapshot.setSchemaVersion(LarkConfigSnapshot.CURRENT_SCHEMA_VERSION);
        snapshot.setNoticeOccasions(Set.of("SUCCESS"));
        snapshot.setRobotConfigs(List.of(createRobot("robot-a")));
        return snapshot;
    }

    private static RobotSnapshot createRobot(String id) {
        RobotSnapshot robotSnapshot = new RobotSnapshot();
        robotSnapshot.setId(id);
        robotSnapshot.setName("Robot " + id);
        robotSnapshot.setWebhook("https://open.feishu.cn/open-apis/bot/v2/hook/" + id);
        return robotSnapshot;
    }
}
