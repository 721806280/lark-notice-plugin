package io.jenkins.plugins.model;

import io.jenkins.plugins.enums.MsgTypeEnum;
import io.jenkins.plugins.sdk.entity.support.At;
import io.jenkins.plugins.sdk.entity.support.Button;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 消息
 *
 * <p>不要使用 @Data 注解，spotbugs 会报错
 *
 * <p>Redundant nullcheck of this$title, which is known to be non-null in
 * io.jenkins.plugins.model.MessageModel.equals(Object)
 *
 * @author xm.z
 */
@Getter
@Setter
@ToString
@Builder
public class MessageModel {

    public static final String DEFAULT_TITLE = "\uD83D\uDCE2 Jenkins 构建通知";

    /**
     * 消息类型
     */
    private MsgTypeEnum type;

    /**
     * 需要 at 的 open_id
     */
    private Set<String> atOpenIds;

    /**
     * 是否 at 全部
     */
    private boolean atAll;

    /**
     * 标题，首屏会话透出的展示内容
     */
    private String title;

    /**
     * 消息正文
     */
    private String text;

    /**
     * 按钮信息
     */
    private List<Button> buttons;

    /**
     * title 不能为空
     *
     * @return 带默认值的标题
     */
    public String getTitle() {
        return StringUtils.defaultIfBlank(title, DEFAULT_TITLE);
    }

    /**
     * 获取 at 设置
     *
     * @return at
     */
    public At getAt() {
        At at = new At();
        if (atOpenIds != null) {
            at.setAtOpenIds(atOpenIds.stream().map(String::trim)
                    .filter(item -> !StringUtils.isEmpty(item))
                    .collect(Collectors.toList()));
        }
        at.setIsAtAll(atAll);
        return at;
    }

}
