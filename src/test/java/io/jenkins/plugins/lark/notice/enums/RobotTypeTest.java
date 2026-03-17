package io.jenkins.plugins.lark.notice.enums;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests for resolving robot types from webhook URLs.
 */
public class RobotTypeTest {

    @Test
    public void fromUrlShouldResolveOfficialLarkWebhook() {
        assertEquals(RobotType.LARK, RobotType.fromUrl("https://open.larksuite.com/open-apis/bot/v2/hook/robot-a"));
    }

    @Test
    public void fromUrlShouldResolveOfficialFeishuWebhook() {
        assertEquals(RobotType.LARK, RobotType.fromUrl("https://open.feishu.cn/open-apis/bot/v2/hook/robot-a"));
    }

    @Test
    public void fromUrlShouldResolveCustomFeishuWebhook() {
        assertEquals(RobotType.LARK, RobotType.fromUrl("https://feishu.example.com/open-apis/bot/v2/hook/robot-a"));
    }

    @Test
    public void fromUrlShouldResolveDingTalkWebhook() {
        assertEquals(RobotType.DING_TAlK, RobotType.fromUrl("https://api.dingtalk.com/robot/send?access_token=token"));
    }

    @Test
    public void fromUrlShouldRejectUnsupportedWebhookPath() {
        assertNull(RobotType.fromUrl("https://example.com/webhook/robot-a"));
    }

    @Test
    public void fromUrlShouldRejectUnsupportedScheme() {
        assertNull(RobotType.fromUrl("ftp://feishu.example.com/open-apis/bot/v2/hook/robot-a"));
    }
}
