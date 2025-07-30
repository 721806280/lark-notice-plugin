package io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.title;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * 标题或副标题内容
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TitleElement implements Serializable {

    /**
     * 文本类型的标签。可选值：plain_text 和 lark_md
     */
    private String tag;

    /**
     * 标题内容
     */
    private String content;

    /**
     * 辅助方法：创建 plain_text 类型的标题构建器
     */
    public static TitleElement buildPlainText(String content) {
        if (content == null) {
            return null;
        }
        TitleElement titleElement = new TitleElement();
        titleElement.setTag("plain_text");
        titleElement.setContent(content);
        return titleElement;
    }

    /**
     * 辅助方法：创建 lark_md 类型的标题构建器
     */
    public static TitleElement buildLarkMd(String content) {
        if (content == null) {
            return null;
        }
        TitleElement titleElement = new TitleElement();
        titleElement.setTag("lark_md");
        titleElement.setContent(content);
        return titleElement;
    }

}