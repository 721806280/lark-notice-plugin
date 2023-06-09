package io.jenkins.plugins.feishu.notification.config;

import com.fasterxml.jackson.core.type.TypeReference;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.User;
import hudson.util.FormValidation;
import hudson.util.FormValidation.Kind;
import hudson.util.Secret;
import io.jenkins.plugins.feishu.notification.Messages;
import io.jenkins.plugins.feishu.notification.enums.BuildStatusEnum;
import io.jenkins.plugins.feishu.notification.enums.MsgTypeEnum;
import io.jenkins.plugins.feishu.notification.enums.SecurityPolicyEnum;
import io.jenkins.plugins.feishu.notification.model.BuildJobModel;
import io.jenkins.plugins.feishu.notification.model.MessageModel;
import io.jenkins.plugins.feishu.notification.model.RobotConfigModel;
import io.jenkins.plugins.feishu.notification.sdk.FeiShuTalkSender;
import io.jenkins.plugins.feishu.notification.sdk.impl.DefaultFeiShuTalkSender;
import io.jenkins.plugins.feishu.notification.sdk.model.SendResult;
import io.jenkins.plugins.feishu.notification.tools.JsonUtils;
import jenkins.model.Jenkins;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.net.ProxySelector;
import java.util.*;
import java.util.stream.Collectors;

import static io.jenkins.plugins.feishu.notification.sdk.constant.Constants.NOTICE_ICON;

