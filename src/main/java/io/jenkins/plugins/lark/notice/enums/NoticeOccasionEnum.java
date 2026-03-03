package io.jenkins.plugins.lark.notice.enums;

import hudson.model.Result;
import io.jenkins.plugins.lark.notice.Messages;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Enum representing the various occasions on which a notification might be sent during the build process.
 * Each enum constant represents a specific occasion (e.g., when a build starts, fails, or succeeds) and is associated with a descriptive message.
 *
 * @author xm.z
 */
public enum NoticeOccasionEnum {

    /**
     * Indicates that the build has started.
     */
    START,

    /**
     * Indicates that the build was aborted.
     */
    ABORTED,

    /**
     * Indicates that the build has failed.
     */
    FAILURE,

    /**
     * Indicates that the build was successful.
     */
    SUCCESS,

    /**
     * Indicates that the build is unstable.
     */
    UNSTABLE,

    /**
     * Indicates that the build has not been built.
     */
    NOT_BUILT;

    /**
     * A static map to quickly find a {@link NoticeOccasionEnum} instance based on a {@link Result}.
     * This facilitates the mapping between build results and the corresponding notification occasions.
     */
    static final Map<Result, NoticeOccasionEnum> RESULT_TO_ENUM_MAP = new HashMap<>(
            Map.of(
                    Result.SUCCESS, SUCCESS,
                    Result.FAILURE, FAILURE,
                    Result.ABORTED, ABORTED,
                    Result.UNSTABLE, UNSTABLE,
                    Result.NOT_BUILT, NOT_BUILT
            )
    );

    /**
     * Retrieves the corresponding {@link NoticeOccasionEnum} for a given {@link Result}.
     *
     * @param result The build result used to determine the notice occasion.
     * @return The corresponding {@link NoticeOccasionEnum}, or null if no match is found.
     */
    public static NoticeOccasionEnum getNoticeOccasion(Result result) {
        return RESULT_TO_ENUM_MAP.get(Objects.nonNull(result) ? result : Result.SUCCESS);
    }

    /**
     * Determines the build status associated with this notice occasion.
     *
     * @return The corresponding {@link BuildStatusEnum}, defaults to {@link BuildStatusEnum#UNKNOWN} if no direct mapping exists.
     */
    public BuildStatusEnum buildStatus() {
        return BuildStatusEnum.BUILD_STATUS_ENUM_MAP.getOrDefault(this, BuildStatusEnum.UNKNOWN);
    }

    /**
     * Returns the localized description for the current notice occasion.
     * The value is resolved at call time so UI rendering follows the current locale.
     *
     * @return Localized description of this notice occasion.
     */
    public String getDesc() {
        return switch (this) {
            case START -> Messages.notice_start();
            case ABORTED -> Messages.notice_aborted();
            case FAILURE -> Messages.notice_failure();
            case SUCCESS -> Messages.notice_success();
            case UNSTABLE -> Messages.notice_unstable();
            case NOT_BUILT -> Messages.notice_not_built();
        };
    }

}
