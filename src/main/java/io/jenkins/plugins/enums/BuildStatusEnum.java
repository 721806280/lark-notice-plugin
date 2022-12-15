package io.jenkins.plugins.enums;

import io.jenkins.plugins.Messages;
import lombok.Getter;

/**
 * 构建状态
 *
 * @author xm.z
 */
@Getter
public enum BuildStatusEnum {

    /**
     * 开始
     */
    START(Messages.BuildStatusType_start(), "green"),

    /**
     * 失败
     */
    FAILURE(Messages.BuildStatusType_failure(), "red"),

    /**
     * 成功
     */
    SUCCESS(Messages.BuildStatusType_success(), "green"),

    /**
     * 取消
     */
    ABORTED(Messages.BuildStatusType_aborted(), "grey"),

    /**
     * 不稳定
     */
    UNSTABLE(Messages.BuildStatusType_unstable(), "grey"),

    /**
     * 未构建
     */
    NOT_BUILT(Messages.BuildStatusType_not_built(), "grey"),

    /**
     * 未知
     */
    UNKNOWN(Messages.BuildStatusType_unknown(), "default");


    private final String label;


    private final String color;

    BuildStatusEnum(String label, String color) {
        this.label = label;
        this.color = color;
    }
}
