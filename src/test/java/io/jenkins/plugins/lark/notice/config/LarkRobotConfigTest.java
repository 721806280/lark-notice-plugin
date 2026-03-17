package io.jenkins.plugins.lark.notice.config;

import io.jenkins.plugins.lark.notice.enums.RobotProtocolType;
import io.jenkins.plugins.lark.notice.enums.WebhookEndpointMode;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests for robot config defaults and derived values.
 */
public class LarkRobotConfigTest {

    @Test
    public void shouldInferProtocolAndEndpointFromWebhook() {
        LarkRobotConfig robotConfig = new LarkRobotConfig(
                "robot-a",
                "Robot A",
                "https://open.feishu.cn/open-apis/bot/v2/hook/robot-a",
                List.of()
        );

        assertEquals(RobotProtocolType.LARK_COMPATIBLE, robotConfig.getProtocolType());
        assertEquals(WebhookEndpointMode.FULL_WEBHOOK, robotConfig.getEndpointMode());
    }

    @Test
    public void shouldDeriveBaseUrlAndTokenFromWebhook() {
        LarkRobotConfig robotConfig = new LarkRobotConfig(
                "robot-a",
                "Robot A",
                "https://open.feishu.cn/open-apis/bot/v2/hook/robot-a",
                List.of()
        );

        assertEquals("https://open.feishu.cn", robotConfig.getBaseUrl());
        assertEquals("robot-a", robotConfig.getWebhookToken());
    }

    @Test
    public void obtainRobotTypeShouldReturnNullWhenProtocolMismatchesWebhook() {
        LarkRobotConfig robotConfig = new LarkRobotConfig(
                "robot-a",
                "Robot A",
                "https://open.feishu.cn/open-apis/bot/v2/hook/robot-a",
                List.of()
        );
        robotConfig.setProtocolType(RobotProtocolType.DING_TALK);

        assertNull(robotConfig.obtainRobotType());
    }
}
