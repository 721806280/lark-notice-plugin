package io.jenkins.plugins.lark.notice.config;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.config.security.LarkPermissions;
import jenkins.model.Jenkins;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

/**
 * Retry configuration for outbound webhook calls.
 *
 * @author xm.z
 */
@Getter
@Setter
@ToString
@Extension
public class LarkRetryConfig extends Descriptor<LarkRetryConfig> implements Describable<LarkRetryConfig> {

    /**
     * Default max attempts (including the initial send).
     */
    public static final int DEFAULT_MAX_ATTEMPTS = 1;

    /**
     * Default initial delay before the first retry.
     */
    public static final long DEFAULT_INITIAL_DELAY_MS = 500L;

    /**
     * Default maximum delay between retries.
     */
    public static final long DEFAULT_MAX_DELAY_MS = 5000L;

    /**
     * Default exponential backoff multiplier.
     */
    public static final double DEFAULT_BACKOFF_MULTIPLIER = 2.0d;

    /**
     * Default jitter ratio applied to computed delays.
     */
    public static final double DEFAULT_JITTER_RATIO = 0.2d;

    /**
     * Whether retries are enabled.
     */
    private boolean enabled;

    /**
     * Maximum attempts including the initial send.
     */
    private int maxAttempts;

    /**
     * Delay before the first retry in milliseconds.
     */
    private long initialDelayMs;

    /**
     * Maximum delay between retries in milliseconds.
     */
    private long maxDelayMs;

    /**
     * Exponential backoff multiplier.
     */
    private double backoffMultiplier;

    /**
     * Jitter ratio applied to computed delays.
     */
    private double jitterRatio;

    /**
     * Creates a retry config initialized with default values.
     */
    public LarkRetryConfig() {
        super(LarkRetryConfig.class);
        applyDefaults();
    }

    /**
     * Creates a retry config with explicit values.
     *
     * @param enabled           whether retries are enabled
     * @param maxAttempts       maximum attempts including the initial send
     * @param initialDelayMs    delay before the first retry in milliseconds
     * @param maxDelayMs        maximum delay between retries in milliseconds
     * @param backoffMultiplier exponential backoff multiplier
     * @param jitterRatio       jitter ratio applied to computed delays
     */
    @DataBoundConstructor
    public LarkRetryConfig(boolean enabled, int maxAttempts, long initialDelayMs, long maxDelayMs,
                           double backoffMultiplier, double jitterRatio) {
        this();
        this.enabled = enabled;
        this.maxAttempts = maxAttempts;
        this.initialDelayMs = initialDelayMs;
        this.maxDelayMs = maxDelayMs;
        this.backoffMultiplier = backoffMultiplier;
        this.jitterRatio = jitterRatio;
    }

    /**
     * Returns a new config instance with default values.
     *
     * @return default retry config
     */
    public static LarkRetryConfig defaultConfig() {
        return new LarkRetryConfig(false, DEFAULT_MAX_ATTEMPTS, DEFAULT_INITIAL_DELAY_MS,
                DEFAULT_MAX_DELAY_MS, DEFAULT_BACKOFF_MULTIPLIER, DEFAULT_JITTER_RATIO);
    }

    /**
     * Applies default values to this config instance.
     */
    private void applyDefaults() {
        this.enabled = false;
        this.maxAttempts = DEFAULT_MAX_ATTEMPTS;
        this.initialDelayMs = DEFAULT_INITIAL_DELAY_MS;
        this.maxDelayMs = DEFAULT_MAX_DELAY_MS;
        this.backoffMultiplier = DEFAULT_BACKOFF_MULTIPLIER;
        this.jitterRatio = DEFAULT_JITTER_RATIO;
    }

    /**
     * Returns the descriptor for UI binding.
     *
     * @return descriptor for this retry config
     */
    @Override
    public Descriptor<LarkRetryConfig> getDescriptor() {
        return this;
    }

    /**
     * Validates backoff multiplier input.
     *
     * @param value input value
     * @return validation result
     */
    @RequirePOST
    public FormValidation doCheckBackoffMultiplier(@QueryParameter String value) {
        Jenkins.get().checkPermission(LarkPermissions.CONFIGURE);
        Double parsed = parseDouble(value);
        if (parsed == null || parsed < 1.0d) {
            return FormValidation.error(Messages.retry_validation_backoff_multiplier_invalid());
        }
        return FormValidation.ok();
    }

    /**
     * Validates jitter ratio input.
     *
     * @param value input value
     * @return validation result
     */
    @RequirePOST
    public FormValidation doCheckJitterRatio(@QueryParameter String value) {
        Jenkins.get().checkPermission(LarkPermissions.CONFIGURE);
        Double parsed = parseDouble(value);
        if (parsed == null || parsed < 0.0d || parsed > 1.0d) {
            return FormValidation.error(Messages.retry_validation_jitter_ratio_invalid());
        }
        return FormValidation.ok();
    }

    /**
     * Parses a string into a Double, returning null when blank or invalid.
     *
     * @param value input value
     * @return parsed Double or null when invalid
     */
    private static Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
