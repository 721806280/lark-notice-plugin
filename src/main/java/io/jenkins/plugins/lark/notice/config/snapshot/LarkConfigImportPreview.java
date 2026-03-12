package io.jenkins.plugins.lark.notice.config.snapshot;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Preview summary for one import operation.
 *
 * @author xm.z
 */
@Data
@NoArgsConstructor
public class LarkConfigImportPreview {

    /**
     * Selected import mode.
     */
    private String mode;

    /**
     * Number of currently configured robots.
     */
    private int currentRobotCount;

    /**
     * Number of robots present in the imported snapshot.
     */
    private int importedRobotCount;

    /**
     * Number of robots that would be added.
     */
    private int addedRobotCount;

    /**
     * Number of robots that would be updated because their IDs already exist.
     */
    private int updatedRobotCount;

    /**
     * Number of robots that would be removed in replace mode.
     */
    private int removedRobotCount;

    /**
     * Number of existing robots that would be retained in merge mode.
     */
    private int retainedRobotCount;

    /**
     * Whether proxy, verbose logging, and notice occasions would be overwritten.
     */
    private boolean globalSettingsOverwritten = true;
}
