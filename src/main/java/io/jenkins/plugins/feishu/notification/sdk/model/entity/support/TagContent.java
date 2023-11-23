package io.jenkins.plugins.feishu.notification.sdk.model.entity.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TagContent
 *
 * @author xm.z
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagContent {

    /**
     * 标签类型
     */
    private String tag;

    /**
     * 信息文本
     */
    private String content;

}
