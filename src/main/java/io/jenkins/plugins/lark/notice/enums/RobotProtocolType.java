package io.jenkins.plugins.lark.notice.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * High-level robot protocol families supported by the plugin.
 *
 * @author xm.z
 */
public enum RobotProtocolType {

    LARK_COMPATIBLE,
    DING_TALK,
    WECHAT_WORK;

    /**
     * Resolves one protocol from a persisted or submitted string value.
     *
     * @param value raw enum value
     * @return matching protocol, or {@code null} when the value is blank or unknown
     */
    public static RobotProtocolType fromValue(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return RobotProtocolType.valueOf(value.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Infers one protocol from a full webhook URL.
     *
     * @param webhook webhook URL
     * @return inferred protocol, or {@code null} when the webhook is unsupported
     */
    public static RobotProtocolType inferFromWebhook(String webhook) {
        RobotType robotType = RobotType.fromUrl(webhook);
        if (robotType == null) {
            return null;
        }
        if (RobotType.DING_TAlK.equals(robotType)) {
            return DING_TALK;
        }
        if (RobotType.WECHAT_WORK.equals(robotType)) {
            return WECHAT_WORK;
        }
        return LARK_COMPATIBLE;
    }

    /**
     * Maps the protocol family to the runtime sender type.
     *
     * @return runtime sender type
     */
    public RobotType toRobotType() {
        return switch (this) {
            case DING_TALK -> RobotType.DING_TAlK;
            case WECHAT_WORK -> RobotType.WECHAT_WORK;
            case LARK_COMPATIBLE -> RobotType.LARK;
        };
    }
}
