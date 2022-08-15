package io.jenkins.plugins.service;

import io.jenkins.plugins.model.MessageModel;

/**
 * 发送消息
 *
 * @author xm.z
 */
public interface FeiShuTalkService {

    String send(String robot, MessageModel msg);

}
