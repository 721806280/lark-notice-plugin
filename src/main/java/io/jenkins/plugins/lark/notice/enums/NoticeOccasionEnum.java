package io.jenkins.plugins.lark.notice.enums;

import hudson.model.Result;
import io.jenkins.plugins.lark.notice.Messages;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum representing the various occasions on which a notification might be sent during the build process.
 * Each enum constant represents a specific occasion (e.g., when a build starts, fails, or succeeds) and is associated with a descriptive message.
 *
 * @author xm.z
 */
@Getter
@AllArgsConstructor
public enum NoticeOccasionEnum {

    /**
     * Indicates that the build has started.
     */
    START(Messages.notice_start()),

    /**
     * Indicates that the build was aborted.
     */
    ABORTED(Messages.notice_aborted()),

    /**
     * Indicates that the build has failed.
     */
    FAILURE(Messages.notice_failure()),

    /**
     * Indicates that the build was successful.
     */
    SUCCESS(Messages.notice_success()),

    /**
     * Indicates that the build is unstable.
     */
    UNSTABLE(Messages.notice_unstable()),

    /**
     * Indicates that the build has not been built.
     */
    NOT_BUILT(Messages.notice_not_built());

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
     * The description of the notice occasion.
     */
    private final String desc;

    /**
     * Retrieves the corresponding {@link NoticeOccasionEnum} for a given {@link Result}.
     *
     * @param result The build result used to determine the notice occasion.
     * @return The corresponding {@link NoticeOccasionEnum}, or null if no match is found.
     */
    public static NoticeOccasionEnum getNoticeOccasion(Result result) {
        return RESULT_TO_ENUM_MAP.get(result);
    }

    /**
     * Determines the build status associated with this notice occasion.
     *
     * @return The corresponding {@link BuildStatusEnum}, defaults to {@link BuildStatusEnum#UNKNOWN} if no direct mapping exists.
     */
    public BuildStatusEnum buildStatus() {
        return BuildStatusEnum.BUILD_STATUS_ENUM_MAP.getOrDefault(this, BuildStatusEnum.UNKNOWN);
    }

}