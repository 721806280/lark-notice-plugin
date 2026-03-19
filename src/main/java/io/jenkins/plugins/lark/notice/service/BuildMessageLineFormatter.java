package io.jenkins.plugins.lark.notice.service;

import io.jenkins.plugins.lark.notice.enums.RobotType;
import io.jenkins.plugins.lark.notice.i18n.NoticeI18n;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Formats the shared build message body lines used by both templates and runtime messages.
 */
public final class BuildMessageLineFormatter {

    private BuildMessageLineFormatter() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Builds the shared message body lines.
     *
     * @param locale              locale used for labels
     * @param robotType           robot platform type
     * @param values              message line values
     * @param includeBlankContent whether to append a blank content line when content is empty
     * @return ordered list of message lines
     */
    public static List<String> buildBodyLines(Locale locale,
                                              RobotType robotType,
                                              BuildMessageLineValues values,
                                              boolean includeBlankContent) {
        String tagName = robotType.getStatusTagName();
        List<String> lines = new ArrayList<>();

        Collections.addAll(lines,
                String.format("\uD83D\uDCCB **%s**: [%s](%s)",
                        NoticeI18n.buildMessageProjectName(locale),
                        values.projectName(),
                        values.projectUrl()),
                String.format("\uD83D\uDD22 **%s**: [%s](%s)",
                        NoticeI18n.buildMessageJobName(locale),
                        values.jobName(),
                        values.jobUrl()),
                String.format("\uD83C\uDF1F **%s**:  <%s color='%s'>%s</%s>",
                        NoticeI18n.buildMessageStatus(locale),
                        tagName,
                        values.statusColor(),
                        values.statusLabel(),
                        tagName),
                String.format("\uD83D\uDD50 **%s**:  %s",
                        NoticeI18n.buildMessageDuration(locale),
                        values.duration()),
                String.format("\uD83D\uDC64 **%s**:  %s",
                        NoticeI18n.buildMessageExecutor(locale),
                        values.executorName())
        );

        if (includeBlankContent) {
            lines.add(values.content() == null ? "" : values.content());
        } else if (StringUtils.isNotBlank(values.content())) {
            lines.add(values.content());
        }

        return lines;
    }

    /**
     * Message values used to format the shared body lines.
     *
     * @param projectName  project name
     * @param projectUrl   project URL
     * @param jobName      job name
     * @param jobUrl       job URL
     * @param statusLabel  status label
     * @param statusColor  status color used in the tag
     * @param duration     build duration
     * @param executorName executor name
     * @param content      optional content line
     */
    public record BuildMessageLineValues(String projectName,
                                         String projectUrl,
                                         String jobName,
                                         String jobUrl,
                                         String statusLabel,
                                         String statusColor,
                                         String duration,
                                         String executorName,
                                         String content) {
    }
}
