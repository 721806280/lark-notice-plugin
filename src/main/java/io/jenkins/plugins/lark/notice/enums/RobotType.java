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
 * RobotType
 *
 * @author xm.z
 */
@Getter
@AllArgsConstructor
public enum RobotType {

    // Lark
    LARK("Lark", "open.larksuite.com", "text_tag") {
        @Override
        public MessageSender obtainInstance(RobotConfigModel robotConfig) {
            return new LarkMessageSender(robotConfig);
        }
    },

    FS("飞书", "open.feishu.cn", "text_tag") {
        @Override
        public MessageSender obtainInstance(RobotConfigModel robotConfig) {
            return new LarkMessageSender(robotConfig);
        }
    },

    DING_TAlK("钉钉", "api.dingtalk.com", "font") {
        @Override
        public MessageSender obtainInstance(RobotConfigModel robotConfig) {
            return new DingMessageSender(robotConfig);
        }
    };

    private final String name;

    private final String host;

    private final String statusTagName;

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

    public abstract MessageSender obtainInstance(RobotConfigModel robotConfig);

}