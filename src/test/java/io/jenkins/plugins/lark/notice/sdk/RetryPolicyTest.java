package io.jenkins.plugins.lark.notice.sdk;

import io.jenkins.plugins.lark.notice.config.LarkRetryConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Unit tests for {@link RetryPolicy}.
 */
public class RetryPolicyTest {

    @Test
    public void disabledConfigShouldForceSingleAttempt() {
        LarkRetryConfig config = new LarkRetryConfig(false, 5, 500, 5000, 2.0, 0.0);
        RetryPolicy policy = RetryPolicy.from(config);
        assertFalse(policy.isEnabled());
        assertEquals(1, policy.getMaxAttempts());
    }

    @Test
    public void delayShouldFollowBackoffWhenNoJitter() {
        LarkRetryConfig config = new LarkRetryConfig(true, 3, 500, 5000, 2.0, 0.0);
        RetryPolicy policy = RetryPolicy.from(config);
        assertEquals(500L, policy.nextDelayMs(1));
        assertEquals(1000L, policy.nextDelayMs(2));
        assertEquals(2000L, policy.nextDelayMs(3));
    }

    @Test
    public void normalizeShouldDefaultInvalidHiddenDetailValues() {
        LarkRetryConfig normalized = LarkRetryConfig.normalize(new LarkRetryConfig(false, 0, 0, 0, 0.0, 0.0));

        assertFalse(normalized.isEnabled());
        assertEquals(LarkRetryConfig.DEFAULT_MAX_ATTEMPTS, normalized.getMaxAttempts());
        assertEquals(LarkRetryConfig.DEFAULT_INITIAL_DELAY_MS, normalized.getInitialDelayMs());
        assertEquals(LarkRetryConfig.DEFAULT_MAX_DELAY_MS, normalized.getMaxDelayMs());
        assertEquals(LarkRetryConfig.DEFAULT_BACKOFF_MULTIPLIER, normalized.getBackoffMultiplier(), 0.001d);
        assertEquals(LarkRetryConfig.DEFAULT_JITTER_RATIO, normalized.getJitterRatio(), 0.001d);
    }

    @Test
    public void normalizeShouldKeepMaxDelayAtLeastInitialDelay() {
        LarkRetryConfig normalized = LarkRetryConfig.normalize(new LarkRetryConfig(true, 3, 10_000, 1_000, 2.0, 0.0));

        assertEquals(10_000L, normalized.getInitialDelayMs());
        assertEquals(10_000L, normalized.getMaxDelayMs());
    }
}
