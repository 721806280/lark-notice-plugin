package io.jenkins.plugins.sdk;

import io.jenkins.plugins.model.MessageModel;
import io.jenkins.plugins.sdk.model.SendResult;

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
