package io.jenkins.plugins.lark.notice.config;

import hudson.model.Descriptor.FormException;
import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.enums.RobotProtocolType;
import io.jenkins.plugins.lark.notice.enums.RobotType;
import io.jenkins.plugins.lark.notice.enums.WebhookEndpointMode;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

/**
 * Normalizes structured form data submitted for the global configuration page.
 */
final class GlobalConfigFormDataSanitizer {

    private GlobalConfigFormDataSanitizer() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    static JSONArray normalizeRobotConfigsPayload(Object robotConfigObj) throws FormException {
        if (robotConfigObj == null) {
            return new JSONArray();
        }

        JSONArray robotConfigs = JSONArray.fromObject(robotConfigObj);
        for (int i = 0; i < robotConfigs.size(); i++) {
            JSONObject normalized = JSONObject.fromObject(robotConfigs.get(i));
            normalizeAndValidateRobotWebhook(normalized, i);
            robotConfigs.set(i, normalized);
        }
        return robotConfigs;
    }

    private static void normalizeAndValidateRobotWebhook(JSONObject jsonObject, int index) throws FormException {
        String protocolValue = stringValue(jsonObject, "protocolType");
        String endpointModeValue = stringValue(jsonObject, "endpointMode");
        String webhook = stringValue(jsonObject, "webhook");
        String baseUrl = stringValue(jsonObject, "baseUrl");
        String webhookToken = stringValue(jsonObject, "webhookToken");

        RobotProtocolType protocolType = RobotWebhookResolver.resolveProtocolType(
                RobotProtocolType.fromValue(protocolValue), webhook, baseUrl, webhookToken);
        WebhookEndpointMode endpointMode = RobotWebhookResolver.resolveEndpointMode(
                protocolType, WebhookEndpointMode.fromValue(endpointModeValue), baseUrl, webhookToken);
        String resolvedWebhook = RobotWebhookResolver.resolveWebhook(
                protocolType, endpointMode, webhook, baseUrl, webhookToken);

        if (!isSupportedWebhook(protocolType, resolvedWebhook)) {
            throw new FormException(Messages.form_validation_webhook_invalid(), resolveValidationField(endpointMode, index));
        }

        jsonObject.put("webhook", resolvedWebhook);
        jsonObject.put("protocolType", protocolType.name());
        jsonObject.put("endpointMode", endpointMode.name());
    }

    private static boolean isSupportedWebhook(RobotProtocolType protocolType, String webhook) {
        return StringUtils.isNotBlank(webhook)
                && RobotType.isSupportedWebhook(webhook)
                && RobotWebhookResolver.isSupportedWebhook(protocolType, webhook);
    }

    private static String resolveValidationField(WebhookEndpointMode endpointMode, int index) {
        if (WebhookEndpointMode.BASE_URL_AND_TOKEN.equals(endpointMode)) {
            return "robotConfigs[" + index + "].baseUrl";
        }
        return "robotConfigs[" + index + "].webhook";
    }

    private static String stringValue(JSONObject jsonObject, String key) {
        Object value = jsonObject.get(key);
        return value == null ? "" : value.toString();
    }
}
