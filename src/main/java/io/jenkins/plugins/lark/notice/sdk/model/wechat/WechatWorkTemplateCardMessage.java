package io.jenkins.plugins.lark.notice.sdk.model.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.plugins.lark.notice.enums.BuildStatusEnum;
import io.jenkins.plugins.lark.notice.i18n.NoticeI18n;
import io.jenkins.plugins.lark.notice.model.MessageModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * WeCom news-notice template card message.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WechatWorkTemplateCardMessage extends BaseWechatWorkMessage {

    private static final String DEFAULT_SOURCE_ICON_URL = "https://get.jenkins.io/art/jenkins-logo/favicon.ico";

    private static final String DEFAULT_CARD_IMAGE_URL = "https://www.jenkins.io/images/post-images/2025/07/24/redesigning-jenkins-part-two.png";

    private static final String SOURCE_DESCRIPTION = "Lark Notice · Jenkins";

    private static final String CARD_TYPE_NEWS_NOTICE = "news_notice";

    private static final int LINK_TYPE = 1;

    private static final int TEXT_TYPE = 0;

    private static final int MAX_JUMP_ITEMS = 3;

    private static final double CARD_IMAGE_ASPECT_RATIO = 2.25d;

    @JsonProperty("template_card")
    private TemplateCard templateCard;

    public WechatWorkTemplateCardMessage(TemplateCard templateCard) {
        this.templateCard = templateCard;
        setMsgType("template_card");
    }

    public static WechatWorkTemplateCardMessage build(MessageModel messageModel, String content) {
        String title = StringUtils.defaultIfBlank(messageModel.getTitle(), "Jenkins Build Notice");
        String actionUrl = StringUtils.defaultIfBlank(resolveActionUrl(messageModel), messageModel.getJobUrl());
        TemplateCard card = new TemplateCard();
        card.setCardType(CARD_TYPE_NEWS_NOTICE);
        card.setSource(buildSource(messageModel));
        card.setMainTitle(new MainTitle(title, null));
        card.setCardImage(buildCardImage(messageModel));
        card.setHorizontalContentList(resolveHorizontalContentList(messageModel));
        card.setVerticalContentList(resolveVerticalContentList(messageModel, content));
        card.setJumpList(resolveJumpList(messageModel));
        card.setCardAction(new CardAction(LINK_TYPE, actionUrl));
        return new WechatWorkTemplateCardMessage(card);
    }

    private static Source buildSource(MessageModel messageModel) {
        return new Source(DEFAULT_SOURCE_ICON_URL, SOURCE_DESCRIPTION, resolveSourceColor(messageModel.getStatusType()));
    }

    private static CardImage buildCardImage(MessageModel messageModel) {
        return new CardImage(resolveCardImageUrl(messageModel), CARD_IMAGE_ASPECT_RATIO);
    }

    private static String resolveActionUrl(MessageModel messageModel) {
        if (!CollectionUtils.isEmpty(messageModel.getButtons())) {
            return StringUtils.defaultIfBlank(messageModel.getButtons().get(0).getUrl(), messageModel.getMessageUrl());
        }
        return StringUtils.defaultIfBlank(messageModel.getMessageUrl(), messageModel.getSingleUrl());
    }

    private static String resolveCardImageUrl(MessageModel messageModel) {
        String topImageUrl = messageModel.getTopImg() == null ? null : messageModel.getTopImg().getImgKey();
        if (isHttpUrl(topImageUrl)) {
            return topImageUrl;
        }
        if (isHttpUrl(messageModel.getPicUrl())) {
            return messageModel.getPicUrl();
        }
        return DEFAULT_CARD_IMAGE_URL;
    }

    private static boolean isHttpUrl(String value) {
        String url = StringUtils.trimToEmpty(value);
        return StringUtils.startsWith(url, "https://") || StringUtils.startsWith(url, "http://");
    }

    private static List<Jump> resolveJumpList(MessageModel messageModel) {
        if (CollectionUtils.isEmpty(messageModel.getButtons())) {
            return null;
        }
        return messageModel.getButtons().stream()
                .filter(button -> StringUtils.isNotBlank(button.getText()) && StringUtils.isNotBlank(button.getUrl()))
                .limit(MAX_JUMP_ITEMS)
                .map(button -> new Jump(LINK_TYPE, button.getUrl(), button.getText()))
                .toList();
    }

    private static List<HorizontalContent> resolveHorizontalContentList(MessageModel messageModel) {
        Locale locale = messageModel.getLocale();
        List<HorizontalContent> contents = new ArrayList<>();
        addHorizontalContent(contents, NoticeI18n.buildMessageProjectName(locale), messageModel.getProjectName(),
                messageModel.getProjectUrl());
        addHorizontalContent(contents, NoticeI18n.buildMessageJobName(locale), messageModel.getJobName(),
                messageModel.getJobUrl());
        addHorizontalContent(contents, NoticeI18n.buildMessageStatus(locale), resolveStatusLabel(messageModel, locale), null);
        addHorizontalContent(contents, NoticeI18n.buildMessageDuration(locale), messageModel.getDuration(), null);
        addHorizontalContent(contents, NoticeI18n.buildMessageExecutor(locale), messageModel.getExecutorName(), null);
        return contents.isEmpty() ? null : contents;
    }

    private static void addHorizontalContent(List<HorizontalContent> contents, String key, String value, String url) {
        if (StringUtils.isBlank(value)) {
            return;
        }
        boolean hasUrl = StringUtils.isNotBlank(url);
        contents.add(new HorizontalContent(hasUrl ? LINK_TYPE : TEXT_TYPE, key, value, hasUrl ? url : null));
    }

    private static List<VerticalContent> resolveVerticalContentList(MessageModel messageModel, String content) {
        if (StringUtils.isNotBlank(messageModel.getAdditionalContent())) {
            return List.of(new VerticalContent(StringUtils.defaultString(messageModel.getTitle()),
                    messageModel.getAdditionalContent()));
        }
        if (hasStructuredBuildFields(messageModel)) {
            return null;
        }
        String plainText = toPlainText(content);
        if (StringUtils.isBlank(plainText)) {
            return null;
        }
        return List.of(new VerticalContent(StringUtils.defaultString(messageModel.getTitle()), plainText));
    }

    private static boolean hasStructuredBuildFields(MessageModel messageModel) {
        return StringUtils.isNotBlank(messageModel.getProjectName())
                || StringUtils.isNotBlank(messageModel.getJobName())
                || StringUtils.isNotBlank(messageModel.getDuration())
                || StringUtils.isNotBlank(messageModel.getExecutorName());
    }

    private static String toPlainText(String markdown) {
        return StringUtils.defaultString(markdown)
                .replaceAll("(?m)^>\\s*", "")
                .replaceAll("\\*\\*([^*]+)\\*\\*", "$1")
                .replaceAll("\\[([^\\]]+)]\\(([^)]+)\\)", "$1")
                .replaceAll("<font\\s+color=['\"][^'\"]+['\"]>(.*?)</font>", "$1")
                .replaceAll("(?m)^#{1,6}\\s*", "")
                .trim();
    }

    private static String resolveStatusLabel(MessageModel messageModel, Locale locale) {
        BuildStatusEnum statusType = messageModel.getStatusType();
        return statusType == null ? "" : statusType.getLabel(locale);
    }

    private static int resolveSourceColor(BuildStatusEnum statusType) {
        if (BuildStatusEnum.FAILURE.equals(statusType) || BuildStatusEnum.UNSTABLE.equals(statusType)) {
            return 2;
        }
        if (BuildStatusEnum.SUCCESS.equals(statusType)) {
            return 3;
        }
        return 0;
    }

    @Data
    @NoArgsConstructor
    public static class TemplateCard implements Serializable {

        @JsonProperty("card_type")
        private String cardType;

        private Source source;

        @JsonProperty("main_title")
        private MainTitle mainTitle;

        @JsonProperty("card_image")
        private CardImage cardImage;

        @JsonProperty("vertical_content_list")
        private List<VerticalContent> verticalContentList;

        @JsonProperty("horizontal_content_list")
        private List<HorizontalContent> horizontalContentList;

        @JsonProperty("jump_list")
        private List<Jump> jumpList;

        @JsonProperty("card_action")
        private CardAction cardAction;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Source implements Serializable {

        @JsonProperty("icon_url")
        private String iconUrl;

        private String desc;

        @JsonProperty("desc_color")
        private Integer descColor;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MainTitle implements Serializable {

        private String title;

        private String desc;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardImage implements Serializable {

        private String url;

        @JsonProperty("aspect_ratio")
        private Double aspectRatio;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerticalContent implements Serializable {

        private String title;

        private String desc;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HorizontalContent implements Serializable {

        private Integer type;

        private String keyname;

        private String value;

        private String url;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Jump implements Serializable {

        private Integer type;

        private String url;

        private String title;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardAction implements Serializable {

        private Integer type;

        private String url;
    }
}
