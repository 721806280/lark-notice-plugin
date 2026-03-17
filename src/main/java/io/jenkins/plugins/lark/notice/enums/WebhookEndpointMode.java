package io.jenkins.plugins.lark.notice.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * Supported webhook input modes shown in the configuration UI.
 *
 * @author xm.z
 */
public enum WebhookEndpointMode {

    FULL_WEBHOOK,
    BASE_URL_AND_TOKEN;

    /**
     * Resolves one endpoint mode from a persisted or submitted string value.
     *
     * @param value raw enum value
     * @return matching mode, or {@code null} when the value is blank or unknown
     */
    public static WebhookEndpointMode fromValue(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return WebhookEndpointMode.valueOf(value.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
