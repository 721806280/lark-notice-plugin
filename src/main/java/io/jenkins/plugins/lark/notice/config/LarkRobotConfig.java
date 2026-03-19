package io.jenkins.plugins.lark.notice.config;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.User;
import hudson.util.FormValidation;
import hudson.util.Secret;
import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.config.security.LarkPermissions;
import io.jenkins.plugins.lark.notice.enums.BuildStatusEnum;
import io.jenkins.plugins.lark.notice.enums.MessageLocaleStrategy;
import io.jenkins.plugins.lark.notice.enums.MsgTypeEnum;
import io.jenkins.plugins.lark.notice.enums.RobotProtocolType;
import io.jenkins.plugins.lark.notice.enums.RobotType;
import io.jenkins.plugins.lark.notice.enums.SecurityPolicyEnum;
import io.jenkins.plugins.lark.notice.enums.WebhookEndpointMode;
import io.jenkins.plugins.lark.notice.i18n.NoticeI18n;
import io.jenkins.plugins.lark.notice.model.BuildJobModel;
import io.jenkins.plugins.lark.notice.model.MessageModel;
import io.jenkins.plugins.lark.notice.model.RobotConfigModel;
import io.jenkins.plugins.lark.notice.sdk.MessageDispatcher;
import io.jenkins.plugins.lark.notice.sdk.MessageSender;
import io.jenkins.plugins.lark.notice.sdk.model.SendResult;
import io.jenkins.plugins.lark.notice.tools.HttpResponses;
import io.jenkins.plugins.lark.notice.tools.JsonUtils;
import jenkins.model.Jenkins;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

