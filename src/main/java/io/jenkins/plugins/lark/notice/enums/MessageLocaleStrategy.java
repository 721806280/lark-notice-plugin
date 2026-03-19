package io.jenkins.plugins.lark.notice.enums;

import java.util.Locale;
import java.util.Objects;

/**
 * Supported strategies for resolving the locale used by default notification content.
 *
 * @author xm.z
 */
public enum MessageLocaleStrategy {

    /**
     * Follow the JVM/Jenkins default locale.
     */
    SYSTEM_DEFAULT,

    /**
     * Always render Simplified Chinese content.
     */
    ZH_CN,

    /**
     * Always render English content.
     */
    EN_US;

    /**
     * Resolves the strategy to a concrete locale.
     *
     * @return concrete locale represented by the strategy
     */
    public Locale toLocale() {
        return switch (this) {
            case SYSTEM_DEFAULT -> Locale.getDefault();
            case ZH_CN -> Locale.SIMPLIFIED_CHINESE;
            case EN_US -> Locale.US;
        };
    }

    /**
     * Collapses the strategy to one of the explicit UI options.
     *
     * @return explicit Chinese or English strategy for segmented controls
     */
    public MessageLocaleStrategy toSelectableStrategy() {
        return switch (this) {
            case ZH_CN -> ZH_CN;
            case EN_US -> EN_US;
            case SYSTEM_DEFAULT -> fromLocale(Locale.getDefault());
        };
    }

    /**
     * Maps a locale to the nearest supported explicit strategy.
     *
     * @param locale source locale
     * @return Chinese when the locale language is zh, otherwise English
     */
    public static MessageLocaleStrategy fromLocale(Locale locale) {
        Locale resolved = Objects.requireNonNullElse(locale, Locale.getDefault());
        return Locale.SIMPLIFIED_CHINESE.getLanguage().equalsIgnoreCase(resolved.getLanguage()) ? ZH_CN : EN_US;
    }
}
