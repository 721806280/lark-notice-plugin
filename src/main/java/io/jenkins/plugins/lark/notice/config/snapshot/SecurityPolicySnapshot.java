package io.jenkins.plugins.lark.notice.config.snapshot;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Snapshot of a security policy entry.
 *
 * @author xm.z
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SecurityPolicySnapshot {

    /**
     * Security policy type name.
     */
    private String type;

    /**
     * Policy value, optionally omitted in redacted exports.
     */
    private String value;

    /**
     * Localized policy description used by the UI.
     */
    private String desc;
}
