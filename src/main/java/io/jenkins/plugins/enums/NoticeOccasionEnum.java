package io.jenkins.plugins.enums;

import hudson.model.Result;
import io.jenkins.plugins.Messages;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知时机
 *
 * @author xm.z
 */
@Getter
@AllArgsConstructor
public enum NoticeOccasionEnum {
    /**
     * 在启动构建时通知
     */
    START(Messages.NoticeOccasion_start()),

    /**
     * 构建中断时通知
     */
    ABORTED(Messages.NoticeOccasion_aborted()),

    /**
     * 构建失败时通知
     */
    FAILURE(Messages.NoticeOccasion_failure()),

    /**
     * 构建成功时通知
     */
    SUCCESS(Messages.NoticeOccasion_success()),

    /**
     * 构建不稳定时通知
     */
    UNSTABLE(Messages.NoticeOccasion_unstable()),

    /**
     * 在未构建时通知
     */
    NOT_BUILT(Messages.NoticeOccasion_not_built());

    private final String desc;

    public static NoticeOccasionEnum getNoticeOccasion(Result result) {
        if (Result.SUCCESS.equals(result)) {
            return NoticeOccasionEnum.SUCCESS;
        }
        if (Result.FAILURE.equals(result)) {
            return NoticeOccasionEnum.FAILURE;
        }
        if (Result.ABORTED.equals(result)) {
            return NoticeOccasionEnum.ABORTED;
        }
        if (Result.UNSTABLE.equals(result)) {
            return NoticeOccasionEnum.UNSTABLE;
        }
        if (Result.NOT_BUILT.equals(result)) {
            return NoticeOccasionEnum.NOT_BUILT;
        }
        return null;
    }

    public BuildStatusEnum buildStatus() {
        switch (this) {
            case START:
                return BuildStatusEnum.START;
            case SUCCESS:
                return BuildStatusEnum.SUCCESS;
            case FAILURE:
                return BuildStatusEnum.FAILURE;
            case ABORTED:
                return BuildStatusEnum.ABORTED;
            case UNSTABLE:
                return BuildStatusEnum.UNSTABLE;
            case NOT_BUILT:
                return BuildStatusEnum.NOT_BUILT;
            default:
                return BuildStatusEnum.UNKNOWN;
        }
    }

}
