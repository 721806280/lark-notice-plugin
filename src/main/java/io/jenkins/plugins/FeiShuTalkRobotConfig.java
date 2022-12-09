package io.jenkins.plugins;

import com.fasterxml.jackson.core.type.TypeReference;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.User;
import hudson.util.FormValidation;
import hudson.util.FormValidation.Kind;
import hudson.util.Secret;
import io.jenkins.plugins.FeiShuTalkSecurityPolicyConfig.FeiShuTalkSecurityPolicyConfigDescriptor;
import io.jenkins.plugins.enums.BuildStatusEnum;
import io.jenkins.plugins.enums.MsgTypeEnum;
import io.jenkins.plugins.enums.SecurityPolicyEnum;
import io.jenkins.plugins.model.BuildJobModel;
import io.jenkins.plugins.model.MessageModel;
import io.jenkins.plugins.sdk.FeiShuTalkSender;
import io.jenkins.plugins.tools.JsonUtils;
import jenkins.model.Jenkins;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.net.Proxy;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 机器人配置页面
 *
 * @author xm.z
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@SuppressWarnings("unused")
public class FeiShuTalkRobotConfig implements Describable<FeiShuTalkRobotConfig> {

    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * webhook 地址
     */
    private Secret webhook;

    /**
     * 安全策略配置
     */
    private List<FeiShuTalkSecurityPolicyConfig> securityPolicyConfigs;

    @DataBoundConstructor
    public FeiShuTalkRobotConfig(String id, String name, String webhook, List<FeiShuTalkSecurityPolicyConfig> securityPolicyConfigs) {
        this.id = StringUtils.isBlank(id) ? UUID.randomUUID().toString() : id;
        this.name = name;
        this.webhook = Secret.fromString(webhook);
        this.securityPolicyConfigs = securityPolicyConfigs;
    }

    public String getId() {
        if (StringUtils.isBlank(id)) {
            setId(UUID.randomUUID().toString());
        }
        return id;
    }

    public String getWebhook() {
        if (webhook == null) {
            return null;
        }
        return webhook.getPlainText();
    }

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

    @Override
    public Descriptor<FeiShuTalkRobotConfig> getDescriptor() {
        return Jenkins.get().getDescriptorByType(FeiShuTalkRobotConfigDescriptor.class);
    }

    @Extension
    public static class FeiShuTalkRobotConfigDescriptor extends Descriptor<FeiShuTalkRobotConfig> {

        /**
         * 安全配置页面
         *
         * @return 安全策略配置页面
         */
        public FeiShuTalkSecurityPolicyConfigDescriptor getFeiShuTalkSecurityPolicyConfigDescriptor() {
            return Jenkins.get().getDescriptorByType(FeiShuTalkSecurityPolicyConfigDescriptor.class);
        }

        /**
         * 获取默认的安全配置选项
         *
         * @return 默认的安全配置选项
         */
        public ArrayList<FeiShuTalkSecurityPolicyConfig> getDefaultSecurityPolicyConfigs() {
            return Arrays.stream(SecurityPolicyEnum.values())
                    .map(FeiShuTalkSecurityPolicyConfig::of)
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        /**
         * name 字段必填
         *
         * @param value name
         * @return 是否校验成功
         */
        public FormValidation doCheckName(@QueryParameter String value) {
            return StringUtils.isNotBlank(value) ? FormValidation.ok() :
                    FormValidation.error(Messages.RobotConfigFormValidation_name());
        }

        /**
         * webhook 字段必填
         *
         * @param value webhook
         * @return 是否校验成功
         */
        public FormValidation doCheckWebhook(@QueryParameter String value) {
            return StringUtils.isNotBlank(value) ? FormValidation.ok() :
                    FormValidation.error(Messages.RobotConfigFormValidation_webhook());
        }

        /**
         * 测试配置信息
         *
         * @param id                   id
         * @param name                 名称
         * @param webhook              webhook
         * @param securityPolicyConfig 安全策略
         * @param proxy                代理
         * @return 机器人配置是否正确
         */
        public FormValidation doTest(@QueryParameter("id") String id, @QueryParameter("name") String name,
                                     @QueryParameter("webhook") String webhook, @QueryParameter("proxy") String proxy,
                                     @QueryParameter("securityPolicyConfigs") String securityPolicyConfig) {
            List<FeiShuTalkSecurityPolicyConfig> securityPolicyConfigs = JsonUtils.toBean(securityPolicyConfig, new TypeReference<>() {
            });

            FeiShuTalkRobotConfig robotConfig = new FeiShuTalkRobotConfig(id, name, webhook, securityPolicyConfigs);

            Proxy proxyVar = JsonUtils.toBean(proxy, FeiShuTalkProxyConfig.class).getProxy();

            FeiShuTalkSender sender = new FeiShuTalkSender(robotConfig, proxyVar);

            String message = sender.sendInteractive(buildTestMessage());

            return StringUtils.isNotBlank(message) ? FormValidation.error(message) :
                    FormValidation.respond(Kind.OK, "<span style='color:#52c41a;font-weight:bold;'>测试成功</span>");
        }

        private MessageModel buildTestMessage() {
            String rootUrl = Jenkins.get().getRootUrl();
            User user = Optional.ofNullable(User.current()).orElse(User.getUnknown());

            BuildJobModel buildJobModel = BuildJobModel.builder().projectName("欢迎使用飞书机器人插件~")
                    .projectUrl(rootUrl).jobName("系统配置").jobUrl(rootUrl + "/configure")
                    .statusType(BuildStatusEnum.SUCCESS).duration("-")
                    .executorName(user.getDisplayName()).executorMobile(user.getDescription())
                    .build();

            return MessageModel.builder().type(MsgTypeEnum.INTERACTIVE)
                    .title("\uD83D\uDCE2 飞书机器人测试成功")
                    .text(buildJobModel.toMarkdown())
                    .atAll(false).build();
        }
    }
}