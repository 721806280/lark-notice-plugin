package io.jenkins.plugins.lark.notice.config.snapshot;

import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import io.jenkins.plugins.lark.notice.config.LarkRobotConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Plans and materializes snapshot imports for different import modes.
 *
 * @author xm.z
 */
public final class LarkConfigImportPlanner {

    private LarkConfigImportPlanner() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Builds a preview summary for importing one snapshot into the current global configuration.
     *
     * @param current current live global configuration
     * @param imported imported snapshot converted to live config values
     * @param mode selected import mode
     * @return preview summary for the requested import
     */
    public static LarkConfigImportPreview preview(LarkGlobalConfig current,
                                                  LarkConfigSnapshotMapper.ImportedGlobalConfig imported,
                                                  LarkConfigImportMode mode) {
        Set<String> currentIds = new LinkedHashSet<>(current.getRobotConfigs().stream().map(LarkRobotConfig::getId).toList());
        Set<String> importedIds = new LinkedHashSet<>(imported.getRobotConfigs().stream().map(LarkRobotConfig::getId).toList());

        int added = 0;
        int updated = 0;
        for (String importedId : importedIds) {
            if (currentIds.contains(importedId)) {
                updated++;
            } else {
                added++;
            }
        }

        int removed = 0;
        int retained = 0;
        for (String currentId : currentIds) {
            if (!importedIds.contains(currentId)) {
                if (mode == LarkConfigImportMode.REPLACE) {
                    removed++;
                } else {
                    retained++;
                }
            }
        }

        LarkConfigImportPreview preview = new LarkConfigImportPreview();
        preview.setMode(mode.name().toLowerCase());
        preview.setCurrentRobotCount(currentIds.size());
        preview.setImportedRobotCount(importedIds.size());
        preview.setAddedRobotCount(added);
        preview.setUpdatedRobotCount(updated);
        preview.setRemovedRobotCount(removed);
        preview.setRetainedRobotCount(retained);
        return preview;
    }

    /**
     * Applies the selected import mode to produce the target live configuration values.
     *
     * @param current current live global configuration
     * @param imported imported snapshot converted to live config values
     * @param mode selected import mode
     * @return final configuration payload to apply
     */
    public static LarkConfigSnapshotMapper.ImportedGlobalConfig apply(LarkGlobalConfig current,
                                                                      LarkConfigSnapshotMapper.ImportedGlobalConfig imported,
                                                                      LarkConfigImportMode mode) {
        if (mode == LarkConfigImportMode.REPLACE) {
            return copyImportedConfig(imported);
        }

        LarkConfigSnapshotMapper.ImportedGlobalConfig merged = new LarkConfigSnapshotMapper.ImportedGlobalConfig();
        merged.setVerbose(imported.isVerbose());
        merged.setNoticeOccasions(new LinkedHashSet<>(imported.getNoticeOccasions()));
        merged.setProxyConfig(LarkConfigSnapshotMapper.copyProxyConfig(imported.getProxyConfig()));

        Map<String, LarkRobotConfig> robotsById = new LinkedHashMap<>();
        for (LarkRobotConfig currentRobot : current.getRobotConfigs()) {
            robotsById.put(currentRobot.getId(), LarkConfigSnapshotMapper.copyRobotConfig(currentRobot));
        }
        for (LarkRobotConfig importedRobot : imported.getRobotConfigs()) {
            robotsById.put(importedRobot.getId(), LarkConfigSnapshotMapper.copyRobotConfig(importedRobot));
        }

        merged.setRobotConfigs(new ArrayList<>(robotsById.values()));
        return merged;
    }

    private static LarkConfigSnapshotMapper.ImportedGlobalConfig copyImportedConfig(
            LarkConfigSnapshotMapper.ImportedGlobalConfig imported
    ) {
        LarkConfigSnapshotMapper.ImportedGlobalConfig copy = new LarkConfigSnapshotMapper.ImportedGlobalConfig();
        copy.setVerbose(imported.isVerbose());
        copy.setNoticeOccasions(new LinkedHashSet<>(imported.getNoticeOccasions()));
        copy.setProxyConfig(LarkConfigSnapshotMapper.copyProxyConfig(imported.getProxyConfig()));

        ArrayList<LarkRobotConfig> robots = new ArrayList<>();
        for (LarkRobotConfig robotConfig : imported.getRobotConfigs()) {
            robots.add(LarkConfigSnapshotMapper.copyRobotConfig(robotConfig));
        }
        copy.setRobotConfigs(robots);
        return copy;
    }
}
