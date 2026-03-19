package io.jenkins.plugins.lark.notice.service;

import hudson.model.Descriptor.FormException;
import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import io.jenkins.plugins.lark.notice.config.snapshot.LarkConfigImportMode;
import io.jenkins.plugins.lark.notice.config.snapshot.LarkConfigImportPlanner;
import io.jenkins.plugins.lark.notice.config.snapshot.LarkConfigImportPreview;
import io.jenkins.plugins.lark.notice.config.snapshot.LarkConfigSnapshot;
import io.jenkins.plugins.lark.notice.config.snapshot.LarkConfigSnapshotMapper;
import io.jenkins.plugins.lark.notice.config.snapshot.LarkConfigSnapshotValidator;
import io.jenkins.plugins.lark.notice.tools.ApiResponse;
import io.jenkins.plugins.lark.notice.tools.JsonUtils;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

/**
 * Service for previewing and applying configuration snapshot imports.
 */
public final class ConfigSnapshotImportService {

    private ConfigSnapshotImportService() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Builds a preview response for a snapshot import without persisting any changes.
     *
     * @param globalConfig current global configuration
     * @param payload      JSON snapshot payload pasted on the management page
     * @param modeValue    raw import mode request parameter
     * @return response payload containing the preview summary
     */
    public static ApiResponse preview(LarkGlobalConfig globalConfig, String payload, String modeValue) {
        try {
            ParsedImportRequest importRequest = parseImportRequest(payload, modeValue);
            LarkConfigImportPreview preview = LarkConfigImportPlanner.preview(
                    globalConfig,
                    importRequest.getImported(),
                    importRequest.getMode()
            );
            return ApiResponse.ok(Messages.config_import_preview_success(), JSONObject.fromObject(preview));
        } catch (FormException ex) {
            return ApiResponse.fail(ex.getMessage());
        } catch (Exception ex) {
            return ApiResponse.fail(buildInvalidPayloadMessage(ex));
        }
    }

    /**
     * Applies a JSON snapshot to the current configuration when valid.
     *
     * @param globalConfig current global configuration
     * @param payload      JSON snapshot payload pasted on the management page
     * @param modeValue    raw import mode request parameter
     * @return response payload describing the import result
     */
    public static ApiResponse apply(LarkGlobalConfig globalConfig, String payload, String modeValue) {
        try {
            ParsedImportRequest importRequest = parseImportRequest(payload, modeValue);
            LarkConfigSnapshotMapper.ImportedGlobalConfig planned = LarkConfigImportPlanner.apply(
                    globalConfig,
                    importRequest.getImported(),
                    importRequest.getMode()
            );
            globalConfig.setVerbose(planned.isVerbose());
            globalConfig.setNoticeOccasions(planned.getNoticeOccasions());
            globalConfig.setProxyConfig(planned.getProxyConfig());
            globalConfig.setRobotConfigs(planned.getRobotConfigs());
            globalConfig.save();

            return ApiResponse.ok(Messages.config_import_success(planned.getRobotConfigs().size()));
        } catch (FormException ex) {
            return ApiResponse.fail(ex.getMessage());
        } catch (Exception ex) {
            return ApiResponse.fail(buildInvalidPayloadMessage(ex));
        }
    }

    private static ParsedImportRequest parseImportRequest(String payload, String modeValue) throws FormException {
        if (StringUtils.isBlank(payload)) {
            throw new FormException(Messages.config_import_payload_missing(), "payload");
        }

        LarkConfigSnapshot snapshot = JsonUtils.readValue(payload, LarkConfigSnapshot.class);
        LarkConfigSnapshotValidator.validateForImport(snapshot);

        LarkConfigImportMode mode;
        try {
            mode = LarkConfigImportMode.fromValue(modeValue);
        } catch (IllegalArgumentException ex) {
            throw new FormException(Messages.config_import_mode_invalid(StringUtils.defaultIfBlank(modeValue, "null")), "mode");
        }

        return new ParsedImportRequest(mode, LarkConfigSnapshotMapper.fromSnapshot(snapshot));
    }

    private static String buildInvalidPayloadMessage(Exception ex) {
        String detail = StringUtils.defaultIfBlank(ex.getMessage(), ex.getClass().getSimpleName());
        return Messages.config_import_payload_invalid(detail);
    }

    /**
     * Parsed import request bundle used by preview and apply flows.
     */
    private static final class ParsedImportRequest {
        private final LarkConfigImportMode mode;
        private final LarkConfigSnapshotMapper.ImportedGlobalConfig imported;

        private ParsedImportRequest(LarkConfigImportMode mode, LarkConfigSnapshotMapper.ImportedGlobalConfig imported) {
            this.mode = mode;
            this.imported = imported;
        }

        private LarkConfigImportMode getMode() {
            return mode;
        }

        private LarkConfigSnapshotMapper.ImportedGlobalConfig getImported() {
            return imported;
        }
    }
}
