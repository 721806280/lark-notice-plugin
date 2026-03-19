package io.jenkins.plugins.lark.notice.enums;

import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.i18n.NoticeI18n;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Enum representing the various build statuses that can occur during the lifecycle of a build process.
 * Each status is associated with a specific label, color, and template for consistent representation across different contexts.
 *
 * @author xm.z
 */
@Getter
@AllArgsConstructor
public enum BuildStatusEnum {

    /**
     * Indicates that the build has started.
     */
    START("blue", "blue"),

    /**
     * Indicates that the build has failed.
     */
    FAILURE("red", "red"),

    /**
     * Indicates that the build was successful.
     */
    SUCCESS("green", "green"),

    /**
     * Indicates that the build was aborted.
     */
    ABORTED("neutral", "grey"),

    /**
     * Indicates that the build is unstable.
     */
    UNSTABLE("yellow", "yellow"),

    /**
     * Indicates that the build has not been built.
     */
    NOT_BUILT("turquoise", "turquoise"),

    /**
     * Indicates an unknown build status.
     */
    UNKNOWN("purple", "purple");

    /**
     * A static map to quickly find a {@link BuildStatusEnum} instance based on a {@link NoticeOccasionEnum}.
     * This facilitates the mapping between occasions when a notice might be sent and the corresponding build statuses.
     */
    static final Map<NoticeOccasionEnum, BuildStatusEnum> BUILD_STATUS_ENUM_MAP = new HashMap<>(
            Map.of(
                    NoticeOccasionEnum.START, START,
                    NoticeOccasionEnum.SUCCESS, SUCCESS,
                    NoticeOccasionEnum.FAILURE, FAILURE,
                    NoticeOccasionEnum.ABORTED, ABORTED,
                    NoticeOccasionEnum.UNSTABLE, UNSTABLE,
                    NoticeOccasionEnum.NOT_BUILT, NOT_BUILT
            )
    );

    /**
     * The color associated with this status, used for UI representation.
     */
    private final String color;

    /**
     * The template identifier associated with this status, if applicable.
     */
    private final String template;

    /**
     * Returns the localized label for the current build status.
     * The value is resolved at call time so UI rendering follows the current locale.
     *
     * @return Localized label of this build status.
     */
    public String getLabel() {
        return getLabel(Locale.getDefault());
    }

    /**
     * Returns the localized label for the current build status using the provided locale.
     *
     * @param locale locale to render
     * @return localized label of this build status
     */
    public String getLabel(Locale locale) {
        return switch (this) {
            case START -> NoticeI18n.buildStatusLabel(locale, "start");
            case FAILURE -> NoticeI18n.buildStatusLabel(locale, "failure");
            case SUCCESS -> NoticeI18n.buildStatusLabel(locale, "success");
            case ABORTED -> NoticeI18n.buildStatusLabel(locale, "aborted");
            case UNSTABLE -> NoticeI18n.buildStatusLabel(locale, "unstable");
            case NOT_BUILT -> NoticeI18n.buildStatusLabel(locale, "not_built");
            case UNKNOWN -> NoticeI18n.buildStatusLabel(locale, "unknown");
        };
    }

}
