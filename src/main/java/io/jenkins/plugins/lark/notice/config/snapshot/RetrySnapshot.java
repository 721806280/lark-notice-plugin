package io.jenkins.plugins.lark.notice.config.snapshot;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.jenkins.plugins.lark.notice.config.LarkRetryConfig;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Snapshot of retry settings.
 *
 * @author xm.z
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RetrySnapshot {

    /**
     * Whether retry is enabled.
     */
    private boolean enabled;

    /**
     * Maximum send attempts including the initial try.
     */
    private Integer maxAttempts;

    /**
     * Delay before the first retry in milliseconds.
     */
    private Long initialDelayMs;

    /**
     * Maximum backoff delay in milliseconds.
     */
    private Long maxDelayMs;

    /**
     * Exponential backoff multiplier.
     */
    private Double backoffMultiplier;

    /**
     * Jitter ratio for retry delay randomization.
     */
    private Double jitterRatio;

    /**
     * Converts this snapshot into a retry config, defaulting omitted detail fields.
     *
     * @return normalized retry config
     */
    public LarkRetryConfig toRetryConfig() {
        return LarkRetryConfig.normalize(new LarkRetryConfig(
                enabled,
                defaultInteger(maxAttempts, LarkRetryConfig.DEFAULT_MAX_ATTEMPTS),
                defaultLong(initialDelayMs, LarkRetryConfig.DEFAULT_INITIAL_DELAY_MS),
                defaultLong(maxDelayMs, LarkRetryConfig.DEFAULT_MAX_DELAY_MS),
                defaultDouble(backoffMultiplier, LarkRetryConfig.DEFAULT_BACKOFF_MULTIPLIER),
                defaultDouble(jitterRatio, LarkRetryConfig.DEFAULT_JITTER_RATIO)
        ));
    }

    boolean hasHiddenFormDefaultValues() {
        return Integer.valueOf(0).equals(maxAttempts)
                && Long.valueOf(0L).equals(initialDelayMs)
                && Long.valueOf(0L).equals(maxDelayMs)
                && Double.valueOf(0.0d).equals(backoffMultiplier)
                && Double.valueOf(0.0d).equals(jitterRatio);
    }

    private static int defaultInteger(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private static long defaultLong(Long value, long defaultValue) {
        return value == null ? defaultValue : value;
    }

    private static double defaultDouble(Double value, double defaultValue) {
        return value == null ? defaultValue : value;
    }
}
