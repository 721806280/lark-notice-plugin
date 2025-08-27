package io.jenkins.plugins.lark.notice.sdk.model.ding;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.at.At;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;

import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.LF;

/**
 * @author xm.z
 */
@Slf4j
@Getter
@Setter
public abstract class BaseDingMessage implements Serializable {

    @JsonProperty("msgtype")
    private String msgType;

    /**
     * 添加 at 信息
     *
     * @param content  原始内容
     * @param at       at 配置
     * @param markdown 是否是 markdown 格式的内容
     * @return 包含 at 信息的内容
     */
    protected static String addAtInfo(String content, At at, boolean markdown) {
        List<String> allAts = at.getAllAts();
        if (CollectionUtils.isEmpty(allAts)) {
            return content;
        }
        String atContent = "@" + StringUtils.join(allAts, " @");
        if (markdown) {
            return content + LF + LF + "<font color='#1890FF'>" + atContent + "</font>" + LF;
        }
        return content + atContent;
    }

}
