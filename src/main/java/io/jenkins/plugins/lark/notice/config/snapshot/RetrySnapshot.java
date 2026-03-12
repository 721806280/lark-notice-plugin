package io.jenkins.plugins.lark.notice.config.snapshot;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    private int maxAttempts;

    /**
     * Delay before the first retry in milliseconds.
     */
    private long initialDelayMs;

    /**
     * Maximum backoff delay in milliseconds.
     */
    private long maxDelayMs;

    /**
     * Exponential backoff multiplier.
     */
    private double backoffMultiplier;

    /**
     * Jitter ratio for retry delay randomization.
     */
    private double jitterRatio;
}
