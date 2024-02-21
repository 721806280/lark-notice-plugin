package io.jenkins.plugins.lark.notice.sdk.model.lark.support;

import lombok.Data;

import java.util.ArrayList;
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
     * 被 @ 人的 user_id
     */
    private List<String> atUserIds;

    /**
     * 被 @ 人的 mobile
     */
    private List<String> atMobiles;

    public List<String> getAllAts() {
        List<String> allAt = new ArrayList<>();
        if (atUserIds != null && !atUserIds.isEmpty()) {
            allAt.addAll(atUserIds);
        }
        if (atMobiles != null && !atMobiles.isEmpty()) {
            allAt.addAll(atMobiles);
        }
        return allAt;
    }

}