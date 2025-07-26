package io.jenkins.plugins.lark.notice.sdk.model.lark.support.form.input;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.form.TextElement;
import lombok.Data;

/**
 * 输入框降级文案配置
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FallbackElement {
    /**
     * 降级文案标签，固定为 "fallback_text"
     */
    private final String tag = "fallback_text";

    /**
     * 降级文案内容
     */
    private TextElement text;
}