import java.net.ProxySelector;
import java.util.*;
import java.util.Locale;
import java.util.stream.Collectors;

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
     * Explicit protocol family used to resolve the sender implementation.
     */
    private RobotProtocolType protocolType;

    /**
     * UI input mode used to collect the webhook endpoint.
     */
    private WebhookEndpointMode endpointMode;

    /**
     * Transient base URL used by the "base URL + token" input mode.
     */
    private transient String baseUrl;

    /**
     * Transient webhook token used by the "base URL + token" input mode.
     */
    private transient String webhookToken;

    /**
     * List of security policy configurations, includes a set of Key-Value pairs.
     */
    private List<LarkSecurityPolicyConfig> securityPolicyConfigs;

    /**
     * Retry configuration for this robot.
     */
    private LarkRetryConfig retryConfig;

    /**
     * Locale strategy used by built-in default message content for this robot.
     */
    private MessageLocaleStrategy messageLocaleStrategy = MessageLocaleStrategy.SYSTEM_DEFAULT;

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
        this.securityPolicyConfigs = copySecurityPolicyConfigs(securityPolicyConfigs);
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

    /**
     * Returns the configured protocol family, inferring it from the existing webhook for legacy configurations.
     *
     * @return configured or inferred protocol family
     */
    public RobotProtocolType getProtocolType() {
        RobotProtocolType inferred = protocolType != null ? protocolType : RobotProtocolType.inferFromWebhook(getWebhook());
        return inferred == null ? RobotProtocolType.LARK_COMPATIBLE : inferred;
    }

    /**
     * Returns the selected endpoint mode, defaulting old configurations to the full webhook mode.
     *
     * @return endpoint mode
     */
    public WebhookEndpointMode getEndpointMode() {
        return RobotWebhookResolver.resolveEndpointMode(getProtocolType(), endpointMode, baseUrl, webhookToken);
    }

    /**
     * Returns the current base URL input value, deriving it from the canonical webhook when possible.
     *
     * @return base URL for token mode
     */
    public String getBaseUrl() {
        return StringUtils.defaultIfBlank(baseUrl, RobotWebhookResolver.extractBaseUrl(getWebhook()));
    }

    /**
     * Returns the current webhook token input value, deriving it from the canonical webhook when possible.
     *
     * @return webhook token for token mode
     */
    public String getWebhookToken() {
        return StringUtils.defaultIfBlank(webhookToken, RobotWebhookResolver.extractWebhookToken(getWebhook()));
    }

    /**
     * Infers the robot type from the configured webhook URL.
     *
     * @return resolved robot type, or {@code null} when the webhook is blank or unsupported
     */
    public RobotType obtainRobotType() {
        String webhook = getWebhook();
        if (StringUtils.isBlank(webhook)) {
            return null;
        }
        RobotProtocolType resolvedProtocol = getProtocolType();
        return RobotWebhookResolver.isSupportedWebhook(resolvedProtocol, webhook)
                ? resolvedProtocol.toRobotType()
                : null;
    }

    /**
     * Returns the UI-facing provider label inferred from the same endpoint rules used by the global robot form.
     *
     * @return provider label shown to users
     */
    public String getProviderDisplayName() {
        if (RobotProtocolType.DING_TALK.equals(getProtocolType())) {
            return Messages.notifier_robot_provider_ding_talk();
        }

        String normalizedBaseUrl = RobotWebhookResolver.normalizeBaseUrl(getBaseUrl());
        if ("https://open.larksuite.com".equalsIgnoreCase(normalizedBaseUrl)
                || StringUtils.containsIgnoreCase(getWebhook(), "open.larksuite.com")) {
            return Messages.notifier_robot_provider_lark();
        }
        return Messages.notifier_robot_provider_feishu();
    }

    /**
     * Gets the list of security policy configurations, includes a set of Key-Value pairs
     *
     * @return List of security policy configurations
     */
    public List<LarkSecurityPolicyConfig> getSecurityPolicyConfigs() {
        Map<String, LarkSecurityPolicyConfig> configsByType = indexSecurityPoliciesByType();
        return Arrays.stream(SecurityPolicyEnum.values()).map(enumItem -> {
            LarkSecurityPolicyConfig policyConfig = LarkSecurityPolicyConfig.of(enumItem);
            Optional.ofNullable(configsByType.get(enumItem.name()))
                    .ifPresent(config -> policyConfig.setValue(config.getValue()));
            return policyConfig;
        }).collect(Collectors.toList());
    }

    /**
     * Returns retry configuration, falling back to defaults when none is configured.
     *
     * @return retry config
     */
    public LarkRetryConfig getRetryConfig() {
        return retryConfig == null ? LarkRetryConfig.defaultConfig() : retryConfig;
    }

    /**
     * Returns the message locale strategy configured for this robot.
     *
     * @return configured locale strategy, never {@code null}
     */
    public MessageLocaleStrategy getMessageLocaleStrategy() {
        return messageLocaleStrategy == null ? MessageLocaleStrategy.SYSTEM_DEFAULT : messageLocaleStrategy;
    }

    /**
     * Returns the locale strategy shown by the robot editor.
     *
     * @return explicit UI strategy
     */
    public MessageLocaleStrategy getUiMessageLocaleStrategy() {
        return getMessageLocaleStrategy().toSelectableStrategy();
    }

    private Map<String, LarkSecurityPolicyConfig> indexSecurityPoliciesByType() {
        if (securityPolicyConfigs == null || securityPolicyConfigs.isEmpty()) {
            return Collections.emptyMap();
        }
        return securityPolicyConfigs.stream()
                .collect(Collectors.toMap(
                        LarkSecurityPolicyConfig::getType,
                        config -> config,
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
    }

    private static List<LarkSecurityPolicyConfig> copySecurityPolicyConfigs(List<LarkSecurityPolicyConfig> securityPolicyConfigs) {
        return securityPolicyConfigs == null ? null : new ArrayList<>(securityPolicyConfigs);
    }

    /**
     * Updates retry configuration for this robot.
     *
     * @param retryConfig retry configuration, or null to reset to defaults
     */
    @DataBoundSetter
    public void setRetryConfig(LarkRetryConfig retryConfig) {
        this.retryConfig = retryConfig;
    }

    /**
     * Updates the locale strategy used for built-in default messages sent by this robot.
     *
     * @param messageLocaleStrategy locale strategy, or {@code null} to reset to system default
     */
    @DataBoundSetter
    public void setMessageLocaleStrategy(MessageLocaleStrategy messageLocaleStrategy) {
        this.messageLocaleStrategy = messageLocaleStrategy == null
                ? MessageLocaleStrategy.SYSTEM_DEFAULT
                : messageLocaleStrategy;
    }

    /**
     * Updates the robot protocol family used for sender selection.
     *
     * @param protocolType protocol family
     */
    @DataBoundSetter
    public void setProtocolType(RobotProtocolType protocolType) {
        this.protocolType = protocolType;
    }

    /**
     * Updates the endpoint input mode used by the UI.
     *
     * @param endpointMode endpoint mode
     */
    @DataBoundSetter
    public void setEndpointMode(WebhookEndpointMode endpointMode) {
        this.endpointMode = endpointMode;
    }

    /**
     * Updates the transient base URL used by the token input mode.
     *
     * @param baseUrl base URL
     */
    @DataBoundSetter
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = StringUtils.trimToEmpty(baseUrl);
    }

    /**
     * Updates the transient webhook token used by the token input mode.
     *
     * @param webhookToken webhook token
     */
    @DataBoundSetter
    public void setWebhookToken(String webhookToken) {
        this.webhookToken = StringUtils.trimToEmpty(webhookToken);
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
         * Gets the retry config descriptor for UI binding.
         *
         * @return retry config descriptor
         */
        public LarkRetryConfig getLarkRetryConfigDescriptor() {
            return Jenkins.get().getDescriptorByType(LarkRetryConfig.class);
        }

        /**
         * Returns the explicit locale strategy used to seed new robot forms.
         *
         * @return default UI locale strategy
         */
        public MessageLocaleStrategy getDefaultUiMessageLocaleStrategy() {
            return MessageLocaleStrategy.fromLocale(Locale.getDefault());
        }

        /**
         * Validates whether the robot name is empty
         *
         * @param value Robot name
         * @return Validation result, returns FormValidation.ok() if validation passes, otherwise returns an error message
         */
        @RequirePOST
        public FormValidation doCheckName(@QueryParameter String value) {
            if (!Jenkins.get().hasPermission(LarkPermissions.CONFIGURE)) {
                return FormValidation.error(Messages.form_validation_permission_denied());
            }
            return StringUtils.isNotBlank(value) ? FormValidation.ok() :
                    FormValidation.error(Messages.form_validation_name_required());
        }

        /**
         * Validates whether the Webhook key is empty
         *
         * @param value Webhook key
         * @return Validation result, returns FormValidation.ok() if validation passes, otherwise returns an error message
         */
        @RequirePOST
        public FormValidation doCheckWebhook(@QueryParameter String value) {
            if (!Jenkins.get().hasPermission(LarkPermissions.CONFIGURE)) {
                return FormValidation.error(Messages.form_validation_permission_denied());
            }
            return !RobotType.isSupportedWebhook(value) ?
                    FormValidation.error(Messages.form_validation_webhook_invalid()) : FormValidation.ok();
        }

        /**
         * Tests whether the robot configuration works properly.
         *
         * @param id              The robot ID.
         * @param name            The robot's name.
         * @param webhook         The Webhook key.
         * @param proxy           The proxy settings.
         * @param securityConfigs The security configs.
         * @return Test result JSON with keys: ok(boolean), message(string).
         */
        @RequirePOST
        public HttpResponse doTest(@QueryParameter String id, @QueryParameter String name,
                                   @QueryParameter String protocolType, @QueryParameter String endpointMode,
                                   @QueryParameter String webhook, @QueryParameter String baseUrl,
                                   @QueryParameter String webhookToken, @QueryParameter String proxy,
                                   @QueryParameter String securityConfigs,
                                   @QueryParameter String messageLocaleStrategy) {
            Jenkins.get().checkPermission(LarkPermissions.CONFIGURE);

            JSONObject response = new JSONObject();
            try {
                List<LarkSecurityPolicyConfig> securityPolicyConfigs = parseSecurityPolicyConfigs(securityConfigs);
                RobotProtocolType resolvedProtocolType = RobotWebhookResolver.resolveProtocolType(
                        RobotProtocolType.fromValue(protocolType), webhook, baseUrl, webhookToken);
                WebhookEndpointMode resolvedEndpointMode = RobotWebhookResolver.resolveEndpointMode(
                        resolvedProtocolType, WebhookEndpointMode.fromValue(endpointMode), baseUrl, webhookToken);
                String resolvedWebhook = RobotWebhookResolver.resolveWebhook(
                        resolvedProtocolType, resolvedEndpointMode, webhook, baseUrl, webhookToken);
                LarkRobotConfig robotConfig = new LarkRobotConfig(id, name, resolvedWebhook, securityPolicyConfigs);
                robotConfig.setProtocolType(resolvedProtocolType);
                robotConfig.setEndpointMode(resolvedEndpointMode);
                robotConfig.setBaseUrl(baseUrl);
                robotConfig.setWebhookToken(webhookToken);
                robotConfig.setMessageLocaleStrategy(MessageLocaleStrategy.parse(messageLocaleStrategy));
                ProxySelector proxySelector = parseProxySelector(proxy);

                RobotType robotType = robotConfig.obtainRobotType();
                if (Objects.isNull(robotType)) {
                    response.put("ok", false);
                    response.put("message", Messages.form_validation_webhook_invalid());
                    return HttpResponses.json(response);
                }

                MessageSender sender = robotType.obtainInstance(RobotConfigModel.of(robotConfig, proxySelector));
                SendResult sendResult = Objects.requireNonNull(MessageDispatcher.getInstance()
                        .send(null, robotConfig.getId(), buildTestMessage(robotType, robotConfig.getMessageLocaleStrategy().toLocale()), sender), "sendResult");
                boolean ok = sendResult.isOk();
                String detail = sendResult.getMsg();
                response.put("ok", ok);
                response.put("message", ok
                        ? Messages.form_validation_test_success()
                        : StringUtils.isNotBlank(detail)
                        ? Messages.form_validation_test_failure_with_detail(detail)
                        : Messages.form_validation_test_failure());
                return HttpResponses.json(response);
            } catch (Exception e) {
                String detail = StringUtils.defaultIfBlank(e.getMessage(), null);
                response.put("ok", false);
                response.put("message", StringUtils.isNotBlank(detail)
                        ? Messages.form_validation_test_failure_with_detail(detail)
                        : Messages.form_validation_test_failure());
                return HttpResponses.json(response);
            }
        }

        private List<LarkSecurityPolicyConfig> parseSecurityPolicyConfigs(String securityConfigs) {
            return JsonUtils.readList(securityConfigs, LarkSecurityPolicyConfig.class)
                    .stream()
                    .filter(config -> StringUtils.isNotBlank(config.getValue()))
                    .toList();
        }

        private ProxySelector parseProxySelector(String proxy) {
            return Optional.ofNullable(JsonUtils.readValue(proxy, LarkProxyConfig.class))
                    .orElseGet(LarkProxyConfig::new)
                    .obtainProxySelector();
        }

        /**
         * Builds a message model for testing the robot configuration.
         *
         * @return The test message model.
         */
        private MessageModel buildTestMessage(RobotType robotType, Locale locale) {
            String rootUrl = Jenkins.get().getRootUrl();
            User user = Optional.ofNullable(User.current()).orElse(User.getUnknown());

            BuildJobModel buildJobModel = BuildJobModel.builder()
                    .projectName(NoticeI18n.robotTestProjectName(locale)).title(NoticeI18n.defaultTitle(locale))
                    .projectUrl(rootUrl).jobName(NoticeI18n.robotTestJobName(locale)).jobUrl(rootUrl + "/configure")
                    .statusType(BuildStatusEnum.SUCCESS).duration("-")
                    .executorName(user.getDisplayName()).build();

            return MessageModel.builder().type(MsgTypeEnum.CARD)
                    .title(NOTICE_ICON + " " + NoticeI18n.robotTestSuccessTitle(locale))
                    .text(buildJobModel.toMarkdown(robotType, locale))
                    .atAll(false).build();
        }
    }
}
