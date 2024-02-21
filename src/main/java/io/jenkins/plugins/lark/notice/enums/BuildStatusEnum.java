package io.jenkins.plugins.lark.notice.enums;

import io.jenkins.plugins.lark.notice.Messages;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
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
    START(Messages.build_status_start(), "blue", "blue"),

    /**
     * Indicates that the build has failed.
     */
    FAILURE(Messages.build_status_failure(), "red", "red"),

    /**
     * Indicates that the build was successful.
     */
    SUCCESS(Messages.build_status_success(), "green", "green"),

    /**
     * Indicates that the build was aborted.
     */
    ABORTED(Messages.build_status_aborted(), "neutral", "grey"),

    /**
     * Indicates that the build is unstable.
     */
    UNSTABLE(Messages.build_status_unstable(), "yellow", "yellow"),

    /**
     * Indicates that the build has not been built.
     */
    NOT_BUILT(Messages.build_status_not_built(), "turquoise", "turquoise"),

    /**
     * Indicates an unknown build status.
     */
    UNKNOWN(Messages.build_status_unknown(), "purple", "purple");

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
     * The human-readable label for the status.
     */
    private final String label;

    /**
     * The color associated with this status, used for UI representation.
     */
    private final String color;

    /**
     * The template identifier associated with this status, if applicable.
     */
    private final String template;

}