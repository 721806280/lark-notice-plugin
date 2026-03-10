package io.jenkins.plugins.lark.notice.enums;

import io.jenkins.plugins.lark.notice.model.RobotConfigModel;
import io.jenkins.plugins.lark.notice.sdk.MessageSender;
import io.jenkins.plugins.lark.notice.sdk.impl.DingMessageSender;
import io.jenkins.plugins.lark.notice.sdk.impl.LarkMessageSender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;

import java.net.URL;

/**
 * Supported robot platform types and their sender factories.
 *
 * @author xm.z
 */
@Getter
@AllArgsConstructor
public enum RobotType {

    // Lark
    LARK("Lark", "open.larksuite.com", "text_tag") {
        /**
         * {@inheritDoc}
         */
        @Override
        public MessageSender obtainInstance(RobotConfigModel robotConfig) {
            return new LarkMessageSender(robotConfig);
        }
    },

    FS("飞书", "open.feishu.cn", "text_tag") {
        /**
         * {@inheritDoc}
         */
        @Override
        public MessageSender obtainInstance(RobotConfigModel robotConfig) {
            return new LarkMessageSender(robotConfig);
        }
    },

    DING_TAlK("钉钉", "api.dingtalk.com", "font") {
        /**
         * {@inheritDoc}
         */
        @Override
        public MessageSender obtainInstance(RobotConfigModel robotConfig) {
            return new DingMessageSender(robotConfig);
        }
    };

    private final String name;

    private final String host;

    private final String statusTagName;

    /**
     * Resolves the robot type from a webhook URL host.
     *
     * @param url webhook URL
     * @return matching robot type, or {@code null} when the host is not recognized
     */
    @SneakyThrows
    public static RobotType fromUrl(String url) {
        String host = new URL(url).getHost();
        for (RobotType type : RobotType.values()) {
            if (host.contains(type.getHost())) {
                return type;
            }
        }
        return null;
    }

    /**
     * Creates a platform-specific message sender for the supplied robot configuration.
     *
     * @param robotConfig robot configuration
     * @return message sender bound to the target platform
     */
    public abstract MessageSender obtainInstance(RobotConfigModel robotConfig);

}
