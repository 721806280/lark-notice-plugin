package io.jenkins.plugins.lark.notice.model;

import io.jenkins.plugins.lark.notice.enums.BuildStatusEnum;
import io.jenkins.plugins.lark.notice.enums.MsgTypeEnum;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.Button;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.at.At;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.img.ImgElement;
import io.jenkins.plugins.lark.notice.tools.Utils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Model for storing message-related information. This class encapsulates properties related to a message,
 * such as the message type, build status, recipient identifiers, title, text content, images, and buttons.
 * It is designed to facilitate the creation and customization of messages sent from Jenkins build notifications
 * to various platforms.
 *
 * @author xm.z
 */
@Getter
@Setter
@ToString
@Builder
public class MessageModel {

    /**
     * The type of the message, determining the format or channel through which the message is sent.
     */
    private MsgTypeEnum type;

    /**
     * The build status, indicating the outcome of the build process (e.g., success, failure).
     */
    private BuildStatusEnum statusType;

    /**
     * A set of UserIds to be mentioned in the message. These are typically user identifiers on platforms like WeChat.
     */
    private Set<String> atUserIds;

    /**
     * Flag indicating whether the message should mention all users within the message scope.
     */
    private boolean atAll;

    /**
     * The title of the message, displayed prominently in the message. This can be customized or left blank to use the default title.
     */
    private String title;

    /**
     * The main text content of the message.
     */
    private String text;

    /**
     * An image element to be displayed at the top of the message body.
     */
    private ImgElement topImg;

    /**
     * An image element to be displayed at the bottom of the message body.
     */
    private ImgElement bottomImg;

    /**
     * A list of buttons that can be included in the message, providing interactive elements for the recipient.
     */
    private List<Button> buttons;


    //==================================================================================================================

    /**
     * 点击单条信息到跳转链接
     */
    private String messageUrl;

    /**
     * 单条信息后面图片的URL
     */
    private String picUrl;

    /**
     * 单个按钮的标题, 设置此项和singleURL后，buttons 无效
     */
    private String singleTitle;

    /**
     * 点击消息跳转的URL
     */
    private String singleUrl;

    /**
     * 0：按钮竖直排列 1：按钮横向排列
     */
    private String btnOrientation;

    /**
     * Retrieves the theme color for the card title, based on the build status. This allows for visual customization
     * of messages according to their significance (e.g., success, failure).
     *
     * @return The theme color for the card title.
     */
    public String obtainHeaderTemplate() {
        return (Objects.nonNull(statusType) ? statusType : BuildStatusEnum.START).getTemplate();
    }

    /**
     * Constructs and returns an {@link At} object, which encapsulates settings for mentioning users in the message.
     * This includes whether to mention all users and a list of specific UserIds to mention.
     *
     * @return An {@link At} object containing mention settings for the message.
     */
    public At getAt() {
        At at = new At();
        at.setIsAtAll(atAll);

        if (atUserIds != null) {
            Map<Boolean, List<String>> partitioned = atUserIds.stream()
                    .map(String::trim).filter(StringUtils::isNotBlank)
                    .collect(Collectors.partitioningBy(Utils::isMobile));
            at.setAtUserIds(partitioned.get(false));
            at.setAtMobiles(partitioned.get(true));
        }

        return at;
    }

}