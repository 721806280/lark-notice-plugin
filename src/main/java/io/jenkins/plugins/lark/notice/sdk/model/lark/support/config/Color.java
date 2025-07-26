package io.jenkins.plugins.lark.notice.sdk.model.lark.support.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 自定义颜色配置
 *
 * @author xm.z
 */
@Data
public class Color {
    /**
     * 浅色主题下的自定义颜色语法
     */
    @JsonProperty("light_mode")
    private String lightMode;

    /**
     * 深色主题下的自定义颜色语法
     */
    @JsonProperty("dark_mode")
    private String darkMode;
}