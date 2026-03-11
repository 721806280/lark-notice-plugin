package io.jenkins.plugins.lark.notice.sdk;

import io.jenkins.plugins.lark.notice.config.LarkRetryConfig;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Normalized retry policy derived from global configuration.
 *
 * @author xm.z
 */
public final class RetryPolicy {

    private final boolean enabled;
    private final int maxAttempts;
    private final long initialDelayMs;
    private final long maxDelayMs;
    private final double backoffMultiplier;
    private final double jitterRatio;

    private RetryPolicy(boolean enabled, int maxAttempts, long initialDelayMs, long maxDelayMs,
                        double backoffMultiplier, double jitterRatio) {
        this.enabled = enabled;
        this.maxAttempts = maxAttempts;
        this.initialDelayMs = initialDelayMs;
        this.maxDelayMs = maxDelayMs;
        this.backoffMultiplier = backoffMultiplier;
        this.jitterRatio = jitterRatio;
    }

    /**
     * Creates a normalized retry policy from config, applying sane defaults and bounds.
     *
     * @param config retry config, may be null
     * @return normalized retry policy
     */
    public static RetryPolicy from(LarkRetryConfig config) {
        LarkRetryConfig effective = config == null ? LarkRetryConfig.defaultConfig() : config;

        boolean enabled = effective.isEnabled();
        int maxAttempts = Math.max(1, effective.getMaxAttempts());
        long initialDelayMs = Math.max(0L, effective.getInitialDelayMs());
        long maxDelayMs = Math.max(initialDelayMs, effective.getMaxDelayMs());
        double backoffMultiplier = effective.getBackoffMultiplier();
        if (backoffMultiplier < 1.0d) {
            backoffMultiplier = 1.0d;
        }
        double jitterRatio = effective.getJitterRatio();
        if (jitterRatio < 0.0d) {
            jitterRatio = 0.0d;
        } else if (jitterRatio > 1.0d) {
            jitterRatio = 1.0d;
        }

        if (!enabled) {
            maxAttempts = 1;
        }

        return new RetryPolicy(enabled, maxAttempts, initialDelayMs, maxDelayMs, backoffMultiplier, jitterRatio);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    /**
     * Computes the delay before the next retry.
     *
     * @param retryIndex 1-based retry index
     * @return delay in milliseconds
     */
    public long nextDelayMs(int retryIndex) {
        if (retryIndex <= 0) {
            return 0L;
        }
        double multiplier = Math.pow(backoffMultiplier, Math.max(0, retryIndex - 1));
        double rawDelay = initialDelayMs * multiplier;
        long delay = rawDelay > Long.MAX_VALUE ? Long.MAX_VALUE : (long) rawDelay;
        if (maxDelayMs > 0L) {
            delay = Math.min(delay, maxDelayMs);
        }
        if (delay <= 0L || jitterRatio <= 0.0d) {
            return delay;
        }

        double jitter = delay * jitterRatio;
        double min = Math.max(0.0d, delay - jitter);
        double max = delay + jitter;
        return (long) (min + ThreadLocalRandom.current().nextDouble() * (max - min));
    }
}
