package io.jenkins.plugins.enums;

import io.jenkins.plugins.Messages;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 构建状态
 *
 * @author xm.z
 */
@Getter
@AllArgsConstructor
public enum BuildStatusEnum {

    /**
     * 开始
     */
    START(Messages.build_status_start(), "green"),

    /**
     * 失败
     */
    FAILURE(Messages.build_status_failure(), "red"),

    /**
     * 成功
     */
    SUCCESS(Messages.build_status_success(), "green"),

    /**
     * 取消
     */
    ABORTED(Messages.build_status_aborted(), "grey"),

    /**
     * 不稳定
     */
    UNSTABLE(Messages.build_status_unstable(), "grey"),

    /**
     * 未构建
     */
    NOT_BUILT(Messages.build_status_not_built(), "grey"),

    /**
     * 未知
     */
    UNKNOWN(Messages.build_status_unknown(), "default");

    private final String label;
    private final String color;

}
