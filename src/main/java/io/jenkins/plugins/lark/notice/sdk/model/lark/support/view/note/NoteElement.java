package io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.note;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * 备注组件（note）
 * 用于在卡片底部展示辅助信息，支持图标 + 文本等内容
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NoteElement {
    /**
     * 组件标签，固定为 "note"
     */
    private final String tag = "note";

    /**
     * 备注内容元素列表，支持图标、图片、文本等
     */
    private List<NoteItemElement> elements;
}