package io.jenkins.plugins.lark.notice.sdk.model.lark.support.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * 样式配置，包括字号和颜色
 *
 * @author xm.z
 */
@Data
public class Style {
    /**
     * 自定义字号配置
     */
    @JsonProperty("text_size")
    private Map<String, TextSize> textSize;

    /**
     * 自定义颜色配置
     */
    private Map<String, Color> color;
}