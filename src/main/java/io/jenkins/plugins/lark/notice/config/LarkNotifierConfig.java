package io.jenkins.plugins.lark.notice.config;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.enums.NoticeOccasionEnum;
import io.jenkins.plugins.lark.notice.service.NotifierTemplatePreviewService;
import io.jenkins.plugins.lark.notice.tools.ApiResponse;
import io.jenkins.plugins.lark.notice.tools.HttpResponses;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

import jenkins.model.Jenkins;
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
public class LarkNotifierConfig implements Describable<LarkNotifierConfig> {

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
        setNoticeOccasions(noticeOccasions);
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
     * Sets the notice occasions for this notifier configuration.
     *
     * @param noticeOccasions set of occasion names; null clears the override
     */
    public void setNoticeOccasions(Set<String> noticeOccasions) {
        this.noticeOccasions = noticeOccasions == null ? null : new HashSet<>(noticeOccasions);
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
     * Returns a UI-friendly robot label that includes the resolved robot platform when available.
     *
     * @return display label such as {@code Bot A (Lark)}
     */
    public String getRobotDisplayName() {
        String robotTypeLabel = LarkGlobalConfig.getRobot(getRobotId())
                .map(LarkRobotConfig::getProviderDisplayName)
                .orElse(null);
        return StringUtils.isBlank(robotTypeLabel) ? getRobotName() : String.format("%s (%s)", getRobotName(), robotTypeLabel);
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

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.plugin_name();
        }

        /**
         * Retrieves a list of selectable notice occasions.
         *
         * @return Array of selectable notice occasions
         */
        public NoticeOccasionEnum[] getNoticeOccasionTypes() {
            return NoticeOccasionEnum.values();
        }

        /**
         * Exposes the shared Jelly view owner class for stable {@code st:include} lookups.
         *
         * @return shared view owner class
         */
        public Class<?> getSharedViewsClass() {
            return SharedConfigViews.class;
        }

        /**
         * Loads the built-in editable template for the selected robot and current notifier fields.
         *
         * @return JSON response containing the template text
         */
        @RequirePOST
        public HttpResponse doLoadDefaultTemplate(@QueryParameter String robotId,
                                                  @QueryParameter String title,
                                                  @QueryParameter String content) {
            Jenkins.get().checkPermission(Jenkins.READ);

            ApiResponse response = NotifierTemplatePreviewService.loadDefaultTemplate(robotId, title, content);
            return HttpResponses.json(response);
        }
    }

}
