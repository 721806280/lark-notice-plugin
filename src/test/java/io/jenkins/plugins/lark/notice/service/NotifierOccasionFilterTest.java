package io.jenkins.plugins.lark.notice.service;

import io.jenkins.plugins.lark.notice.config.LarkNotifierConfig;
import io.jenkins.plugins.lark.notice.enums.NoticeOccasionEnum;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link NotifierOccasionFilter}.
 *
 * @author xm.z
 */
public class NotifierOccasionFilterTest {

    @Test
    public void filterByOccasionShouldReturnOnlyMatchedConfigs() {
        LarkNotifierConfig startOnly = createNotifierConfig("robot-start", Set.of("START"));
        LarkNotifierConfig successOnly = createNotifierConfig("robot-success", Set.of("SUCCESS"));
        LarkNotifierConfig both = createNotifierConfig("robot-both", Set.of("START", "SUCCESS"));

        List<LarkNotifierConfig> matched = NotifierOccasionFilter.filterByOccasion(
                List.of(startOnly, successOnly, both),
                NoticeOccasionEnum.SUCCESS);

        assertEquals(2, matched.size());
        assertEquals("robot-success", matched.get(0).getRobotId());
        assertEquals("robot-both", matched.get(1).getRobotId());
    }

    @Test
    public void filterByOccasionShouldReturnEmptyWhenNoConfigs() {
        List<LarkNotifierConfig> matched = NotifierOccasionFilter.filterByOccasion(
                List.of(),
                NoticeOccasionEnum.START);

        assertTrue(matched.isEmpty());
    }

    private static LarkNotifierConfig createNotifierConfig(String robotId, Set<String> occasions) {
        return new LarkNotifierConfig(
                false,
                false,
                true,
                robotId,
                "Robot-" + robotId,
                false,
                "",
                "title",
                "content",
                "",
                occasions
        );
    }
}
