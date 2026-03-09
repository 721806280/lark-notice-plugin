package io.jenkins.plugins.lark.notice.service;

import io.jenkins.plugins.lark.notice.config.LarkNotifierConfig;
import io.jenkins.plugins.lark.notice.enums.NoticeOccasionEnum;

import java.util.List;

/**
 * Filters notifier configs by notice occasion.
 *
 * @author xm.z
 */
public final class NotifierOccasionFilter {

    private NotifierOccasionFilter() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Returns notifier configs that include the target occasion.
     *
     * @param notifierConfigs all candidate notifier configs
     * @param occasion        current notice occasion
     * @return configs matched by occasion
     */
    public static List<LarkNotifierConfig> filterByOccasion(List<LarkNotifierConfig> notifierConfigs,
                                                             NoticeOccasionEnum occasion) {
        return notifierConfigs.stream()
                .filter(config -> config.getNoticeOccasions().contains(occasion.name()))
                .toList();
    }
}
