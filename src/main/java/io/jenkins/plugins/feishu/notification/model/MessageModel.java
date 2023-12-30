package io.jenkins.plugins.feishu.notification.model;

import io.jenkins.plugins.feishu.notification.enums.BuildStatusEnum;
import io.jenkins.plugins.feishu.notification.enums.MsgTypeEnum;
import io.jenkins.plugins.feishu.notification.sdk.model.entity.support.At;
import io.jenkins.plugins.feishu.notification.sdk.model.entity.support.Button;
import io.jenkins.plugins.feishu.notification.sdk.model.entity.support.ImgElement;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static io.jenkins.plugins.feishu.notification.sdk.constant.Constants.NOTICE_ICON;

/**
 * 用于存储消息相关的模型
 *
 * @author xm.z
 */
@Getter
@Setter
@ToString
@Builder
public class MessageModel {

    /**
     * 默认标题
     */
    public static final String DEFAULT_TITLE = NOTICE_ICON + " Jenkins 构建通知";

    /**
     * 消息类型
     */
    private MsgTypeEnum type;

    /**
     * 构建状态
     */
    private BuildStatusEnum statusType;

    /**
     * 需要 at 的 open_id 集合
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
     * 消息正文顶部图片
     */
    private ImgElement topImg;

    /**
     * 消息正文底部图片
     */
    private ImgElement bottomImg;

    /**
     * 按钮信息
     */
    private List<Button> buttons;

    /**
     * 获取带默认值的标题
     *
     * @return 带默认值的标题
     */
    public String getTitle() {
        return StringUtils.defaultIfBlank(title, DEFAULT_TITLE);
    }

    /**
     * 获取标题模版色
     *
     * @return 模版色
     */
    public String obtainTitleTemplate() {
        return (Objects.nonNull(statusType) ? statusType : BuildStatusEnum.START).getTemplate();
    }

    /**
     * 获取 at 设置
     *
     * @return at
     */
    public At getAt() {
        At at = new At();
        at.setIsAtAll(atAll);

        if (atOpenIds != null) {
            at.setAtOpenIds(atOpenIds.stream().map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList()));
        }

        return at;
    }

}
