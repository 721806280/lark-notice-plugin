package io.jenkins.plugins.lark.notice.config;

import io.jenkins.plugins.lark.notice.enums.RobotProtocolType;
import io.jenkins.plugins.lark.notice.enums.RobotType;
import io.jenkins.plugins.lark.notice.enums.WebhookEndpointMode;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Resolves submitted robot endpoint settings into one canonical webhook URL.
 *
 * @author xm.z
 */
public final class RobotWebhookResolver {

    private static final String LARK_WEBHOOK_PREFIX = "/open-apis/bot/v2/hook/";

    private RobotWebhookResolver() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Resolves the canonical webhook URL from the submitted protocol and endpoint inputs.
     *
     * @param protocolType declared protocol family (may be null)
     * @param endpointMode selected endpoint input mode (may be null)
     * @param webhook      full webhook URL input
     * @param baseUrl      base URL input used by token mode
     * @param webhookToken token input used by token mode
     * @return canonical webhook URL, or {@code null} when the inputs are incompatible
     */
    public static String resolveWebhook(RobotProtocolType protocolType, WebhookEndpointMode endpointMode,
                                        String webhook, String baseUrl, String webhookToken) {
        RobotProtocolType resolvedProtocol = resolveProtocolType(protocolType, webhook, baseUrl, webhookToken);
        WebhookEndpointMode resolvedMode = resolveEndpointMode(resolvedProtocol, endpointMode, baseUrl, webhookToken);
        if (WebhookEndpointMode.BASE_URL_AND_TOKEN.equals(resolvedMode)) {
            if (!RobotProtocolType.LARK_COMPATIBLE.equals(resolvedProtocol)) {
                return null;
            }
            String normalizedBaseUrl = normalizeBaseUrl(baseUrl);
            String normalizedToken = StringUtils.trimToNull(webhookToken);
            if (normalizedBaseUrl == null || normalizedToken == null) {
                return StringUtils.trimToNull(webhook);
            }
            return normalizedBaseUrl + LARK_WEBHOOK_PREFIX + normalizedToken;
        }
        return StringUtils.trimToNull(webhook);
    }

    /**
     * Resolves the protocol family from explicit input or by inferring from the webhook.
     *
     * @param protocolType explicit protocol type (may be null)
     * @param webhook      webhook URL input
     * @param baseUrl      base URL input
     * @param webhookToken token input
     * @return resolved protocol family (never null)
     */
    public static RobotProtocolType resolveProtocolType(RobotProtocolType protocolType, String webhook, String baseUrl, String webhookToken) {
        if (protocolType != null) {
            return protocolType;
        }
        RobotProtocolType inferred = RobotProtocolType.inferFromWebhook(webhook);
        if (inferred != null) {
            return inferred;
        }
        return RobotProtocolType.LARK_COMPATIBLE;
    }

    /**
     * Resolves which endpoint mode to use, considering the protocol and input completeness.
     *
     * @param protocolType resolved protocol family
     * @param endpointMode explicit endpoint mode (may be null)
     * @param baseUrl      base URL input
     * @param webhookToken token input
     * @return resolved endpoint mode
     */
    public static WebhookEndpointMode resolveEndpointMode(RobotProtocolType protocolType, WebhookEndpointMode endpointMode,
                                                          String baseUrl, String webhookToken) {
        if (RobotProtocolType.DING_TALK.equals(protocolType)) {
            return WebhookEndpointMode.FULL_WEBHOOK;
        }
        if (endpointMode != null) {
            return endpointMode;
        }
        return StringUtils.isAnyBlank(baseUrl, webhookToken)
                ? WebhookEndpointMode.FULL_WEBHOOK
                : WebhookEndpointMode.BASE_URL_AND_TOKEN;
    }

    /**
     * Returns whether the webhook is supported under the supplied protocol family.
     *
     * @param protocolType protocol family (may be null for "any")
     * @param webhook      webhook URL input
     * @return {@code true} when the webhook is supported
     */
    public static boolean isSupportedWebhook(RobotProtocolType protocolType, String webhook) {
        if (StringUtils.isBlank(webhook)) {
            return false;
        }
        RobotType resolvedType = RobotType.fromUrl(webhook);
        if (resolvedType == null) {
            return false;
        }
        if (protocolType == null) {
            return true;
        }
        return protocolType.toRobotType().equals(resolvedType);
    }

    /**
     * Extracts the base URL from a canonical Lark-compatible webhook.
     *
     * @param webhook webhook URL input
     * @return base URL, or empty string when the webhook is not Lark-compatible
     */
    public static String extractBaseUrl(String webhook) {
        ParsedWebhook parsedWebhook = ParsedWebhook.parse(webhook);
        if (parsedWebhook == null || !parsedWebhook.larkCompatible) {
            return "";
        }
        return parsedWebhook.scheme + "://" + parsedWebhook.authority;
    }

    /**
     * Extracts the webhook token from a canonical Lark-compatible webhook.
     *
     * @param webhook webhook URL input
     * @return webhook token, or empty string when the webhook is not Lark-compatible
     */
    public static String extractWebhookToken(String webhook) {
        ParsedWebhook parsedWebhook = ParsedWebhook.parse(webhook);
        if (parsedWebhook == null || !parsedWebhook.larkCompatible) {
            return "";
        }
        return parsedWebhook.path.startsWith(LARK_WEBHOOK_PREFIX)
                ? parsedWebhook.path.substring(LARK_WEBHOOK_PREFIX.length())
                : parsedWebhook.path;
    }

    /**
     * Normalizes a base URL and validates its scheme/authority components.
     *
     * @param baseUrl base URL input
     * @return normalized base URL, or {@code null} when invalid
     */
    public static String normalizeBaseUrl(String baseUrl) {
        String normalized = StringUtils.trimToNull(baseUrl);
        if (normalized != null && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized == null) {
            return null;
        }
        try {
            URI uri = new URI(normalized);
            if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    || StringUtils.isBlank(uri.getHost())) {
                return null;
            }
            String authority = StringUtils.defaultString(uri.getRawAuthority());
            if (StringUtils.isBlank(authority)) {
                return null;
            }
            return uri.getScheme() + "://" + authority;
        } catch (URISyntaxException ex) {
            return null;
        }
    }

    private static final class ParsedWebhook {

        private final String scheme;

        private final String authority;

        private final String path;

        private final boolean larkCompatible;

        private ParsedWebhook(String scheme, String authority, String path, boolean larkCompatible) {
            this.scheme = scheme;
            this.authority = authority;
            this.path = path;
            this.larkCompatible = larkCompatible;
        }

        private static ParsedWebhook parse(String webhook) {
            if (StringUtils.isBlank(webhook)) {
                return null;
            }
            try {
                URI uri = new URI(webhook.trim());
                String scheme = StringUtils.defaultString(uri.getScheme());
                String authority = StringUtils.defaultString(uri.getRawAuthority());
                String path = StringUtils.defaultString(uri.getPath());
                if (StringUtils.isAnyBlank(scheme, authority, path)) {
                    return null;
                }
                return new ParsedWebhook(
                        scheme,
                        authority,
                        path,
                        path.startsWith(LARK_WEBHOOK_PREFIX)
                );
            } catch (URISyntaxException ex) {
                return null;
            }
        }
    }
}
