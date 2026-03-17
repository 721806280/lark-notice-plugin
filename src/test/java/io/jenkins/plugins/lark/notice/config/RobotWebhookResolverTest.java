package io.jenkins.plugins.lark.notice.config;

import io.jenkins.plugins.lark.notice.enums.RobotProtocolType;
import io.jenkins.plugins.lark.notice.enums.WebhookEndpointMode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for webhook resolution, normalization, and protocol validation.
 */
public class RobotWebhookResolverTest {

    @Test
    public void shouldResolveLarkCompatibleWebhookFromBaseUrlAndToken() {
        String webhook = RobotWebhookResolver.resolveWebhook(
                RobotProtocolType.LARK_COMPATIBLE,
                WebhookEndpointMode.BASE_URL_AND_TOKEN,
                "",
                "https://feishu.example.com/",
                "robot-a"
        );

        assertEquals("https://feishu.example.com/open-apis/bot/v2/hook/robot-a", webhook);
    }

    @Test
    public void shouldRejectTokenModeForDingTalk() {
        String webhook = RobotWebhookResolver.resolveWebhook(
                RobotProtocolType.DING_TALK,
                WebhookEndpointMode.BASE_URL_AND_TOKEN,
                "",
                "https://oapi.dingtalk.com",
                "token"
        );

        assertNull(webhook);
    }

    @Test
    public void shouldExtractBaseUrlAndTokenFromLarkCompatibleWebhook() {
        String webhook = "https://open.feishu.cn/open-apis/bot/v2/hook/robot-a";

        assertEquals("https://open.feishu.cn", RobotWebhookResolver.extractBaseUrl(webhook));
        assertEquals("robot-a", RobotWebhookResolver.extractWebhookToken(webhook));
    }

    @Test
    public void shouldNormalizeBaseUrl() {
        assertEquals("https://open.feishu.cn", RobotWebhookResolver.normalizeBaseUrl(" https://open.feishu.cn/ "));
        assertNull(RobotWebhookResolver.normalizeBaseUrl("feishu.internal"));
    }

    @Test
    public void shouldRespectExplicitProtocolWhenValidatingWebhook() {
        assertTrue(RobotWebhookResolver.isSupportedWebhook(
                RobotProtocolType.LARK_COMPATIBLE,
                "https://feishu.example.com/open-apis/bot/v2/hook/robot-a"
        ));
    }

    @Test
    public void shouldInferProtocolFromWebhookWhenMissing() {
        assertEquals(RobotProtocolType.DING_TALK, RobotWebhookResolver.resolveProtocolType(
                null,
                "https://api.dingtalk.com/robot/send?access_token=token",
                "",
                ""
        ));
        assertEquals(RobotProtocolType.LARK_COMPATIBLE, RobotWebhookResolver.resolveProtocolType(
                null,
                "https://open.feishu.cn/open-apis/bot/v2/hook/robot-a",
                "",
                ""
        ));
    }

    @Test
    public void shouldDefaultEndpointModeToFullWebhookWhenInputMissing() {
        assertEquals(WebhookEndpointMode.FULL_WEBHOOK, RobotWebhookResolver.resolveEndpointMode(
                RobotProtocolType.LARK_COMPATIBLE,
                null,
                "",
                "token"
        ));
        assertEquals(WebhookEndpointMode.FULL_WEBHOOK, RobotWebhookResolver.resolveEndpointMode(
                RobotProtocolType.LARK_COMPATIBLE,
                null,
                "https://open.feishu.cn",
                ""
        ));
        assertEquals(WebhookEndpointMode.BASE_URL_AND_TOKEN, RobotWebhookResolver.resolveEndpointMode(
                RobotProtocolType.LARK_COMPATIBLE,
                null,
                "https://open.feishu.cn",
                "token"
        ));
    }

    @Test
    public void shouldRejectWebhookWhenProtocolMismatches() {
        assertFalse(RobotWebhookResolver.isSupportedWebhook(
                RobotProtocolType.DING_TALK,
                "https://open.feishu.cn/open-apis/bot/v2/hook/robot-a"
        ));
    }

    @Test
    public void shouldReturnEmptyWhenExtractingFromUnsupportedWebhook() {
        assertEquals("", RobotWebhookResolver.extractBaseUrl("https://example.com/webhook/robot-a"));
        assertEquals("", RobotWebhookResolver.extractWebhookToken("https://example.com/webhook/robot-a"));
    }
}
