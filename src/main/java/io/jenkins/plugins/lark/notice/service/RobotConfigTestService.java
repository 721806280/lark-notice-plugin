package io.jenkins.plugins.lark.notice.service;

import hudson.model.User;
import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.config.LarkProxyConfig;
import io.jenkins.plugins.lark.notice.config.LarkRobotConfig;
import io.jenkins.plugins.lark.notice.config.LarkSecurityPolicyConfig;
import io.jenkins.plugins.lark.notice.config.MessageLocaleResolver;
import io.jenkins.plugins.lark.notice.config.RobotWebhookResolver;
import io.jenkins.plugins.lark.notice.enums.BuildStatusEnum;
import io.jenkins.plugins.lark.notice.enums.MessageLocaleStrategy;
import io.jenkins.plugins.lark.notice.enums.MsgTypeEnum;
import io.jenkins.plugins.lark.notice.enums.RobotProtocolType;
import io.jenkins.plugins.lark.notice.enums.RobotType;
import io.jenkins.plugins.lark.notice.enums.WebhookEndpointMode;
import io.jenkins.plugins.lark.notice.i18n.NoticeI18n;
import io.jenkins.plugins.lark.notice.model.BuildJobModel;
import io.jenkins.plugins.lark.notice.model.MessageModel;
import io.jenkins.plugins.lark.notice.model.RobotConfigModel;
import io.jenkins.plugins.lark.notice.sdk.MessageDispatcher;
import io.jenkins.plugins.lark.notice.sdk.MessageSender;
import io.jenkins.plugins.lark.notice.sdk.model.SendResult;
import io.jenkins.plugins.lark.notice.tools.ApiResponse;
import io.jenkins.plugins.lark.notice.tools.JsonUtils;
import jenkins.model.Jenkins;
import org.apache.commons.lang3.StringUtils;

import java.net.ProxySelector;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.NOTICE_ICON;

/**
 * Service for validating robot configurations and sending test messages.
 */
public final class RobotConfigTestService {

    private RobotConfigTestService() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static ApiResponse testRobotConfig(String id, String name,
                                              String protocolType, String endpointMode,
                                              String webhook, String baseUrl, String webhookToken,
                                              String proxy, String securityConfigs,
                                              String messageLocaleStrategy) {
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
                return ApiResponse.fail(Messages.form_validation_webhook_invalid());
            }

            MessageSender sender = robotType.obtainInstance(RobotConfigModel.of(robotConfig, proxySelector));
            SendResult sendResult = Objects.requireNonNull(MessageDispatcher.getInstance()
                    .send(null, robotConfig.getId(), buildTestMessage(robotType, MessageLocaleResolver.resolve(robotConfig)), sender), "sendResult");
            boolean ok = sendResult.isOk();
            String detail = sendResult.getMsg();
            String message = ok
                    ? Messages.form_validation_test_success()
                    : StringUtils.isNotBlank(detail)
                    ? Messages.form_validation_test_failure_with_detail(detail)
                    : Messages.form_validation_test_failure();
            return ok ? ApiResponse.ok(message) : ApiResponse.fail(message);
        } catch (Exception e) {
            String detail = StringUtils.defaultIfBlank(e.getMessage(), null);
            String message = StringUtils.isNotBlank(detail)
                    ? Messages.form_validation_test_failure_with_detail(detail)
                    : Messages.form_validation_test_failure();
            return ApiResponse.fail(message);
        }
    }

    private static List<LarkSecurityPolicyConfig> parseSecurityPolicyConfigs(String securityConfigs) {
        return JsonUtils.readList(securityConfigs, LarkSecurityPolicyConfig.class)
                .stream()
                .filter(config -> StringUtils.isNotBlank(config.getValue()))
                .toList();
    }

    private static ProxySelector parseProxySelector(String proxy) {
        return Optional.ofNullable(JsonUtils.readValue(proxy, LarkProxyConfig.class))
                .orElseGet(LarkProxyConfig::new)
                .obtainProxySelector();
    }

    private static MessageModel buildTestMessage(RobotType robotType, Locale locale) {
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
