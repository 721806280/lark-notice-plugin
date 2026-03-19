package io.jenkins.plugins.lark.notice.service;

import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.config.LarkNotifierConfig;
import io.jenkins.plugins.lark.notice.tools.ApiResponse;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

/**
 * Service for building the editable default template response for notifier configuration.
 */
public final class NotifierTemplatePreviewService {

    private NotifierTemplatePreviewService() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Builds a response payload containing the editable default template for the given inputs.
     *
     * @param robotId robot identifier from the UI
     * @param title   current title input
     * @param content current content input
     * @return response payload with default template or failure message
     */
    public static ApiResponse loadDefaultTemplate(String robotId, String title, String content) {
        try {
            LarkNotifierConfig config = new LarkNotifierConfig(
                    false,
                    false,
                    true,
                    robotId,
                    "",
                    false,
                    "",
                    title,
                    content,
                    "",
                    null
            );

            String template = NotificationTemplateService.buildEditableDefaultTemplate(config);
            return ApiResponse.ok().data(new JSONObject().element("defaultTemplate", template));
        } catch (Exception ex) {
            String detail = StringUtils.defaultIfBlank(ex.getMessage(), ex.getClass().getSimpleName());
            return ApiResponse.fail(Messages.config_import_payload_invalid(detail));
        }
    }
}
