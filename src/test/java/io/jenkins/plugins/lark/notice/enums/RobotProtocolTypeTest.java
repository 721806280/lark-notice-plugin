package io.jenkins.plugins.lark.notice.enums;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests for protocol value parsing, inference, and mapping.
 */
public class RobotProtocolTypeTest {

    @Test
    public void fromValueShouldReturnNullForBlankOrUnknown() {
        assertNull(RobotProtocolType.fromValue(null));
        assertNull(RobotProtocolType.fromValue("  "));
        assertNull(RobotProtocolType.fromValue("UNKNOWN"));
    }

    @Test
    public void fromValueShouldTrimAndResolve() {
        assertEquals(RobotProtocolType.DING_TALK, RobotProtocolType.fromValue(" DING_TALK "));
        assertEquals(RobotProtocolType.WECHAT_WORK, RobotProtocolType.fromValue(" WECHAT_WORK "));
    }

    @Test
    public void inferFromWebhookShouldResolveKnownPlatforms() {
        assertEquals(RobotProtocolType.DING_TALK,
                RobotProtocolType.inferFromWebhook("https://api.dingtalk.com/robot/send?access_token=token"));
        assertEquals(RobotProtocolType.LARK_COMPATIBLE,
                RobotProtocolType.inferFromWebhook("https://open.feishu.cn/open-apis/bot/v2/hook/robot-a"));
        assertEquals(RobotProtocolType.WECHAT_WORK,
                RobotProtocolType.inferFromWebhook("https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=token"));
        assertNull(RobotProtocolType.inferFromWebhook("https://example.com/webhook/robot-a"));
    }

    @Test
    public void toRobotTypeShouldMapToRuntimeType() {
        assertEquals(RobotType.DING_TALK, RobotProtocolType.DING_TALK.toRobotType());
        assertEquals(RobotType.WECHAT_WORK, RobotProtocolType.WECHAT_WORK.toRobotType());
        assertEquals(RobotType.LARK, RobotProtocolType.LARK_COMPATIBLE.toRobotType());
    }
}
