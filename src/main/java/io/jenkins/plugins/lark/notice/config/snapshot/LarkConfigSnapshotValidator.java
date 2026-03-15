package io.jenkins.plugins.lark.notice.config.snapshot;

import hudson.model.Descriptor.FormException;
import hudson.util.VersionNumber;
import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.enums.NoticeOccasionEnum;
import io.jenkins.plugins.lark.notice.enums.RobotType;
import io.jenkins.plugins.lark.notice.enums.SecurityPolicyEnum;
import org.apache.commons.lang3.StringUtils;

import java.net.Proxy;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates imported configuration snapshots before applying them.
 *
 * @author xm.z
 */
public final class LarkConfigSnapshotValidator {

    private static final String IMPORT_FIELD = "payload";

    private LarkConfigSnapshotValidator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Validates one imported snapshot before it is applied to the live global configuration.
     *
     * @param snapshot imported snapshot payload
     * @throws FormException when the snapshot is incomplete, unsupported, or semantically invalid
     */
    public static void validateForImport(LarkConfigSnapshot snapshot) throws FormException {
        validateForImport(snapshot, LarkConfigSnapshotMapper.resolvePluginVersion());
    }

    static void validateForImport(LarkConfigSnapshot snapshot, String currentPluginVersion) throws FormException {
        if (snapshot == null) {
            throw new FormException(Messages.config_import_payload_missing(), IMPORT_FIELD);
        }
        if (snapshot.getSchemaVersion() == null || snapshot.getSchemaVersion() != LarkConfigSnapshot.CURRENT_SCHEMA_VERSION) {
            throw new FormException(Messages.config_import_schema_unsupported(), IMPORT_FIELD);
        }
        validatePluginCompatibility(snapshot.getPluginVersion(), currentPluginVersion);
        validateNoticeOccasions(snapshot.getNoticeOccasions());
        validateProxy(snapshot.getProxyConfig());
        validateRobots(snapshot.getRobotConfigs());
    }

    private static void validatePluginCompatibility(String snapshotPluginVersion, String currentPluginVersion) throws FormException {
        if (StringUtils.isBlank(snapshotPluginVersion) || StringUtils.isBlank(currentPluginVersion)) {
            return;
        }
        try {
            VersionNumber snapshotVersion = new VersionNumber(snapshotPluginVersion);
            VersionNumber currentVersion = new VersionNumber(currentPluginVersion);
            if (snapshotVersion.compareTo(currentVersion) > 0) {
                throw new FormException(
                        Messages.config_import_plugin_version_unsupported(snapshotPluginVersion, currentPluginVersion),
                        IMPORT_FIELD
                );
            }
        } catch (IllegalArgumentException ignored) {
            // Ignore non-standard plugin versions and continue with schema validation.
        }
    }

    private static void validateNoticeOccasions(Set<String> noticeOccasions) throws FormException {
        if (noticeOccasions == null) {
            return;
        }
        for (String occasion : noticeOccasions) {
            try {
                NoticeOccasionEnum.valueOf(occasion);
            } catch (Exception ex) {
                throw new FormException(Messages.config_import_notice_occasion_invalid(occasion), IMPORT_FIELD);
            }
        }
    }

    private static void validateProxy(ProxySnapshot proxyConfig) throws FormException {
        if (proxyConfig == null) {
            return;
        }
        Proxy.Type type;
        try {
            type = proxyConfig.getType() == null ? Proxy.Type.DIRECT : Proxy.Type.valueOf(proxyConfig.getType());
        } catch (Exception ex) {
            throw new FormException(Messages.config_import_proxy_invalid(), IMPORT_FIELD);
        }
        if (!proxyConfig.isEnabled() || type == Proxy.Type.DIRECT) {
            return;
        }
        if (StringUtils.isBlank(proxyConfig.getHost()) || proxyConfig.getPort() == null || proxyConfig.getPort() <= 0) {
            throw new FormException(Messages.config_import_proxy_invalid(), IMPORT_FIELD);
        }
    }

    private static void validateRobots(List<RobotSnapshot> robotConfigs) throws FormException {
        if (robotConfigs == null) {
            return;
        }
        Set<String> robotIds = new HashSet<>();
        for (RobotSnapshot robotConfig : robotConfigs) {
            validateRobot(robotConfig, robotIds);
        }
    }

    private static void validateRobot(RobotSnapshot robotConfig, Set<String> robotIds) throws FormException {
        if (robotConfig == null) {
            throw new FormException(Messages.config_import_robot_invalid(), IMPORT_FIELD);
        }
        if (StringUtils.isBlank(robotConfig.getId()) || !robotIds.add(robotConfig.getId())) {
            throw new FormException(Messages.config_import_robot_id_invalid(), IMPORT_FIELD);
        }
        if (StringUtils.isBlank(robotConfig.getName())) {
            throw new FormException(Messages.form_validation_name_required(), IMPORT_FIELD);
        }
        if (StringUtils.isBlank(robotConfig.getWebhook()) || RobotType.fromUrl(robotConfig.getWebhook()) == null) {
            throw new FormException(Messages.form_validation_webhook_invalid(), IMPORT_FIELD);
        }
        validateRetry(robotConfig.getRetryConfig());
        validateSecurityPolicies(robotConfig.getSecurityPolicyConfigs());
    }

    private static void validateRetry(RetrySnapshot retryConfig) throws FormException {
        if (retryConfig == null) {
            return;
        }
        if (retryConfig.getMaxAttempts() < 1
                || retryConfig.getInitialDelayMs() < 0
                || retryConfig.getMaxDelayMs() < retryConfig.getInitialDelayMs()
                || retryConfig.getBackoffMultiplier() < 1.0d
                || retryConfig.getJitterRatio() < 0.0d
                || retryConfig.getJitterRatio() > 1.0d) {
            throw new FormException(Messages.config_import_retry_invalid(), IMPORT_FIELD);
        }
    }

    private static void validateSecurityPolicies(List<SecurityPolicySnapshot> securityPolicies) throws FormException {
        if (securityPolicies == null) {
            return;
        }
        Set<String> types = new HashSet<>();
        for (SecurityPolicySnapshot securityPolicy : securityPolicies) {
            if (securityPolicy == null || StringUtils.isBlank(securityPolicy.getType()) || !types.add(securityPolicy.getType())) {
                throw new FormException(Messages.config_import_security_policy_invalid(), IMPORT_FIELD);
            }
            try {
                SecurityPolicyEnum.valueOf(securityPolicy.getType());
            } catch (Exception ex) {
                throw new FormException(Messages.config_import_security_policy_invalid(), IMPORT_FIELD);
            }
        }
    }
}