/**
 * 飞书机器人配置类，包括机器人ID，名称，Webhook密钥和安全策略配置列表。
 *
 * @author xm.z
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@SuppressWarnings("unused")
public class FeiShuTalkRobotConfig implements Describable<FeiShuTalkRobotConfig> {

    /**
     * 机器人ID，可以为空，为空时自动生成UUID。
     */
    private String id;

    /**
     * 机器人名称，不可为空。
     */
    private String name;

    /**
     * Webhook密钥，采用Jenkins提供的Secret实现，不可为空。
     */
    private Secret webhook;

    /**
     * 安全策略配置列表，包括一组Key-Value对，不可为空。
     */
    private List<FeiShuTalkSecurityPolicyConfig> securityPolicyConfigs;

    /**
     * 构造方法，用于初始化机器人配置对象。
     *
     * @param id                    机器人ID，可以为空。
     * @param name                  机器人名称，不可为空。
     * @param webhook               Webhook密钥，不可为空。
     * @param securityPolicyConfigs 安全策略配置列表，不可为空。
     */
    @DataBoundConstructor
    public FeiShuTalkRobotConfig(String id, String name, String webhook, List<FeiShuTalkSecurityPolicyConfig> securityPolicyConfigs) {
        this.id = StringUtils.isBlank(id) ? UUID.randomUUID().toString() : id;
        this.name = name;
        this.webhook = Secret.fromString(webhook);
        this.securityPolicyConfigs = securityPolicyConfigs;
    }

    /**
     * 获取机器人ID，为空时生成一个UUID，并返回。
     *
     * @return 机器人ID。
     */
    public String getId() {
        if (StringUtils.isBlank(id)) {
            setId(UUID.randomUUID().toString());
        }
        return id;
    }

    /**
     * 获取Webhook密钥的明文值。
     *
     * @return Webhook密钥的明文值。
     */
    public String getWebhook() {
        if (webhook == null) {
            return null;
        }
        return webhook.getPlainText();
    }

    /**
     * 获取安全策略配置列表，其中包括一组Key-Value对。
     *
     * @return 安全策略配置列表。
     */
    public List<FeiShuTalkSecurityPolicyConfig> getSecurityPolicyConfigs() {
        return Arrays.stream(SecurityPolicyEnum.values()).map(enumItem -> {
            FeiShuTalkSecurityPolicyConfig policyConfig = FeiShuTalkSecurityPolicyConfig.of(enumItem);
            if (securityPolicyConfigs != null) {
                Optional<FeiShuTalkSecurityPolicyConfig> config = securityPolicyConfigs.stream()
                        .filter(configItem -> enumItem.name().equals(configItem.getType())).findAny();
                config.ifPresent(t -> policyConfig.setValue(t.getValue()));
            }
            return policyConfig;
        }).collect(Collectors.toList());
    }

    /**
     * 获取该类的描述符，用于在Jenkins中显示机器人配置页面。
     *
     * @return 机器人配置页面描述符。
     */
    @Override
    public Descriptor<FeiShuTalkRobotConfig> getDescriptor() {
        return Jenkins.get().getDescriptorByType(FeiShuTalkRobotConfigDescriptor.class);
    }

    /**
     * 机器人配置页面描述符，用于Jenkins中显示机器人配置页面。
     */
    @Extension
    public static class FeiShuTalkRobotConfigDescriptor extends Descriptor<FeiShuTalkRobotConfig> {

        /**
         * 获取安全策略配置页面描述符。
         *
         * @return 安全策略配置页面描述符。
         */
        public FeiShuTalkSecurityPolicyConfig.FeiShuTalkSecurityPolicyConfigDescriptor getFeiShuTalkSecurityPolicyConfigDescriptor() {
            return Jenkins.get().getDescriptorByType(FeiShuTalkSecurityPolicyConfig.FeiShuTalkSecurityPolicyConfigDescriptor.class);
        }

        /**
         * 获取默认的安全策略配置列表。
         *
         * @return 默认安全策略配置列表。
         */
        public ArrayList<FeiShuTalkSecurityPolicyConfig> getDefaultSecurityPolicyConfigs() {
            return Arrays.stream(SecurityPolicyEnum.values())
                    .map(FeiShuTalkSecurityPolicyConfig::of)
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        /**
         * 校验机器人名称是否为空。
         *
         * @param value 机器人名称。
         * @return 返回验证结果，如果验证通过，返回FormValidation.ok()，否则返回错误信息。
         */
        public FormValidation doCheckName(@QueryParameter String value) {
            return StringUtils.isNotBlank(value) ? FormValidation.ok() :
                    FormValidation.error(Messages.form_validation_name());
        }

        /**
         * 校验Webhook密钥是否为空。
         *
         * @param value Webhook密钥。
         * @return 返回验证结果，如果验证通过，返回FormValidation.ok()，否则返回错误信息。
         */
        public FormValidation doCheckWebhook(@QueryParameter String value) {
            return StringUtils.isNotBlank(value) ? FormValidation.ok() :
                    FormValidation.error(Messages.form_validation_webhook());
        }

        /**
         * 测试机器人配置是否可以正常工作。
         *
         * @param id                   机器人ID。
         * @param name                 机器人名称。
         * @param webhook              Webhook密钥。
         * @param proxy                代理设置。
         * @param securityPolicyConfig 安全策略配置列表。
         * @return 返回测试结果，如果测试通过，返回FormValidation.respond(Kind.OK)，否则返回错误信息。
         */
        public FormValidation doTest(@QueryParameter("id") String id, @QueryParameter("name") String name,
                                     @QueryParameter("webhook") String webhook, @QueryParameter("proxy") String proxy,
                                     @QueryParameter("securityPolicyConfigs") String securityPolicyConfig) {
            List<FeiShuTalkSecurityPolicyConfig> securityPolicyConfigs = JsonUtils.toBean(securityPolicyConfig, new TypeReference<>() {
            });

            FeiShuTalkRobotConfig robotConfig = new FeiShuTalkRobotConfig(id, name, webhook, securityPolicyConfigs);

            ProxySelector proxySelector = JsonUtils.toBean(proxy, FeiShuTalkProxyConfig.class).obtainProxySelector();

            FeiShuTalkSender sender = new DefaultFeiShuTalkSender(RobotConfigModel.of(robotConfig, proxySelector));

            SendResult sendResult = sender.sendInteractive(buildTestMessage());

            return !sendResult.isOk() ? FormValidation.error(sendResult.getMsg()) :
                    FormValidation.respond(Kind.OK, "<span style='color:#52c41a;font-weight:bold;'>测试成功</span>");
        }

        /**
         * 构建用于测试机器人配置的消息模型。
         *
         * @return 测试消息模型。
         */
        private MessageModel buildTestMessage() {
            String rootUrl = Jenkins.get().getRootUrl();
            User user = Optional.ofNullable(User.current()).orElse(User.getUnknown());

            BuildJobModel buildJobModel = BuildJobModel.builder().projectName("欢迎使用飞书机器人插件~")
                    .projectUrl(rootUrl).jobName("系统配置").jobUrl(rootUrl + "/configure")
                    .statusType(BuildStatusEnum.SUCCESS).duration("-")
                    .executorName(user.getDisplayName()).executorMobile(user.getDescription())
                    .build();

            return MessageModel.builder().type(MsgTypeEnum.INTERACTIVE)
                    .title(NOTICE_ICON + " 飞书机器人测试成功")
                    .text(buildJobModel.toMarkdown())
                    .atAll(false).build();
        }
    }
}