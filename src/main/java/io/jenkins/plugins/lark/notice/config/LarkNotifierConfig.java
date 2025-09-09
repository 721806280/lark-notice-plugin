package io.jenkins.plugins.lark.notice.config;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import io.jenkins.plugins.lark.notice.enums.NoticeOccasionEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.COMMA;
import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.LF;

/**
 * Configuration class for Lark notifier. This class holds the settings for a Lark notification,
 * such as message format, robot details, and occasions for sending notifications.
 *
 * @author xm.z
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class LarkNotifierConfig extends AbstractDescribableImpl<LarkNotifierConfig> {

    /**
     * Whether to use the raw message format.
     */
    private boolean raw;

    /**
     * Whether the configuration is disabled.
     */
    private boolean disabled;

    /**
     * Whether the configuration is selected.
     */
    private boolean checked;

    /**
     * Robot ID.
     */
    private String robotId;

    /**
     * Robot name.
     */
    private String robotName;

    /**
     * Whether to @mention all users.
     */
    private boolean atAll;

    /**
     * OpenID of the user(s) to @mention.
     */
    private String atUserId;

    /**
     * Message title.
     */
    private String title;

    /**
     * Message content.
     */
    private String content;

    /**
     * Message template.
     */
    private String message;

    /**
     * Set of occasions for which notifications should be sent.
     */
    private Set<String> noticeOccasions;

    /**
     * DataBoundConstructor for binding form data to LarkNotifierConfig instance.
     *
     * @param raw             Whether to use raw message format
     * @param disabled        Whether the configuration is disabled
     * @param checked         Whether the configuration is selected
     * @param robotId         Robot ID
     * @param robotName       Robot name
     * @param atAll           Whether to @mention all users
     * @param atUserId        OpenID of the user(s) to @mention
     * @param title           Message title
     * @param content         Message content
     * @param message         Message template
     * @param noticeOccasions Set of occasions for notifications
     */
    @DataBoundConstructor
    public LarkNotifierConfig(boolean raw, boolean disabled, boolean checked, String robotId, String robotName,
                              boolean atAll, String atUserId, String title, String content, String message, Set<String> noticeOccasions) {
        this.raw = raw;
        this.disabled = disabled;
        this.checked = checked;
        this.robotId = robotId;
        this.robotName = robotName;
        this.atAll = atAll;
        this.atUserId = atUserId;
        this.title = title;
        this.content = content;
        this.message = message;
        this.noticeOccasions = noticeOccasions;
    }

    /**
     * Constructs a LarkNotifierConfig instance from a given robotConfig.
     * Uses default noticeOccasion values.
     *
     * @param robotConfig Lark robot configuration
     */
    public LarkNotifierConfig(LarkRobotConfig robotConfig) {
        this(false, false, false, robotConfig.getId(), robotConfig.getName(), false,
                null, null, null, null, getDefaultNoticeOccasions());
    }

    /**
     * Retrieves the default set of notice occasions.
     *
     * @return Default set of notice occasions
     */
    private static Set<String> getDefaultNoticeOccasions() {
        return LarkGlobalConfig.getInstance().getNoticeOccasions();
    }

    /**
     * Gets the notice occasions.
     * Returns the default notice occasions if the current notifier's noticeOccasions field is null.
     *
     * @return Set of notice occasions
     */
    public Set<String> getNoticeOccasions() {
        return noticeOccasions == null ? getDefaultNoticeOccasions() : noticeOccasions;
    }

    /**
     * Resolves atUserIds based on environment variables.
     * Returns an empty Set if atUserId property is empty.
     *
     * @param envVars Environment variables
     * @return Set of resolved atUserIds
     */
    public Set<String> resolveAtUserIds(EnvVars envVars) {
        if (StringUtils.isBlank(atUserId)) {
            return new HashSet<>(16);
        }
        String realOpenId = envVars.expand(atUserId);
        return Arrays.stream(StringUtils.split(realOpenId.replace(LF, COMMA), COMMA))
                .collect(Collectors.toSet());
    }

    /**
     * Returns the value of the content property.
     * Returns an empty string if content is null.
     *
     * @return Value of the content property
     */
    public String getContent() {
        return content == null ? "" : content;
    }

    /**
     * Copies properties from another notifierConfig instance to this one.
     *
     * @param notifierConfig Notification configuration to copy from
     */
    public void copy(LarkNotifierConfig notifierConfig) {
        this.setRaw(notifierConfig.isRaw());
        this.setDisabled(notifierConfig.isDisabled());
        this.setChecked(notifierConfig.isChecked());
        this.setAtAll(notifierConfig.isAtAll());
        this.setAtUserId(notifierConfig.getAtUserId());
        this.setTitle(notifierConfig.getTitle());
        this.setContent(notifierConfig.getContent());
        this.setMessage(notifierConfig.getMessage());
        this.setNoticeOccasions(notifierConfig.getNoticeOccasions());
    }

    /**
     * Descriptor for LarkNotifierConfig.
     * Used for displaying properties of LarkNotifierConfig in the Jenkins UI.
     */
    @Extension
    public static class LarkNotifierConfigDescriptor extends Descriptor<LarkNotifierConfig> {

        /**
         * Retrieves a list of selectable notice occasions.
         *
         * @return Array of selectable notice occasions
         */
        public NoticeOccasionEnum[] getNoticeOccasionTypes() {
            return NoticeOccasionEnum.values();
        }
    }

}