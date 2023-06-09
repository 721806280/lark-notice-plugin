package io.jenkins.plugins.feishu.notification.sdk.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 群名片消息 类型
 *
 * @author xm.z
 */
@Data
public class ShareChat {

    /**
     * 群名片ID
     */
    @JsonProperty(value = "share_chat_id")
    private String shareChatId;

}