package io.jenkins.plugins.lark.notice.config.snapshot;

import org.apache.commons.lang3.StringUtils;

/**
 * Supported import modes for configuration snapshots.
 *
 * @author xm.z
 */
public enum LarkConfigImportMode {

    /**
     * Replaces the full plugin configuration with the imported snapshot.
     */
    REPLACE,

    /**
     * Replaces global settings and merges robot entries by their stable IDs.
     */
    MERGE;

    /**
     * Resolves an import mode from a request parameter.
     *
     * @param value raw request parameter value
     * @return resolved import mode, defaulting to {@link #REPLACE}
     */
    public static LarkConfigImportMode fromValue(String value) {
        if (StringUtils.isBlank(value)) {
            return REPLACE;
        }
        for (LarkConfigImportMode mode : values()) {
            if (mode.name().equalsIgnoreCase(value.trim())) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unsupported import mode: " + value);
    }
}
