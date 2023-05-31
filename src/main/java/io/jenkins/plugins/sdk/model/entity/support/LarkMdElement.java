package io.jenkins.plugins.sdk.model.entity.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LarkMdElement
 *
 * @author xm.z
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LarkMdElement {

    /**
     * 标签类型
     */
    private String tag = "div";

    /**
     * 信息文本
     */
    private LarkMdText text;

}
