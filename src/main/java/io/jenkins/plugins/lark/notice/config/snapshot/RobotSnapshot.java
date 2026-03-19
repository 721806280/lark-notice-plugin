package io.jenkins.plugins.lark.notice.config.snapshot;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.jenkins.plugins.lark.notice.enums.MessageLocaleStrategy;
import io.jenkins.plugins.lark.notice.enums.RobotProtocolType;
import io.jenkins.plugins.lark.notice.enums.WebhookEndpointMode;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Snapshot of a robot configuration.
 *
 * @author xm.z
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RobotSnapshot {

    /**
     * Stable robot identifier referenced by notifier settings.
     */
    private String id;

    /**
     * Robot display name.
     */
    private String name;

    /**
     * Webhook URL, optionally omitted in redacted exports.
     */
    private String webhook;

    /**
     * Explicit protocol family used for sender resolution.
     */
    private RobotProtocolType protocolType;

    /**
     * UI endpoint mode preference.
     */
    private WebhookEndpointMode endpointMode;

    /**
     * Locale strategy used for built-in default messages sent by this robot.
     */
    private MessageLocaleStrategy messageLocaleStrategy;

    /**
     * Per-robot retry settings.
     */
    private RetrySnapshot retryConfig;

    /**
     * Security policy entries with non-blank values.
     */
    private List<SecurityPolicySnapshot> securityPolicyConfigs = new ArrayList<>();
}
