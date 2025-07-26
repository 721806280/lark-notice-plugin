package io.jenkins.plugins.lark.notice.config;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.User;
import hudson.util.FormValidation;
import hudson.util.Secret;
import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.enums.BuildStatusEnum;
import io.jenkins.plugins.lark.notice.enums.MsgTypeEnum;
import io.jenkins.plugins.lark.notice.enums.RobotType;
import io.jenkins.plugins.lark.notice.enums.SecurityPolicyEnum;
import io.jenkins.plugins.lark.notice.model.BuildJobModel;
import io.jenkins.plugins.lark.notice.model.MessageModel;
import io.jenkins.plugins.lark.notice.model.RobotConfigModel;
import io.jenkins.plugins.lark.notice.sdk.MessageSender;
import io.jenkins.plugins.lark.notice.sdk.model.SendResult;
import io.jenkins.plugins.lark.notice.tools.JsonUtils;
import jenkins.model.Jenkins;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

import java.net.ProxySelector;
import java.util.*;
import java.util.stream.Collectors;

import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.DEFAULT_TITLE;
import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.NOTICE_ICON;

/**
 * Configuration class for Lark robot, including robot ID, name, Webhook key, and a list of security policy configurations
 *
 * @author xm.z
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@SuppressWarnings("unused")
public class LarkRobotConfig implements Describable<LarkRobotConfig> {

    /**
     * Robot ID.
     */
    private String id;

    /**
     * Robot name.
     */
    private String name;

    /**
     * Webhook key, implemented using Jenkins' Secret.
     */
    private Secret webhook;

    /**
     * List of security policy configurations, includes a set of Key-Value pairs.
     */
    private List<LarkSecurityPolicyConfig> securityPolicyConfigs;

    /**
     * Constructor for initializing robot configuration object
     *
     * @param id                    Robot ID
     * @param name                  Robot name
     * @param webhook               Webhook key
     * @param securityPolicyConfigs List of security policy configurations
     */
    @DataBoundConstructor
    public LarkRobotConfig(String id, String name, String webhook, List<LarkSecurityPolicyConfig> securityPolicyConfigs) {
        this.id = StringUtils.defaultIfBlank(id, UUID.randomUUID().toString());
        this.name = name;
        this.webhook = Secret.fromString(webhook);
        this.securityPolicyConfigs = securityPolicyConfigs;
    }

    /**
     * Gets robot ID, generates a UUID if empty
     *
     * @return Robot ID
     */
    public String getId() {
        if (StringUtils.isBlank(id)) {
            setId(UUID.randomUUID().toString());
        }
        return id;
    }

    /**
     * Gets the plaintext value of the Webhook key
     *
     * @return Webhook key plaintext value
     */
    public String getWebhook() {
        if (webhook == null) {
            return null;
        }
        return webhook.getPlainText();
    }

    public RobotType obtainRobotType() {
        String webhook = getWebhook();
        if (StringUtils.isBlank(webhook)) {
            return null;
        }
        return RobotType.fromUrl(webhook);
    }

    /**
     * Gets the list of security policy configurations, includes a set of Key-Value pairs
     *
     * @return List of security policy configurations
     */
    public List<LarkSecurityPolicyConfig> getSecurityPolicyConfigs() {
        return Arrays.stream(SecurityPolicyEnum.values()).map(enumItem -> {
            LarkSecurityPolicyConfig policyConfig = LarkSecurityPolicyConfig.of(enumItem);
            if (securityPolicyConfigs != null) {
                Optional<LarkSecurityPolicyConfig> config = securityPolicyConfigs.stream()
                        .filter(configItem -> enumItem.name().equals(configItem.getType())).findAny();
                config.ifPresent(t -> policyConfig.setValue(t.getValue()));
            }
            return policyConfig;
        }).collect(Collectors.toList());
    }

    /**
     * Gets the descriptor for this class, used to display the robot configuration page in Jenkins
     *
     * @return Robot configuration page descriptor
     */
    @Override
    public Descriptor<LarkRobotConfig> getDescriptor() {
        return Jenkins.get().getDescriptorByType(LarkRobotConfigDescriptor.class);
    }

    /**
     * Descriptor for robot configuration page, used to display the robot configuration page in Jenkins
     */
    @Extension
    public static class LarkRobotConfigDescriptor extends Descriptor<LarkRobotConfig> {

        /**
         * Gets the descriptor for the security policy configuration page
         *
         * @return Security policy configuration page descriptor
         */
        public LarkSecurityPolicyConfig.LarkSecurityPolicyConfigDescriptor getLarkSecurityPolicyConfigDescriptor() {
            return Jenkins.get().getDescriptorByType(LarkSecurityPolicyConfig.LarkSecurityPolicyConfigDescriptor.class);
        }


        /**
         * Gets the default list of security policy configurations
         *
         * @return Default list of security policy configurations
         */
        public ArrayList<LarkSecurityPolicyConfig> getDefaultSecurityPolicyConfigs() {
            return Arrays.stream(SecurityPolicyEnum.values())
                    .map(LarkSecurityPolicyConfig::of)
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        /**
         * Validates whether the robot name is empty
         *
         * @param value Robot name
         * @return Validation result, returns FormValidation.ok() if validation passes, otherwise returns an error message
         */
        @RequirePOST
        public FormValidation doCheckName(@QueryParameter String value) {
            if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                return FormValidation.error("You do not have permission to access this resource");
            }
            return StringUtils.isNotBlank(value) ? FormValidation.ok() :
                    FormValidation.error(Messages.form_validation_name());
        }

        /**
         * Validates whether the Webhook key is empty
         *
         * @param value Webhook key
         * @return Validation result, returns FormValidation.ok() if validation passes, otherwise returns an error message
         */
        @RequirePOST
        public FormValidation doCheckWebhook(@QueryParameter String value) {
            if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                return FormValidation.error("You do not have permission to access this resource");
            }
            return StringUtils.isBlank(value) || Objects.isNull(RobotType.fromUrl(value)) ?
                    FormValidation.error(Messages.form_validation_webhook()) : FormValidation.ok();
        }

        /**
         * Tests whether the robot configuration works properly.
         *
         * @param id              The robot ID.
         * @param name            The robot's name.
         * @param webhook         The Webhook key.
         * @param proxy           The proxy settings.
         * @param securityConfigs The security configs.
         * @return Returns the test result. If the test passes, it returns FormValidation.respond(Kind.OK); otherwise, it returns an error message.
         */
        @RequirePOST
        public String doTest(@QueryParameter String id, @QueryParameter String name,
                             @QueryParameter String webhook, @QueryParameter String proxy,
                             @QueryParameter String securityConfigs) {
            if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                return "Error: You do not have permission to access this resource.";
            }

            List<LarkSecurityPolicyConfig> securityPolicyConfigs = JsonUtils.readList(securityConfigs, LarkSecurityPolicyConfig.class)
                    .stream().filter(config -> StringUtils.isNotBlank(config.getValue())).toList();

            LarkRobotConfig robotConfig = new LarkRobotConfig(id, name, webhook, securityPolicyConfigs);

            ProxySelector proxySelector = Optional.ofNullable(JsonUtils.readValue(proxy, LarkProxyConfig.class))
                    .orElseGet(LarkProxyConfig::new).obtainProxySelector();

            RobotType robotType = robotConfig.obtainRobotType();
            if (Objects.isNull(robotType)) {
                return "Error: " + Messages.form_validation_webhook();
            }

            MessageSender sender = robotType.obtainInstance(RobotConfigModel.of(robotConfig, proxySelector));
            SendResult sendResult = sender.sendCard(buildTestMessage(robotType));

            return sendResult.isOk() ? Messages.form_validation_test_success() : "Error: " + sendResult.getMsg();
        }

        /**
         * Builds a message model for testing the robot configuration.
         *
         * @return The test message model.
         */
        private MessageModel buildTestMessage(RobotType robotType) {
            String rootUrl = Jenkins.get().getRootUrl();
            User user = Optional.ofNullable(User.current()).orElse(User.getUnknown());

            BuildJobModel buildJobModel = BuildJobModel.builder().projectName("Lark Notice Plugin").title(DEFAULT_TITLE)
                    .projectUrl(rootUrl).jobName("System Configuration").jobUrl(rootUrl + "/configure")
                    .statusType(BuildStatusEnum.SUCCESS).duration("-")
                    .executorName(user.getDisplayName()).build();

            return MessageModel.builder().type(MsgTypeEnum.CARD)
                    .title(NOTICE_ICON + " Test Successful")
                    .text(buildJobModel.toMarkdown(robotType))
                    .atAll(false).build();
        }
    }
}