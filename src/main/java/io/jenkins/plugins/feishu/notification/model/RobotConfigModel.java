package io.jenkins.plugins.feishu.notification.model;

import io.jenkins.plugins.feishu.notification.config.FeiShuTalkRobotConfig;
import io.jenkins.plugins.feishu.notification.enums.SecurityPolicyEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.ProxySelector;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * 机器人配置模型。
 *
 * @author xm.z
 */
@Data
@Slf4j
public class RobotConfigModel {

    /**
     * 代理选择器，用于处理网络请求。
     */
    private ProxySelector proxySelector;

    /**
     * 提供机器人发送消息的 webhook。
     */
    private String webhook;

    /**
     * 提供机器人安全校验的 key。
     */
    private String keys;

    /**
     * 提供机器人安全校验的 secret。
     */
    private String sign;

    /**
     * 根据飞书机器人配置对象创建 RobotConfigModel 对象。
     *
     * @param robotConfig 飞书机器人配置对象
     * @return RobotConfigModel 对象
     */
    public static RobotConfigModel of(FeiShuTalkRobotConfig robotConfig, ProxySelector proxySelector) {
        Objects.requireNonNull(robotConfig, "robotConfig must not be null");
        Objects.requireNonNull(proxySelector, "proxySelector must not be null");

        RobotConfigModel meta = new RobotConfigModel();
        meta.setProxySelector(proxySelector);
        meta.setWebhook(robotConfig.getWebhook());

        // 解析安全策略
        robotConfig.getSecurityPolicyConfigs().stream()
                .filter(config -> StringUtils.isNotBlank(config.getValue()))
                .forEach(config -> {
                    String type = config.getType();
                    SecurityPolicyEnum securityPolicyEnum = SecurityPolicyEnum.valueOf(type);
                    switch (securityPolicyEnum) {
                        case KEY:
                            meta.setKeys(config.getValue());
                            break;
                        case SECRET:
                            meta.setSign(config.getValue());
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid security policy type: " + type);
                    }
                });
        return meta;
    }

    /**
     * 使用给定的时间戳和 secret 创建签名。
     *
     * @param timestamp 时间戳
     * @param secret    secret
     * @return 签名字符串
     */
    private static String createSign(long timestamp, String secret) {
        String result = "";
        try {
            String seed = timestamp + "\n" + secret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(seed.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(new byte[]{});
            result = Base64.encodeBase64String(signData);
        } catch (Exception e) {
            log.error("飞书插件设置签名失败", e);
        }
        return result;
    }

    /**
     * 使用给定内容构建加密后的签名。
     *
     * @param content 待加密的内容
     * @return 包含签名信息的 Map 对象
     */
    public Map<String, Object> buildSign(Map<String, Object> content) {
        if (StringUtils.isNotBlank(sign)) {
            long timestamp = System.currentTimeMillis() / 1000L;
            content.put("timestamp", String.valueOf(timestamp));
            content.put("sign", createSign(timestamp, sign));
        }
        return content;
    }

}