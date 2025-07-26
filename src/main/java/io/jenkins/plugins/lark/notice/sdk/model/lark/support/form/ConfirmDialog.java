package io.jenkins.plugins.lark.notice.sdk.model.lark.support.form;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 输入框二次确认弹窗配置
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfirmDialog {
    /**
     * 弹窗标题
     */
    private TextElement title;

    /**
     * 弹窗文本内容
     */
    private TextElement text;
}