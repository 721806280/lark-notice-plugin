package io.jenkins.plugins.lark.notice.config;

import hudson.model.Descriptor.FormException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for global configuration form payload normalization.
 *
 * @author xm.z
 */
public class GlobalConfigFormDataSanitizerTest {

    @Test
    public void shouldReturnEmptyArrayWhenRobotConfigsAreMissing() throws Exception {
        JSONArray normalized = GlobalConfigFormDataSanitizer.normalizeRobotConfigsPayload(null);

        assertTrue(normalized.isEmpty());
    }

    @Test
    public void shouldNormalizeSingleRobotObjectIntoArray() throws Exception {
        JSONObject robot = new JSONObject();
        robot.put("id", "robot-a");
        robot.put("name", "Robot A");
        robot.put("webhook", "https://open.feishu.cn/open-apis/bot/v2/hook/robot-a");

        JSONArray normalized = GlobalConfigFormDataSanitizer.normalizeRobotConfigsPayload(robot);

        assertEquals(1, normalized.size());
        assertEquals("robot-a", normalized.getJSONObject(0).getString("id"));
        assertEquals("Robot A", normalized.getJSONObject(0).getString("name"));
    }

    @Test
    public void shouldPreserveMultipleValidRobotConfigs() throws Exception {
        JSONArray robots = new JSONArray();
        robots.add(createRobot("robot-a"));
        robots.add(createRobot("robot-b"));

        JSONArray normalized = GlobalConfigFormDataSanitizer.normalizeRobotConfigsPayload(robots);

        assertEquals(2, normalized.size());
        assertEquals("robot-a", normalized.getJSONObject(0).getString("id"));
        assertEquals("robot-b", normalized.getJSONObject(1).getString("id"));
    }

    @Test
    public void shouldRejectRobotConfigWithoutWebhook() throws Exception {
        JSONArray robots = new JSONArray();
        JSONObject robot = createRobot("robot-a");
        robot.put("webhook", "   ");
        robots.add(robot);

        try {
            GlobalConfigFormDataSanitizer.normalizeRobotConfigsPayload(robots);
            fail("Expected FormException");
        } catch (FormException e) {
            assertEquals("robotConfigs[0].webhook", e.getFormField());
        }
    }

    @Test
    public void shouldRejectRobotConfigWithUnsupportedWebhookHost() throws Exception {
        JSONArray robots = new JSONArray();
        JSONObject robot = createRobot("robot-a");
        robot.put("webhook", "https://example.com/webhook/robot-a");
        robots.add(robot);

        try {
            GlobalConfigFormDataSanitizer.normalizeRobotConfigsPayload(robots);
            fail("Expected FormException");
        } catch (FormException e) {
            assertEquals("robotConfigs[0].webhook", e.getFormField());
        }
    }

    @Test
    public void shouldAcceptRobotConfigWithCustomFeishuWebhookHost() throws Exception {
        JSONArray robots = new JSONArray();
        JSONObject robot = createRobot("robot-a");
        robot.put("webhook", "https://feishu.example.com/open-apis/bot/v2/hook/robot-a");
        robots.add(robot);

        JSONArray normalized = GlobalConfigFormDataSanitizer.normalizeRobotConfigsPayload(robots);

        assertEquals(1, normalized.size());
        assertEquals("https://feishu.example.com/open-apis/bot/v2/hook/robot-a",
                normalized.getJSONObject(0).getString("webhook"));
    }

    @Test
    public void shouldResolveRobotConfigFromBaseUrlAndTokenMode() throws Exception {
        JSONArray robots = new JSONArray();
        JSONObject robot = createRobot("robot-a");
        robot.put("protocolType", "LARK_COMPATIBLE");
        robot.put("endpointMode", "BASE_URL_AND_TOKEN");
        robot.put("webhook", "");
        robot.put("baseUrl", "https://feishu.example.com");
        robot.put("webhookToken", "robot-a");
        robots.add(robot);

        JSONArray normalized = GlobalConfigFormDataSanitizer.normalizeRobotConfigsPayload(robots);

        assertEquals("https://feishu.example.com/open-apis/bot/v2/hook/robot-a",
                normalized.getJSONObject(0).getString("webhook"));
        assertEquals("LARK_COMPATIBLE", normalized.getJSONObject(0).getString("protocolType"));
        assertEquals("BASE_URL_AND_TOKEN", normalized.getJSONObject(0).getString("endpointMode"));
    }

    @Test
    public void shouldForceFullWebhookModeForDingTalk() throws Exception {
        JSONArray robots = new JSONArray();
        JSONObject robot = createRobot("robot-a");
        robot.put("protocolType", "DING_TALK");
        robot.put("endpointMode", "BASE_URL_AND_TOKEN");
        robot.put("webhook", "https://api.dingtalk.com/robot/send?access_token=token");
        robot.put("baseUrl", "https://oapi.dingtalk.com");
        robot.put("webhookToken", "token");
        robots.add(robot);

        JSONArray normalized = GlobalConfigFormDataSanitizer.normalizeRobotConfigsPayload(robots);

        assertEquals("DING_TALK", normalized.getJSONObject(0).getString("protocolType"));
        assertEquals("FULL_WEBHOOK", normalized.getJSONObject(0).getString("endpointMode"));
        assertEquals("https://api.dingtalk.com/robot/send?access_token=token",
                normalized.getJSONObject(0).getString("webhook"));
    }

    @Test
    public void shouldRejectTokenModeWhenBaseUrlInvalid() throws Exception {
        JSONArray robots = new JSONArray();
        JSONObject robot = createRobot("robot-a");
        robot.put("protocolType", "LARK_COMPATIBLE");
        robot.put("endpointMode", "BASE_URL_AND_TOKEN");
        robot.put("webhook", "");
        robot.put("baseUrl", "feishu.internal");
        robot.put("webhookToken", "robot-a");
        robots.add(robot);

        try {
            GlobalConfigFormDataSanitizer.normalizeRobotConfigsPayload(robots);
            fail("Expected FormException");
        } catch (FormException e) {
            assertEquals("robotConfigs[0].baseUrl", e.getFormField());
        }
    }

    @Test
    public void shouldRejectRobotConfigWithMalformedWebhook() throws Exception {
        JSONArray robots = new JSONArray();
        JSONObject robot = createRobot("robot-a");
        robot.put("webhook", "::not-a-url::");
        robots.add(robot);

        try {
            GlobalConfigFormDataSanitizer.normalizeRobotConfigsPayload(robots);
            fail("Expected FormException");
        } catch (FormException e) {
            assertEquals("robotConfigs[0].webhook", e.getFormField());
        }
    }

    private static JSONObject createRobot(String id) {
        JSONObject robot = new JSONObject();
        robot.put("id", id);
        robot.put("name", "Robot-" + id);
        robot.put("webhook", "https://open.feishu.cn/open-apis/bot/v2/hook/" + id);
        return robot;
    }
}
