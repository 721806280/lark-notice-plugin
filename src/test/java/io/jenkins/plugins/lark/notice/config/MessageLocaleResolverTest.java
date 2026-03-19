package io.jenkins.plugins.lark.notice.config;

import io.jenkins.plugins.lark.notice.enums.MessageLocaleStrategy;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link MessageLocaleResolver}.
 */
public class MessageLocaleResolverTest {

    @Test
    public void shouldUseRobotStrategyWhenConfigured() {
        assertEquals(Locale.SIMPLIFIED_CHINESE, MessageLocaleResolver.resolve(MessageLocaleStrategy.ZH_CN));
    }

    @Test
    public void shouldFallbackToSystemDefaultWhenRobotStrategyMissing() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);

            assertEquals(Locale.US, MessageLocaleResolver.resolve((MessageLocaleStrategy) null));
        } finally {
            Locale.setDefault(previous);
        }
    }
}
