package io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.note;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.title.TitleElement;
import lombok.Data;

/**
 * 备注组件中的单个元素项（图标 / 文本）
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NoteItemElement {
    /**
     * 元素类型标签，如 "standard_icon"、"plain_text" 等
     */
    private String tag;

    /**
     * 图标 token，仅在 tag 为 standard_icon 时生效
     */
    @SuppressWarnings("lgtm[jenkins/plaintext-storage]")
    private String token;

    /**
     * 图标颜色，仅在 tag 为 standard_icon 时生效
     */
    private String color;

    /**
     * 图片的 key，仅在 tag 为 custom_icon 时生效
     */
    @JsonProperty("img_key")
    @SuppressWarnings("lgtm[jenkins/plaintext-storage]")
    private String imgKey;

    /**
     * 光标悬浮在图片上时展示的说明文本, 仅在 imgKey 非空时生效
     */
    private TitleElement alt;

    /**
     * 文本内容，当 tag 为 plain_text 或 lark_md 时生效
     */
    private String content;
}