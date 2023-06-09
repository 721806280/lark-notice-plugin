package io.jenkins.plugins.feishu.notification.enums;

import hudson.model.Result;
import io.jenkins.plugins.feishu.notification.Messages;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知时机枚举类
 *
 * @author xm.z
 */
@Getter
@AllArgsConstructor
public enum NoticeOccasionEnum {

    /**
     * 在启动构建时通知
     */
    START(Messages.notice_start()),

    /**
     * 构建中断时通知
     */
    ABORTED(Messages.notice_aborted()),

    /**
     * 构建失败时通知
     */
    FAILURE(Messages.notice_failure()),

    /**
     * 构建成功时通知
     */
    SUCCESS(Messages.notice_success()),

    /**
     * 构建不稳定时通知
     */
    UNSTABLE(Messages.notice_unstable()),

    /**
     * 在未构建时通知
     */
    NOT_BUILT(Messages.notice_not_built());

    /**
     * 将Result值和NoticeOccasionEnum值进行映射，方便获取对应的通知时机。
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
     * 描述信息
     */
    private final String desc;

    /**
     * 根据给定的Result值获取对应的通知时机。
     *
     * @param result 给定的Result值
     * @return 对应的通知时机
     */
    public static NoticeOccasionEnum getNoticeOccasion(Result result) {
        return RESULT_TO_ENUM_MAP.get(result);
    }

    /**
     * 获取当前通知时机所对应的构建状态。
     *
     * @return 当前通知时机所对应的构建状态
     */
    public BuildStatusEnum buildStatus() {
        return BuildStatusEnum.BUILD_STATUS_ENUM_MAP.getOrDefault(this, BuildStatusEnum.UNKNOWN);
    }

}
