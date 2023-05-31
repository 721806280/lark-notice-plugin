package io.jenkins.plugins.model;

import io.jenkins.plugins.enums.BuildStatusEnum;
import io.jenkins.plugins.tools.Utils;
import lombok.Builder;
import lombok.Data;

import java.util.Arrays;

/**
 * 用于存储构建任务相关的模型。
 *
 * @author xm.z
 */
@Data
@Builder
public class BuildJobModel {

    /**
     * 项目名称。
     */
    private String projectName;

    /**
     * 项目 URL。
     */
    private String projectUrl;

    /**
     * 构建任务名称。
     */
    private String jobName;

    /**
     * 构建任务 URL。
     */
    private String jobUrl;

    /**
     * 构建任务状态。
     */
    private BuildStatusEnum statusType;

    /**
     * 构建持续时间。
     */
    private String duration;

    /**
     * 执行构建任务的用户名称。
     */
    private String executorName;

    /**
     * 执行构建任务的用户手机号码。
     */
    private String executorMobile;

    /**
     * 额外的任务执行信息。
     */
    private String content;

    /**
     * 将该模型转化为 Markdown 格式的字符串。
     *
     * @return Markdown 格式的字符串。
     */
    public String toMarkdown() {
        return Utils.join(
                Arrays.asList(
                        String.format("\uD83D\uDCCB **任务名称**：[%s](%s)", projectName, projectUrl),
                        String.format("\uD83D\uDD22 **任务编号**：[%s](%s)", jobName, jobUrl),
                        String.format("\uD83C\uDF1F **构建状态**:  %s", Utils.dye(statusType.getLabel(), statusType.getColor())),
                        String.format("\uD83D\uDD50 **构建用时**:  %s", duration),
                        String.format("\uD83D\uDC64 **执  行 者**:  %s", executorName),
                        content == null ? "" : content
                )
        );
    }
}