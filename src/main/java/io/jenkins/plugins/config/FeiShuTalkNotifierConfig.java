package io.jenkins.plugins.config;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import io.jenkins.plugins.enums.NoticeOccasionEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * FeiShuTalkNotifierConfig 类用于存储飞书通知器的配置信息。
 *
 * <p>FeiShuTalkNotifierConfig 类继承了 AbstractDescribableImpl 类，表示它是一个可描述的 Jenkins 插件组件。
 * FeiShuTalkNotifierConfig 类可以通过 Jenkins 界面进行编辑，以便设置飞书通知器的相关参数。</p>
 *
 * <p>该类包含多个属性，这些属性用于描述飞书通知器的具体信息。例如，robotId 表示飞书机器人的唯一标识符，
 * atAll 表示是否需要在消息中 @ 所有人等。</p>
 *
 * <p>FeiShuTalkNotifierConfig 类提供了多个构造方法和公共方法，可以方便地对其属性进行操作。</p>
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class FeiShuTalkNotifierConfig extends AbstractDescribableImpl<FeiShuTalkNotifierConfig> {

    /**
     * 是否使用原生消息格式
     */
    private boolean raw;

    /**
     * 配置是否已禁用
     */
    private boolean disabled;

    /**
     * 配置是否已被选中
     */
    private boolean checked;

    /**
     * 机器人 ID
     */
    private String robotId;

    /**
     * 机器人名称
     */
    private String robotName;

    /**
     * 是否 @ 所有人
     */
    private boolean atAll;

    /**
     * 需要 @ 的用户 OpenID
     */
    private String atOpenId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息模板
     */
    private String message;

    /**
     * 需要进行哪些通知的 Set 集合
     */
    private Set<String> noticeOccasions;

    /**
     * FeiShuTalkNotifierConfig 的 DataBoundConstructor 构造方法。
     * 该方法可在 Jenkins 界面中将表单数据绑定到 FeiShuTalkNotifierConfig 实例上。
     *
     * @param raw             是否使用原生消息格式。
     * @param disabled        配置是否已禁用。
     * @param checked         配置是否已被选中。
     * @param robotId         机器人 ID。
     * @param robotName       机器人名称。
     * @param atAll           是否 @ 所有人。
     * @param atOpenId        需要 @ 的用户 OpenID。
     * @param content         消息内容。
     * @param message         消息模板。
     * @param noticeOccasions 需要进行哪些通知的 Set 集合。
     */
    @DataBoundConstructor
    public FeiShuTalkNotifierConfig(boolean raw, boolean disabled, boolean checked, String robotId, String robotName,
                                    boolean atAll, String atOpenId, String content, String message, Set<String> noticeOccasions) {
        this.raw = raw;
        this.disabled = disabled;
        this.checked = checked;
        this.robotId = robotId;
        this.robotName = robotName;
        this.atAll = atAll;
        this.atOpenId = atOpenId;
        this.content = content;
        this.message = message;
        this.noticeOccasions = noticeOccasions;
    }

    /**
     * 根据给定的 robotConfig 构造一个 FeiShuTalkNotifierConfig 实例。
     * 该方法会使用默认的 noticeOccasion 值。
     *
     * @param robotConfig 飞书机器人配置信息。
     */
    public FeiShuTalkNotifierConfig(FeiShuTalkRobotConfig robotConfig) {
        this(false, false, false, robotConfig.getId(), robotConfig.getName(), false,
                null, null, null, getDefaultNoticeOccasions());
    }

    /**
     * 获取默认的通知时机集合。
     *
     * @return 默认的通知时机集合。
     */
    private static Set<String> getDefaultNoticeOccasions() {
        return FeiShuTalkGlobalConfig.getInstance().getNoticeOccasions();
    }

    /**
     * 获取通知时机。
     *
     * <p>如果当前通知器的 noticeOccasions 字段为 null，则返回默认的通知时机集合。</p>
     *
     * @return 通知时机集合。
     */
    public Set<String> getNoticeOccasions() {
        return noticeOccasions == null ? getDefaultNoticeOccasions() : noticeOccasions;
    }

    /**
     * 根据环境变量解析 atOpenIds。
     *
     * <p>如果 atOpenId 属性为空，则返回空的 Set 集合。</p>
     *
     * @param envVars 环境变量。
     * @return 解析后的 atOpenId 集合。
     */
    public Set<String> resolveAtOpenIds(EnvVars envVars) {
        if (StringUtils.isEmpty(atOpenId)) {
            return new HashSet<>(16);
        }
        String realOpenId = envVars.expand(atOpenId);
        return Arrays.stream(StringUtils.split(realOpenId.replace("\n", ","), ","))
                .collect(Collectors.toSet());
    }

    /**
     * 获取内容属性的值。
     *
     * <p>如果 content 为 null，则返回空字符串。</p>
     *
     * @return 内容属性的值。
     */
    public String getContent() {
        return content == null ? "" : content;
    }

    /**
     * 将 notifierConfig 的属性值赋予当前实例。
     *
     * @param notifierConfig 消息通知器配置信息。
     */
    public void copy(FeiShuTalkNotifierConfig notifierConfig) {
        this.setRaw(notifierConfig.isRaw());
        this.setDisabled(notifierConfig.isDisabled());
        this.setChecked(notifierConfig.isChecked());
        this.setAtAll(notifierConfig.isAtAll());
        this.setAtOpenId(notifierConfig.getAtOpenId());
        this.setContent(notifierConfig.getContent());
        this.setMessage(notifierConfig.getMessage());
        this.setNoticeOccasions(notifierConfig.getNoticeOccasions());
    }

    /**
     * FeiShuTalkNotifierConfigDescriptor 是 FeiShuTalkNotifierConfig 的描述符。
     * 该类用于在 Jenkins 界面上展示 FeiShuTalkNotifierConfig 的属性。
     */
    @Extension
    public static class FeiShuTalkNotifierConfigDescriptor extends Descriptor<FeiShuTalkNotifierConfig> {

        /**
         * 获取可供选择的通知时机列表。
         *
         * @return 可供选择的通知时机列表。
         */
        public NoticeOccasionEnum[] getNoticeOccasionTypes() {
            return NoticeOccasionEnum.values();
        }
    }
}