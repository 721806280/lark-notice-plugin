package io.jenkins.plugins.lark.notice.enums;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link MessageLocaleStrategy}.
 */
public class MessageLocaleStrategyTest {

    @Test
    public void systemDefaultShouldCollapseToChineseWhenJenkinsLocaleIsChinese() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.SIMPLIFIED_CHINESE);

            assertEquals(MessageLocaleStrategy.ZH_CN, MessageLocaleStrategy.SYSTEM_DEFAULT.toSelectableStrategy());
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    public void systemDefaultShouldCollapseToEnglishWhenJenkinsLocaleIsNotChinese() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);

            assertEquals(MessageLocaleStrategy.EN_US, MessageLocaleStrategy.SYSTEM_DEFAULT.toSelectableStrategy());
        } finally {
            Locale.setDefault(previous);
        }
    }
}
