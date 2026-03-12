package io.jenkins.plugins.lark.notice.config.snapshot;

import hudson.PluginWrapper;
import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import io.jenkins.plugins.lark.notice.config.LarkProxyConfig;
import io.jenkins.plugins.lark.notice.config.LarkRetryConfig;
import io.jenkins.plugins.lark.notice.config.LarkRobotConfig;
import io.jenkins.plugins.lark.notice.config.LarkSecurityPolicyConfig;
import jenkins.model.Jenkins;
import org.apache.commons.lang3.StringUtils;

import java.net.Proxy;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Maps between live configuration objects and import/export snapshots.
 *
 * @author xm.z
 */
public final class LarkConfigSnapshotMapper {

    private LarkConfigSnapshotMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converts the live global configuration into a serializable snapshot.
     *
     * @param globalConfig current live configuration
     * @return exported snapshot
     */
    public static LarkConfigSnapshot toSnapshot(LarkGlobalConfig globalConfig) {
        LarkConfigSnapshot snapshot = new LarkConfigSnapshot();
        snapshot.setPluginVersion(resolvePluginVersion());
        snapshot.setExportedAt(OffsetDateTime.now(ZoneOffset.UTC).toString());
        snapshot.setVerbose(globalConfig.isVerbose());
        snapshot.setNoticeOccasions(new LinkedHashSet<>(globalConfig.getNoticeOccasions()));
        snapshot.setProxyConfig(toProxySnapshot(globalConfig.getProxyConfig()));

        List<RobotSnapshot> robots = globalConfig.getRobotConfigs().stream()
                .map(LarkConfigSnapshotMapper::toRobotSnapshot)
                .toList();
        snapshot.setRobotConfigs(new ArrayList<>(robots));
        return snapshot;
    }

    /**
     * Converts one imported snapshot into values ready to be applied to the live descriptor.
     *
     * @param snapshot imported snapshot
     * @return converted configuration payload
     */
    public static ImportedGlobalConfig fromSnapshot(LarkConfigSnapshot snapshot) {
        ImportedGlobalConfig imported = new ImportedGlobalConfig();
        imported.setVerbose(snapshot.isVerbose());
        imported.setNoticeOccasions(snapshot.getNoticeOccasions() == null
                ? new LinkedHashSet<>()
                : new LinkedHashSet<>(snapshot.getNoticeOccasions()));
        imported.setProxyConfig(toProxyConfig(snapshot.getProxyConfig()));

        ArrayList<LarkRobotConfig> robotConfigs = new ArrayList<>();
        if (snapshot.getRobotConfigs() != null) {
            for (RobotSnapshot robotSnapshot : snapshot.getRobotConfigs()) {
                robotConfigs.add(toRobotConfig(robotSnapshot));
            }
        }
        imported.setRobotConfigs(robotConfigs);
        return imported;
    }

    static LarkProxyConfig copyProxyConfig(LarkProxyConfig proxyConfig) {
        return toProxyConfig(toProxySnapshot(proxyConfig));
    }

    static LarkRobotConfig copyRobotConfig(LarkRobotConfig robotConfig) {
        if (robotConfig == null) {
            return null;
        }
        return toRobotConfig(toRobotSnapshot(robotConfig));
    }

    private static ProxySnapshot toProxySnapshot(LarkProxyConfig proxyConfig) {
        if (proxyConfig == null) {
            return null;
        }
        ProxySnapshot snapshot = new ProxySnapshot();
        snapshot.setEnabled(proxyConfig.isEnabled());
        snapshot.setHost(proxyConfig.getHost());
        snapshot.setPort(proxyConfig.getPort());
        snapshot.setType(proxyConfig.getType() == null ? Proxy.Type.DIRECT.name() : proxyConfig.getType().name());
        return snapshot;
    }

    private static RobotSnapshot toRobotSnapshot(LarkRobotConfig robotConfig) {
        RobotSnapshot snapshot = new RobotSnapshot();
        snapshot.setId(robotConfig.getId());
        snapshot.setName(robotConfig.getName());
        snapshot.setWebhook(robotConfig.getWebhook());
        snapshot.setRetryConfig(toRetrySnapshot(robotConfig.getRetryConfig()));

        List<SecurityPolicySnapshot> policies = robotConfig.getSecurityPolicyConfigs().stream()
                .filter(policyConfig -> StringUtils.isNotBlank(policyConfig.getValue()))
                .map(LarkConfigSnapshotMapper::toSecurityPolicySnapshot)
                .toList();
        snapshot.setSecurityPolicyConfigs(new ArrayList<>(policies));
        return snapshot;
    }

    private static SecurityPolicySnapshot toSecurityPolicySnapshot(LarkSecurityPolicyConfig policyConfig) {
        SecurityPolicySnapshot snapshot = new SecurityPolicySnapshot();
        snapshot.setType(policyConfig.getType());
        snapshot.setDesc(policyConfig.getDesc());
        snapshot.setValue(policyConfig.getValue());
        return snapshot;
    }

