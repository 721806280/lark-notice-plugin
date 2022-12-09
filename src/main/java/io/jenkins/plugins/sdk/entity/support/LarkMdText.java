package io.jenkins.plugins.sdk.entity.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LarkMdText
 *
 * @author xm.z
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LarkMdText {

    /**
     * 标签类型
     */
    private String tag = "lark_md";

    /**
     * 信息文本
     */
    private String content;

}
