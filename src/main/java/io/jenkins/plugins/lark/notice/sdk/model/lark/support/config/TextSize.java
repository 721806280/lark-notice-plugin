package io.jenkins.plugins.lark.notice.sdk.model.lark.support.config;

import lombok.Data;

/**
 * 自定义字号配置
 *
 * @author xm.z
 */
@Data
public class TextSize {
    /**
     * 桌面端的字号
     */
    private String pc;

    /**
     * 移动端的字号
     */
    private String mobile;

    /**
     * 在无法差异化配置字号的旧版飞书客户端上，生效的字号属性。选填
     */
    private String def;
}