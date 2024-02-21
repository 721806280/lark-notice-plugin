package io.jenkins.plugins.lark.notice.model;

import io.jenkins.plugins.lark.notice.enums.BuildStatusEnum;
import io.jenkins.plugins.lark.notice.enums.RobotType;
import io.jenkins.plugins.lark.notice.tools.Utils;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.jenkins.plugins.lark.notice.enums.MsgTypeEnum.CARD;
import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.LF;

/**
 * Represents a model for a build job, encapsulating details about a specific build process.
 * This model includes information such as the project and job names, URLs, build status,
 * duration, executor details, and additional content related to the build.
 *
 * @author xm.z
 */
@Data
@Builder
public class BuildJobModel {

    /**
     * The title of the project associated with the build job.
     */
    private String title;

    /**
     * The name of the project associated with the build job.
     */
    private String projectName;

    /**
     * The URL pointing to the project's homepage or repository.
     */
    private String projectUrl;

    /**
     * The name of the build job. This often serves as an identifier within the CI/CD system.
     */
    private String jobName;

    /**
     * The URL pointing to the detailed page of the build job within the CI/CD system.
     */
    private String jobUrl;

    /**
     * The status of the build, represented as an enum value. It indicates the outcome of the build process
     * (e.g., success, failure).
     */
    private BuildStatusEnum statusType;

    /**
     * The duration of the build process. This is typically a human-readable string representing the time taken
     * to complete the build.
     */
    private String duration;

    /**
     * The name of the person or system that executed the build. This can be useful for tracking responsibility
     * and ownership of the build process.
     */
    private String executorName;

    /**
     * The mobile number of the executor. This could be used for sending notifications or alerts related to the build.
     */
    private String executorMobile;

    /**
     * The OpenID of the executor. In systems integrated with WeChat Work or similar platforms, this can be used
     * to identify users uniquely.
     */
    private String executorOpenId;

    /**
     * Additional content or notes related to the build job. This could include logs, error messages, or custom
     * messages intended for reporting or notification purposes.
     */
    private String content;

    /**
     * Converts the build job details into a Markdown formatted string.
     * This is useful for generating readable and formatted messages for notifications or reports.
     *
     * @return A string in Markdown format containing the build job details.
     */
    public String toMarkdown(RobotType robotType) {
        boolean hasDingTask = RobotType.DING_TAlK.equals(robotType);
        String tagName = robotType.getStatusTagName();
        List<String> lines = new ArrayList<>();
        // 如果是钉钉任务，先添加特定格式的标题和分隔线
        if (hasDingTask) {
            lines.add(String.format("## <%s color='%s'>%s</%s>", tagName, statusType.getColor(), title, tagName));
            lines.add("---");
        }
        // 添加通用信息
        Collections.addAll(lines,
                String.format("\uD83D\uDCCB **任务名称**: [%s](%s)", projectName, projectUrl),
                String.format("\uD83D\uDD22 **任务编号**: [%s](%s)", jobName, jobUrl),
                String.format("\uD83C\uDF1F **构建状态**:  <%s color='%s'>%s</%s>",
                        tagName, statusType.getColor(), statusType.getLabel(), tagName),
                String.format("\uD83D\uDD50 **构建用时**:  %s", duration),
                String.format("\uD83D\uDC64 **执  行 者** :  %s", executorName),
                content == null ? "" : content
        );
        return String.join(hasDingTask ? "  " + LF : LF, lines);
    }

    /**
     * Prepares a {@link MessageModel.MessageModelBuilder} instance with pre-populated fields based on the build job details.
     * This builder can then be used to further customize and create a {@link MessageModel} instance for messaging purposes.
     *
     * @return A {@link MessageModel.MessageModelBuilder} instance with pre-populated fields.
     */
    public MessageModel.MessageModelBuilder messageModelBuilder() {
        return MessageModel.builder().type(CARD).statusType(statusType)
                .buttons(Utils.createDefaultButtons(jobUrl)).title(title);
    }

}