    private static RetrySnapshot toRetrySnapshot(LarkRetryConfig retryConfig) {
        if (retryConfig == null) {
            return null;
        }
        RetrySnapshot snapshot = new RetrySnapshot();
        snapshot.setEnabled(retryConfig.isEnabled());
        snapshot.setMaxAttempts(retryConfig.getMaxAttempts());
        snapshot.setInitialDelayMs(retryConfig.getInitialDelayMs());
        snapshot.setMaxDelayMs(retryConfig.getMaxDelayMs());
        snapshot.setBackoffMultiplier(retryConfig.getBackoffMultiplier());
        snapshot.setJitterRatio(retryConfig.getJitterRatio());
        return snapshot;
    }

    private static LarkProxyConfig toProxyConfig(ProxySnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        LarkProxyConfig proxyConfig = new LarkProxyConfig();
        proxyConfig.setEnabled(snapshot.isEnabled());
        proxyConfig.setHost(snapshot.getHost());
        proxyConfig.setPort(snapshot.getPort());
        proxyConfig.setType(snapshot.getType() == null ? null : Proxy.Type.valueOf(snapshot.getType()));
        return proxyConfig;
    }

    private static LarkRobotConfig toRobotConfig(RobotSnapshot snapshot) {
        LarkRobotConfig robotConfig = new LarkRobotConfig(
                snapshot.getId(),
                snapshot.getName(),
                snapshot.getWebhook(),
                toSecurityPolicyConfigs(snapshot.getSecurityPolicyConfigs())
        );
        robotConfig.setRetryConfig(toRetryConfig(snapshot.getRetryConfig()));
        return robotConfig;
    }

    private static List<LarkSecurityPolicyConfig> toSecurityPolicyConfigs(List<SecurityPolicySnapshot> snapshots) {
        if (snapshots == null) {
            return List.of();
        }
        return snapshots.stream()
                .map(snapshot -> new LarkSecurityPolicyConfig(snapshot.getType(), snapshot.getValue(), snapshot.getDesc()))
                .toList();
    }

    private static LarkRetryConfig toRetryConfig(RetrySnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        return new LarkRetryConfig(
                snapshot.isEnabled(),
                snapshot.getMaxAttempts(),
                snapshot.getInitialDelayMs(),
                snapshot.getMaxDelayMs(),
                snapshot.getBackoffMultiplier(),
                snapshot.getJitterRatio()
        );
    }

    private static String resolvePluginVersion() {
        Jenkins jenkins = Jenkins.getInstanceOrNull();
        if (jenkins == null) {
            return null;
        }
        PluginWrapper plugin = jenkins.getPluginManager().getPlugin("lark-notice");
        return plugin == null ? null : plugin.getVersion();
    }

    /**
     * Imported configuration bundle ready to be applied to the live descriptor.
     *
     * @author xm.z
     */
    public static class ImportedGlobalConfig {
        private LarkProxyConfig proxyConfig;
        private boolean verbose;
        private Set<String> noticeOccasions = new LinkedHashSet<>();
        private ArrayList<LarkRobotConfig> robotConfigs = new ArrayList<>();

        /**
         * Returns the imported proxy configuration.
         *
         * @return proxy configuration, or {@code null} when omitted
         */
        public LarkProxyConfig getProxyConfig() {
            return proxyConfig;
        }

        /**
         * Sets the imported proxy configuration.
         *
         * @param proxyConfig proxy configuration from the snapshot
         */
        public void setProxyConfig(LarkProxyConfig proxyConfig) {
            this.proxyConfig = proxyConfig;
        }

        /**
         * Returns the imported verbose logging flag.
         *
         * @return {@code true} when verbose logging should be enabled
         */
        public boolean isVerbose() {
            return verbose;
        }

        /**
         * Sets the imported verbose logging flag.
         *
         * @param verbose imported verbose flag
         */
        public void setVerbose(boolean verbose) {
            this.verbose = verbose;
        }

        /**
         * Returns the imported notice occasions.
         *
         * @return imported notice occasions
         */
        public Set<String> getNoticeOccasions() {
            return noticeOccasions;
        }

        /**
         * Sets the imported notice occasions.
         *
         * @param noticeOccasions imported notice occasion names
         */
        public void setNoticeOccasions(Set<String> noticeOccasions) {
            this.noticeOccasions = noticeOccasions;
        }

        /**
         * Returns the imported robot configurations.
         *
         * @return imported robot configuration list
         */
        public ArrayList<LarkRobotConfig> getRobotConfigs() {
            return robotConfigs;
        }

        /**
         * Sets the imported robot configurations.
         *
         * @param robotConfigs imported robot configurations
         */
        public void setRobotConfigs(ArrayList<LarkRobotConfig> robotConfigs) {
            this.robotConfigs = robotConfigs;
        }
    }
}
