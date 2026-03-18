package io.jenkins.plugins.lark.notice.enums;

import io.jenkins.plugins.lark.notice.model.RobotConfigModel;
import io.jenkins.plugins.lark.notice.sdk.MessageSender;
import io.jenkins.plugins.lark.notice.sdk.impl.DingMessageSender;
import io.jenkins.plugins.lark.notice.sdk.impl.LarkMessageSender;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Supported robot platform types and their sender factories.
 *
 * @author xm.z
 */
@Getter
public enum RobotType {

    //
    LARK("Lark", "text_tag", "/open-apis/bot/v2/hook/") {
        /**
         * {@inheritDoc}
         */
        @Override
        public MessageSender obtainInstance(RobotConfigModel robotConfig) {
            return new LarkMessageSender(robotConfig);
        }
    },

    DING_TAlK("钉钉", "font", "/robot/send") {
        /**
         * {@inheritDoc}
         */
        @Override
        public MessageSender obtainInstance(RobotConfigModel robotConfig) {
            return new DingMessageSender(robotConfig);
        }
    };

    private final String name;

    private final String statusTagName;

    private final String webhookPathPrefix;

    RobotType(String name, String statusTagName, String webhookPathPrefix) {
        this.name = name;
        this.statusTagName = statusTagName;
        this.webhookPathPrefix = webhookPathPrefix;
    }

    /**
     * Resolves the robot type from a webhook URL using both host and path conventions.
     * Custom Feishu deployments may use private domains while still keeping the standard
     * bot webhook path, so host checks alone are insufficient.
     *
     * @param url webhook URL
     * @return matching robot type, or {@code null} when the URL is not recognized
     */
    public static RobotType fromUrl(String url) {
        ParsedWebhook webhook = ParsedWebhook.parse(url);
        if (webhook == null) {
            return null;
        }
        if (DING_TAlK.matches(webhook)) {
            return DING_TAlK;
        }
        if (LARK.matches(webhook)) {
            return LARK;
        }
        return null;
    }

    /**
     * Returns whether the supplied webhook URL matches any supported robot platform.
     *
     * @param url webhook URL
     * @return {@code true} when the webhook is supported
     */
    public static boolean isSupportedWebhook(String url) {
        return fromUrl(url) != null;
    }

    /**
     * Creates a platform-specific message sender for the supplied robot configuration.
     *
     * @param robotConfig robot configuration
     * @return message sender bound to the target platform
     */
    public abstract MessageSender obtainInstance(RobotConfigModel robotConfig);

    private boolean matches(ParsedWebhook webhook) {
        return webhook != null
                && webhook.httpScheme
                && Strings.CS.startsWith(webhook.path, webhookPathPrefix);
    }

    private static final class ParsedWebhook {

        private final boolean httpScheme;
        private final String path;

        private ParsedWebhook(boolean httpScheme, String path) {
            this.httpScheme = httpScheme;
            this.path = path;
        }

        private static ParsedWebhook parse(String url) {
            if (StringUtils.isBlank(url)) {
                return null;
            }
            try {
                URI uri = new URI(url.trim());
                String scheme = StringUtils.defaultString(uri.getScheme());
                String path = StringUtils.defaultString(uri.getPath());
                if (StringUtils.isBlank(uri.getHost()) || StringUtils.isBlank(path)) {
                    return null;
                }
                boolean httpScheme = "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
                return new ParsedWebhook(httpScheme, path);
            } catch (URISyntaxException ex) {
                return null;
            }
        }
    }
}
