package io.jenkins.plugins.lark.notice.sdk.model.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.plugins.lark.notice.sdk.constant.Constants;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.at.At;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Base message model for WeCom group robots.
 */
@Getter
@Setter
public abstract class BaseWechatWorkMessage implements Serializable {

    @JsonProperty("msgtype")
    private String msgType;

    protected static String appendMarkdownAtInfo(String content, At at) {
        if (at == null) {
            return content;
        }
        if (Boolean.TRUE.equals(at.getIsAtAll())) {
            return StringUtils.defaultString(content) + Constants.LF + Constants.LF + "@all";
        }
        List<String> mentions = buildMarkdownMentions(at);
        if (CollectionUtils.isEmpty(mentions)) {
            return content;
        }
        return StringUtils.defaultString(content) + Constants.LF + Constants.LF + StringUtils.join(mentions, " ");
    }

    private static List<String> buildMarkdownMentions(At at) {
        if (CollectionUtils.isEmpty(at.getAtUserIds())) {
            return List.of();
        }
        return at.getAtUserIds().stream()
                .filter(StringUtils::isNotBlank)
                .map(userId -> "<@" + userId + ">")
                .collect(Collectors.toList());
    }
}
