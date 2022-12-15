package io.jenkins.plugins.service;

import io.jenkins.plugins.model.MessageModel;

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
    String send(String robot, MessageModel msg);

}
