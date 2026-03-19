package io.jenkins.plugins.lark.notice.model;

import io.jenkins.plugins.lark.notice.enums.BuildStatusEnum;
import io.jenkins.plugins.lark.notice.enums.RobotType;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.Button;
import io.jenkins.plugins.lark.notice.tools.Utils;
import org.junit.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BuildJobModelI18nTest {

    @Test
    public void shouldResolveMarkdownLabelsByCurrentLocale() {
        Locale previous = Locale.getDefault();
        try {
            BuildJobModel model = createModel();

            Locale.setDefault(Locale.US);
            String englishMarkdown = model.toMarkdown(RobotType.LARK);
            assertTrue(englishMarkdown.contains("**Task Name**"));
            assertTrue(englishMarkdown.contains("**Build Status**"));
            assertTrue(englishMarkdown.contains("**Executor**"));

            Locale.setDefault(Locale.SIMPLIFIED_CHINESE);
            String chineseMarkdown = model.toMarkdown(RobotType.LARK);
            assertTrue(chineseMarkdown.contains("**任务名称**"));
            assertTrue(chineseMarkdown.contains("**构建状态**"));
            assertTrue(chineseMarkdown.contains("**执行者**"));
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    public void shouldResolveDefaultButtonsByCurrentLocale() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            List<Button> englishButtons = Utils.createDefaultButtons("https://example.com/job/1/");
            assertEquals("Changelog", englishButtons.get(0).getText());
            assertEquals("Console", englishButtons.get(1).getText());

            Locale.setDefault(Locale.SIMPLIFIED_CHINESE);
            List<Button> chineseButtons = Utils.createDefaultButtons("https://example.com/job/1/");
            assertEquals("更改记录", chineseButtons.get(0).getText());
            assertEquals("控制台", chineseButtons.get(1).getText());
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    public void shouldNormalizeDefaultButtonUrlsWhenJobUrlHasNoTrailingSlash() {
        List<Button> buttons = Utils.createDefaultButtons("https://example.com/job/demo/42", Locale.US);

        assertEquals("https://example.com/job/demo/42/changes", buttons.get(0).getUrl());
        assertEquals("https://example.com/job/demo/42/console", buttons.get(1).getUrl());
    }

    private static BuildJobModel createModel() {
        return BuildJobModel.builder()
                .title("title")
                .projectName("Demo Project")
                .projectUrl("https://example.com/project")
                .jobName("#42")
                .jobUrl("https://example.com/job/42")
                .statusType(BuildStatusEnum.SUCCESS)
                .duration("1 sec")
                .executorName("xm.z")
                .build();
    }
}
