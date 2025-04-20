package io.jenkins.plugins.lark.notice.model;

import io.jenkins.cli.shaded.org.apache.commons.lang.StringUtils;
import io.jenkins.plugins.lark.notice.config.LarkRobotConfig;
import io.jenkins.plugins.lark.notice.enums.RobotType;
import io.jenkins.plugins.lark.notice.enums.SecurityPolicyEnum;
import io.jenkins.plugins.lark.notice.sdk.constant.Constants;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.ProxySelector;
import java.net.URLEncoder;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Model for robot configuration.
 * This class represents the configuration for a robot, including proxy settings, webhook URL,
 * security key, and secret for authentication.
 * <p>
 *
 * @author xm.z
 */
@Data
@Slf4j
public class RobotConfigModel {

    private RobotType robotType;

    /**
     * Proxy selector used to handle network requests.
     */
    private ProxySelector proxySelector;

    /**
     * Webhook URL for sending messages via the robot.
     */
    private String webhook;

    /**
     * Security key used for authentication.
     */
    @SuppressWarnings("lgtm[jenkins/plaintext-storage]")
    private String keys;

    /**
     * Secret used for authentication.
     */
    private String sign;

    /**
     * Secret used for authentication.
     */
    private Boolean noSsl;

    /**
     * Creates a RobotConfigModel object based on the LarkRobotConfig object and proxy selector.
     *
     * @param robotConfig   LarkRobotConfig object representing the configuration of a Lark robot
     * @param proxySelector ProxySelector object for handling network requests
     * @return RobotConfigModel object
     */
    public static RobotConfigModel of(LarkRobotConfig robotConfig, ProxySelector proxySelector) {
        Objects.requireNonNull(robotConfig, "robotConfig must not be null");
        Objects.requireNonNull(proxySelector, "proxySelector must not be null");

        RobotConfigModel meta = new RobotConfigModel();
        meta.setRobotType(robotConfig.obtainRobotType());
        meta.setProxySelector(proxySelector);
        meta.setWebhook(robotConfig.getWebhook());

        // Parse security policies
        robotConfig.getSecurityPolicyConfigs().stream()
                .filter(config -> StringUtils.isNotBlank(config.getValue()))
                .forEach(config -> {
                    String type = config.getType();
                    SecurityPolicyEnum securityPolicyEnum = SecurityPolicyEnum.valueOf(type);
                    switch (securityPolicyEnum) {
                        case KEY -> meta.setKeys(config.getValue());
                        case SECRET -> meta.setSign(config.getValue());
                        case NO_SSL -> meta.setNoSsl(Boolean.parseBoolean(config.getValue()));
                        default -> throw new IllegalArgumentException("Invalid security policy type: " + type);
                    }
                });
        return meta;
    }

    /**
     * Creates a signature using the given timestamp and secret.
     *
     * @param timestamp timestamp
     * @return signature string
     */
    public String createSign(long timestamp) {
        try {
            boolean hasDing = RobotType.DING_TAlK.equals(robotType);
            String seed = timestamp + Constants.LF + sign;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec((hasDing ? sign : seed).getBytes(UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal((hasDing ? seed.getBytes(UTF_8) : new byte[]{}));
            String sign = Base64.encodeBase64String(signData);
            return hasDing ? URLEncoder.encode(sign, UTF_8) : sign;
        } catch (Exception e) {
            log.error("Failed to create signature for Lark plugin", e);
        }
        return "";
    }

}