package io.jenkins.plugins.lark.notice.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * High-level robot protocol families supported by the plugin.
 *
 * @author xm.z
 */
public enum RobotProtocolType {

    LARK_COMPATIBLE,
    DING_TALK;

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
        return RobotType.DING_TAlK.equals(robotType) ? DING_TALK : LARK_COMPATIBLE;
    }

    /**
     * Maps the protocol family to the runtime sender type.
     *
     * @return runtime sender type
     */
    public RobotType toRobotType() {
        return DING_TALK.equals(this) ? RobotType.DING_TAlK : RobotType.LARK;
    }
}
