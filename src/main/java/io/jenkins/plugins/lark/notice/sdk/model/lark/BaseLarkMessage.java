package io.jenkins.plugins.lark.notice.sdk.model.lark;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.at.At;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.LF;

/**
 * @author xm.z
 */
@Slf4j
@Getter
@Setter
public abstract class BaseLarkMessage implements Serializable {

    @JsonProperty("msg_type")
    private String msgType;

    /**
     * Adds @mention information to the message content.
     * This can be used to notify specific users or everyone in a group.
     *
     * @param content The original message content.
     * @param at      An At object containing the configuration for the @mention.
     * @return The original content appended with the @mention information.
     */
    protected static String addAtInfo(String content, At at) {
        String atTemplate = "<at id=%s></at>";
        if (at.getIsAtAll()) {
            return content + String.format(atTemplate, "all");
        }

        List<String> atUserIds = at.getAtUserIds();
        if (atUserIds == null || atUserIds.isEmpty()) {
            return content;
        }

        List<String> atContents = atUserIds.stream().map(v -> String.format(atTemplate, v)).collect(Collectors.toList());
        String atContent = StringUtils.join(atContents, "");
        return (StringUtils.endsWith(content, LF) ? content : content + LF) + atContent;
    }

}
