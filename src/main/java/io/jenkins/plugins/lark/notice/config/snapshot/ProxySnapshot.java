package io.jenkins.plugins.lark.notice.config.snapshot;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Snapshot of proxy settings.
 *
 * @author xm.z
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProxySnapshot {

    /**
     * Proxy type name such as {@code DIRECT}, {@code HTTP}, or {@code SOCKS}.
     */
    private String type;

    /**
     * Whether proxying is enabled.
     */
    private boolean enabled;

    /**
     * Proxy host name.
     */
    private String host;

    /**
     * Proxy port.
     */
    private Integer port;
}
