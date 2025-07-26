package io.jenkins.plugins.lark.notice.sdk.model.lark.support.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * 卡片摘要信息
 *
 * @author xm.z
 */
@Data
public class Summary {
    /**
     * 自定义摘要信息。如果开启了流式更新模式，该参数将默认为“生成中”
     */
    private String content;

    /**
     * 摘要信息的多语言配置。了解支持的所有语种。参考配置卡片多语言文档
     */
    @JsonProperty("i18n_content")
    private Map<String, String> i18nContent;

}