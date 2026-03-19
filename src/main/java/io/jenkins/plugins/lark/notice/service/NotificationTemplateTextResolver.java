package io.jenkins.plugins.lark.notice.service;

import hudson.EnvVars;
import io.jenkins.plugins.lark.notice.i18n.NoticeI18n;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.LF;
import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.defaultTitle;

/**
 * Resolves editable and runtime template text fragments.
 */
public final class NotificationTemplateTextResolver {

    private NotificationTemplateTextResolver() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Resolves the title template, falling back to the built-in default title when appropriate.
     *
     * @param configuredTitle raw title input
     * @param locale          message locale
     * @return resolved title template
     */
    public static String resolveTitleTemplate(String configuredTitle, Locale locale) {
        String trimmedTitle = StringUtils.trimToNull(configuredTitle);
        return trimmedTitle == null || NoticeI18n.isBuiltInDefaultTitle(trimmedTitle)
                ? defaultTitle(locale)
                : trimmedTitle;
    }

    /**
     * Normalizes editable content template input by converting escaped newlines.
     *
     * @param content raw content input
     * @return normalized content template
     */
    public static String normalizeContent(String content) {
        return StringUtils.defaultString(content).replaceAll("\\\\n", LF);
    }

    /**
     * Expands and normalizes runtime content input using build environment variables.
     *
     * @param content raw content input
     * @param envVars build environment variables
     * @return expanded content template
     */
    public static String expandContent(String content, EnvVars envVars) {
        return envVars.expand(StringUtils.defaultString(content)).replaceAll("\\\\n", LF);
    }
}
