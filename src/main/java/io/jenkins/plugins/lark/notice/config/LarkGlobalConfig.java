package io.jenkins.plugins.lark.notice.config;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import io.jenkins.plugins.lark.notice.config.LarkRobotConfig.LarkRobotConfigDescriptor;
import io.jenkins.plugins.lark.notice.config.security.LarkPermissions;
import io.jenkins.plugins.lark.notice.enums.NoticeOccasionEnum;
import io.jenkins.plugins.lark.notice.sdk.MessageDispatcher;
import jenkins.model.Jenkins;
import lombok.Getter;
import lombok.ToString;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest2;

import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Global configuration for the Lark notification plugin. This class stores settings that are global to Jenkins,
 * such as proxy configurations, verbosity level, occasions for notifications, and configurations for individual Lark robots.
 *
 * @author xm.z
 */
@Getter
@ToString
@Extension
@SuppressWarnings("unused")
public class LarkGlobalConfig extends Descriptor<LarkGlobalConfig> implements Describable<LarkGlobalConfig> {

    private LarkProxyConfig proxyConfig;
    private boolean verbose;
    private Set<String> noticeOccasions = Arrays.stream(NoticeOccasionEnum.values()).map(Enum::name).collect(Collectors.toSet());
    private ArrayList<LarkRobotConfig> robotConfigs = new ArrayList<>();

    /**
     * Data-bound constructor for setting up global Lark notification configurations.
     *
     * @param proxyConfig     Configuration for proxy if it is needed.
     * @param verbose         Whether to enable verbose logging for the plugin.
     * @param noticeOccasions Occasions on which notifications should be sent.
     * @param robotConfigs    Configurations for individual Lark robots.
     */
    @DataBoundConstructor
    public LarkGlobalConfig(LarkProxyConfig proxyConfig, boolean verbose,
                            Set<String> noticeOccasions, ArrayList<LarkRobotConfig> robotConfigs) {
        this.proxyConfig = proxyConfig;
        this.verbose = verbose;
        this.noticeOccasions = noticeOccasions;
        this.robotConfigs = robotConfigs;
    }

    /**
     * Default constructor that loads saved configurations or initializes the class with default values.
     */
    public LarkGlobalConfig() {
        super(LarkGlobalConfig.class);
        load(); // Load saved configuration from disk
    }

    /**
     * Gets the singleton instance of the LarkGlobalConfig.
     *
     * @return The singleton instance of LarkGlobalConfig.
     */
    public static LarkGlobalConfig getInstance() {
        return Jenkins.get().getDescriptorByType(LarkGlobalConfig.class);
    }

    /**
     * Retrieves a specific Lark robot configuration by its ID.
     *
     * @param robotId The ID of the robot whose configuration is to be retrieved.
     * @return An Optional containing the LarkRobotConfig if found, otherwise an empty Optional.
     */
    public static Optional<LarkRobotConfig> getRobot(String robotId) {
        return getInstance().robotConfigs.stream()
                .filter(item -> robotId.equals(item.getId()))
                .findAny();
    }

    /**
     * Obtains a ProxySelector based on the current proxy configuration.
     *
     * @return A ProxySelector if proxy is configured, otherwise null.
     */
    public ProxySelector obtainProxySelector() {
        return proxyConfig == null ? null : proxyConfig.obtainProxySelector();
    }

    @DataBoundSetter
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @DataBoundSetter
    public void setNoticeOccasions(Set<String> noticeOccasions) {
        this.noticeOccasions = noticeOccasions;
    }

    @DataBoundSetter
    public void setProxyConfig(LarkProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

    @DataBoundSetter
    public void setRobotConfigs(ArrayList<LarkRobotConfig> robotConfigs) {
        MessageDispatcher.getInstance().clearSenders();
        this.robotConfigs = robotConfigs;
    }

    /**
     * Customizes the behavior when the global configuration form is submitted.
     * Filters out robot configs without a webhook URL.
     *
     * @param req  The StaplerRequest containing the form submission.
     * @param json The JSONObject representing the submitted form data.
     * @return true if successful, throwing FormException otherwise.
     * @throws FormException If there's an error processing the form submission.
     */
    @Override
    public boolean configure(StaplerRequest2 req, JSONObject json) throws FormException {
        // At the beginning of your Stapler web method
        Jenkins.get().checkPermission(LarkPermissions.CONFIGURE);

        Object robotConfigObj = json.get("robotConfigs");
        if (robotConfigObj == null) {
            json.put("robotConfigs", new JSONArray());
        } else {
            JSONArray robotConfigs = JSONArray.fromObject(robotConfigObj);
            robotConfigs.removeIf(item -> {
                JSONObject jsonObject = JSONObject.fromObject(item);
                String webhook = jsonObject.getString("webhook");
                return StringUtils.isEmpty(webhook);
            });
        }

        req.bindJSON(this, json);
        // Additional form processing can be done here
        save(); // Save the configuration to disk
        return super.configure(req, json);
    }

    /**
     * Returns all possible notice occasions as defined in NoticeOccasionEnum.
     *
     * @return An array of NoticeOccasionEnum values.
     */
    public NoticeOccasionEnum[] getAllNoticeOccasions() {
        return NoticeOccasionEnum.values();
    }

    @Override
    public Descriptor<LarkGlobalConfig> getDescriptor() {
        return this;
    }

    /**
     * Additional getters for UI binding
     */
    public LarkProxyConfig getLarkProxyConfig() {
        return Jenkins.get().getDescriptorByType(LarkProxyConfig.class);
    }

    public LarkRobotConfigDescriptor getLarkRobotConfigDescriptor() {
        return Jenkins.get().getDescriptorByType(LarkRobotConfigDescriptor.class);
    }
}
