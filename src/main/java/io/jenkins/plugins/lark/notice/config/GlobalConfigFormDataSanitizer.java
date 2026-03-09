package io.jenkins.plugins.lark.notice.config;

import hudson.model.Descriptor.FormException;
import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.enums.RobotType;
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
            validateRobotWebhook(JSONObject.fromObject(robotConfigs.get(i)), i);
        }
        return robotConfigs;
    }

    private static void validateRobotWebhook(JSONObject jsonObject, int index) throws FormException {
        Object webhookObj = jsonObject.get("webhook");
        String webhook = webhookObj == null ? "" : webhookObj.toString();
        if (!isSupportedWebhook(webhook)) {
            throw new FormException(Messages.form_validation_webhook_invalid(), "robotConfigs[" + index + "].webhook");
        }
    }

    private static boolean isSupportedWebhook(String webhook) {
        if (StringUtils.isBlank(webhook)) {
            return false;
        }
        try {
            return RobotType.fromUrl(webhook) != null;
        } catch (Exception ex) {
            return false;
        }
    }
}
