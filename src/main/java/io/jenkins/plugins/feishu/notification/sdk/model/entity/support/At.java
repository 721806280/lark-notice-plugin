package io.jenkins.plugins.feishu.notification.sdk.model.entity.support;

import lombok.Data;

import java.util.List;

/**
 * At
 *
 * @author xm.z
 */
@Data
public class At {

    /**
     * 是否 @ 所有人
     */
    private Boolean isAtAll;

    /**
     * 被 @ 人的 open_id
     */
    private List<String> atOpenIds;

}