package io.jenkins.plugins.lark.notice.sdk.model.lark.support.at;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Alt
 *
 * @author xm.z
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alt {

    /**
     * 标签类型
     */
    private String tag = "plain_text";

    /**
     * 提示内容
     */
    private String content;

    public static Alt build(String content) {
        Alt alt = new Alt();
        alt.setContent(content);
        return alt;
    }
}
