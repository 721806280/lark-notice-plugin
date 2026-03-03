package io.jenkins.plugins.lark.notice.enums;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class EnumI18nTest {

    @Test
    public void shouldResolveNoticeOccasionDescByCurrentLocale() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            assertEquals("Start", NoticeOccasionEnum.START.getDesc());

            Locale.setDefault(Locale.SIMPLIFIED_CHINESE);
            assertEquals("构建启动时", NoticeOccasionEnum.START.getDesc());
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    public void shouldResolveSecurityPolicyDescByCurrentLocale() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            assertEquals("Custom keyword", SecurityPolicyEnum.KEY.getDesc());

            Locale.setDefault(Locale.SIMPLIFIED_CHINESE);
            assertEquals("自定义关键词", SecurityPolicyEnum.KEY.getDesc());
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    public void shouldResolveBuildStatusLabelByCurrentLocale() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            assertEquals("Success", BuildStatusEnum.SUCCESS.getLabel());

            Locale.setDefault(Locale.SIMPLIFIED_CHINESE);
            assertEquals("成功", BuildStatusEnum.SUCCESS.getLabel());
        } finally {
            Locale.setDefault(previous);
        }
    }
}
