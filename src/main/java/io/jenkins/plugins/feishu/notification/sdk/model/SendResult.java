package io.jenkins.plugins.feishu.notification.sdk.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 飞书机器人发送消息接口响应数据
 *
 * @author xm.z
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendResult {

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String msg;

    public static SendResult fail(String msg) {
        return new SendResult(-1, msg);
    }

    public boolean isOk() {
        return Objects.nonNull(this.getCode()) && this.getCode() == 0;
    }
}
