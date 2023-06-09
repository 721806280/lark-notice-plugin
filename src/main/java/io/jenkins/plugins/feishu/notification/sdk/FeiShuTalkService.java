package io.jenkins.plugins.feishu.notification.sdk;

import io.jenkins.plugins.feishu.notification.model.MessageModel;
import io.jenkins.plugins.feishu.notification.sdk.model.SendResult;

/**
 * 发送消息
 *
 * @author xm.z
 */
public interface FeiShuTalkService {

    /**
     * 发送消息
     *
     * @param robot 机器人ID
     * @param msg   消息体
     * @return 响应结果
     */
    SendResult send(String robot, MessageModel msg);

}
