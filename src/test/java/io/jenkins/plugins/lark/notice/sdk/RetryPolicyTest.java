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
}
