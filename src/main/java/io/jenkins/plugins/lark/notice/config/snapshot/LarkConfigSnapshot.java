package io.jenkins.plugins.lark.notice.config.snapshot;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Serializable snapshot of the plugin global configuration.
 *
 * @author xm.z
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LarkConfigSnapshot {

    /**
     * Current snapshot schema version used for import/export compatibility checks.
     */
    public static final int CURRENT_SCHEMA_VERSION = 2;

    /**
     * Snapshot schema version.
     */
    private Integer schemaVersion = CURRENT_SCHEMA_VERSION;

    /**
     * Plugin version that produced this snapshot.
     */
    private String pluginVersion;

    /**
     * Export timestamp in UTC offset format.
     */
    private String exportedAt;

    /**
     * Exported proxy configuration.
     */
    private ProxySnapshot proxyConfig;

    /**
     * Verbose logging flag.
     */
    private boolean verbose;

    /**
     * Globally enabled notice occasions.
     */
    private Set<String> noticeOccasions = new LinkedHashSet<>();

    /**
     * Exported robot configurations.
     */
    private List<RobotSnapshot> robotConfigs = new ArrayList<>();
}
