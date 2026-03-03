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

    public String getLabel() {
        return switch (this) {
            case START -> Messages.build_status_start();
            case FAILURE -> Messages.build_status_failure();
            case SUCCESS -> Messages.build_status_success();
            case ABORTED -> Messages.build_status_aborted();
            case UNSTABLE -> Messages.build_status_unstable();
            case NOT_BUILT -> Messages.build_status_not_built();
            case UNKNOWN -> Messages.build_status_unknown();
        };
    }

}
