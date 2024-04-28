package io.jenkins.plugins.lark.notice.sdk.model.lark.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * TagContent
 *
 * @author xm.z
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagContent implements Serializable {

    /**
     * 标签类型
     */
    private String tag;

    /**
     * 信息文本
     */
    private String content;

}
