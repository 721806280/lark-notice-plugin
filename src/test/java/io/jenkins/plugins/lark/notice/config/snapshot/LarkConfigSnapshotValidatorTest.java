package io.jenkins.plugins.lark.notice.config.snapshot;

import hudson.model.Descriptor.FormException;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
    public void validateForImportShouldAcceptValidSnapshot() throws Exception {
        LarkConfigSnapshotValidator.validateForImport(createValidSnapshot());
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
