package io.jenkins.plugins.lark.notice.service;

import hudson.BulkChange;
import hudson.model.AbstractProject;
import hudson.model.Descriptor.FormException;
import hudson.model.Item;
import hudson.model.Job;
import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import io.jenkins.plugins.lark.notice.config.LarkNotifier;
import io.jenkins.plugins.lark.notice.config.LarkNotifierConfig;
import io.jenkins.plugins.lark.notice.config.LarkRobotConfig;
import io.jenkins.plugins.lark.notice.config.property.LarkJobProperty;
import io.jenkins.plugins.lark.notice.tools.ApiResponse;
import jenkins.branch.MultiBranchProject;
import jenkins.model.Jenkins;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;
import lombok.Getter;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * Service for robot-centric job binding queries and updates.
 *
 * @author xm.z
 */
public final class RobotJobBindingService {
    private RobotJobBindingService() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Loads one robot's job binding data for the standalone management page.
     *
     * @param robotId    selected robot ID
     * @param keyword    optional full name keyword
     * @param stateValue optional state filter
     * @return response payload containing summary and filtered jobs
     */
    public static ApiResponse load(String robotId, String keyword, String stateValue) {
        try {
            LoadRequest request = parseLoadRequest(robotId, keyword, stateValue);
            BindingDataset dataset = buildDataset(request.robotConfig(), request.keyword(), request.stateFilter());
            return ApiResponse.ok().data(JSONObject.fromObject(dataset));
        } catch (FormException ex) {
            return ApiResponse.fail(ex.getMessage());
        } catch (Exception ex) {
            return ApiResponse.fail(buildInvalidRequestMessage(ex));
        }
    }

    /**
     * Applies selected bind and unbind changes from the standalone management page.
     *
     * @param robotId                   selected robot ID
     * @param bindJobFullNamesPayload   newline-delimited jobs to bind
     * @param unbindJobFullNamesPayload newline-delimited jobs to unbind
     * @return apply result payload
     */
    public static ApiResponse applySelection(String robotId, String bindJobFullNamesPayload, String unbindJobFullNamesPayload) {
        try {
            SelectionRequest request = parseSelectionRequest(robotId, bindJobFullNamesPayload, unbindJobFullNamesPayload);
            SelectionApplyReport report = applySelection(request);
            if (report.getFailedJobCount() > 0) {
                return ApiResponse.fail(Messages.job_binding_apply_partial_failure(
                        report.getChangedJobCount(),
                        report.getFailedJobCount(),
                        report.getRobotName()
                )).data(JSONObject.fromObject(report));
            }
            return ApiResponse.ok(
                    Messages.job_binding_apply_success(report.getChangedJobCount(), report.getRobotName()),
                    JSONObject.fromObject(report)
            );
        } catch (FormException ex) {
            return ApiResponse.fail(ex.getMessage());
        } catch (Exception ex) {
            return ApiResponse.fail(buildInvalidRequestMessage(ex));
        }
    }

    private static LoadRequest parseLoadRequest(String robotId, String keyword, String stateValue) throws FormException {
        LarkRobotConfig robotConfig = requireRobot(robotId);
        return new LoadRequest(
                robotConfig,
                StringUtils.trimToEmpty(keyword),
                JobFilterState.fromValue(stateValue)
        );
    }

    private static SelectionRequest parseSelectionRequest(String robotId,
                                                          String bindJobFullNamesPayload,
                                                          String unbindJobFullNamesPayload) throws FormException {
        LarkRobotConfig robotConfig = requireRobot(robotId);
        List<String> bindJobNames = parseJobNames(bindJobFullNamesPayload);
        List<String> unbindJobNames = parseJobNames(unbindJobFullNamesPayload);
        if (bindJobNames.isEmpty() && unbindJobNames.isEmpty()) {
            throw new FormException(Messages.job_binding_jobs_missing(), "bindJobFullNames");
        }
        return new SelectionRequest(robotConfig, bindJobNames, unbindJobNames);
    }

    private static LarkRobotConfig requireRobot(String robotId) throws FormException {
        if (StringUtils.isBlank(robotId)) {
            throw new FormException(Messages.job_binding_robot_missing(), "robotId");
        }
        Optional<LarkRobotConfig> robotConfigOpt = LarkGlobalConfig.getRobot(robotId);
        if (robotConfigOpt.isEmpty()) {
            throw new FormException(Messages.job_binding_robot_missing(), "robotId");
        }
        return robotConfigOpt.get();
    }

    private static List<String> parseJobNames(String payload) {
        if (StringUtils.isBlank(payload)) {
            return List.of();
        }

        String normalized = StringUtils.replace(payload, "\r\n", "\n");
        normalized = StringUtils.replace(normalized, "\r", "\n");

        LinkedHashSet<String> jobNames = new LinkedHashSet<>();
        for (String line : normalized.split("\n")) {
            String trimmed = StringUtils.trimToNull(line);
            if (trimmed != null) {
                jobNames.add(trimmed);
            }
        }
        return new ArrayList<>(jobNames);
    }

    private static BindingDataset buildDataset(LarkRobotConfig robotConfig,
                                               String keyword,
                                               JobFilterState filterState) {
        List<JobBindingRow> summaryRows = collectRows(robotConfig, "");
        sortRows(summaryRows);

        List<JobBindingRow> queryRows = collectRows(robotConfig, keyword);
        sortRows(queryRows);

        Summary summary = Summary.from(summaryRows);
        List<JobBindingRow> matchingRows = queryRows.stream()
                .filter(filterState::matches)
                .toList();
        return new BindingDataset(
                robotConfig.getId(),
                robotConfig.getName(),
                robotConfig.getProviderDisplayName(),
                summary,
                matchingRows,
                filterState.getValue(),
                keyword,
                matchingRows.size()
        );
    }

    private static void sortRows(List<JobBindingRow> rows) {
        rows.sort(Comparator
                .comparingInt((JobBindingRow row) -> stateSortOrder(row.getState()))
                .thenComparing(JobBindingRow::getJobFullName, String.CASE_INSENSITIVE_ORDER));
    }

    private static int stateSortOrder(String state) {
        if (StringUtils.equals(JobBindingState.BOUND.getValue(), state)) {
            return 0;
        }
        if (StringUtils.equals(JobBindingState.DISABLED.getValue(), state)) {
            return 1;
        }
        if (StringUtils.equals(JobBindingState.UNBOUND.getValue(), state)) {
            return 2;
        }
        if (StringUtils.equals(JobBindingState.CONFLICT.getValue(), state)) {
            return 3;
        }
        if (StringUtils.equals(JobBindingState.UNSUPPORTED.getValue(), state)) {
            return 4;
        }
        if (StringUtils.equals(JobBindingState.FORBIDDEN.getValue(), state)) {
            return 5;
        }
        return 6;
    }

    private static List<JobBindingRow> collectRows(LarkRobotConfig robotConfig, String keyword) {
        Collection<Job> jobs = Jenkins.get().getAllItems(Job.class);
        List<JobBindingRow> rows = new ArrayList<>();
        for (Job<?, ?> job : jobs) {
            if (!matchesKeyword(job, keyword)) {
                continue;
            }
            rows.add(describeJob(robotConfig, job));
        }
        return rows;
    }

    private static boolean matchesKeyword(Job<?, ?> job, String keyword) {
        return StringUtils.isBlank(keyword)
                || StringUtils.containsIgnoreCase(job.getFullName(), keyword)
                || StringUtils.containsIgnoreCase(job.getDisplayName(), keyword);
    }

    private static JobBindingRow describeJob(LarkRobotConfig robotConfig, Job<?, ?> job) {
        JobBindingState state = resolveState(robotConfig, job);
        boolean actionable = state.actionable();
        return new JobBindingRow(
                job.getFullName(),
                job.getDisplayName(),
                StringUtils.trimToEmpty(job.getDescription()),
                job.getClass().getSimpleName(),
                state.getValue(),
                state.initiallyChecked(),
                actionable,
                state.reason(job.getFullName())
        );
    }

    private static JobBindingState resolveState(LarkRobotConfig robotConfig, Job<?, ?> job) {
        if (!job.hasPermission(Item.CONFIGURE)) {
            return JobBindingState.FORBIDDEN;
        }
        if (job.getParent() instanceof MultiBranchProject) {
            return JobBindingState.UNSUPPORTED;
        }
        if (hasFreestylePostBuildNotifier(job)) {
            return JobBindingState.CONFLICT;
        }

        LarkJobProperty property = job.getProperty(LarkJobProperty.class);
        if (property == null || property.getLarkNotifierConfigs() == null) {
            return JobBindingState.UNBOUND;
        }

        for (LarkNotifierConfig notifierConfig : property.getLarkNotifierConfigs()) {
            if (!StringUtils.equals(robotConfig.getId(), notifierConfig.getRobotId())) {
                continue;
            }
            if (notifierConfig.isChecked() && !notifierConfig.isDisabled()) {
                return JobBindingState.BOUND;
            }
            return JobBindingState.DISABLED;
        }
        return JobBindingState.UNBOUND;
    }

    private static SelectionApplyReport applySelection(SelectionRequest request) {
        SelectionApplyReport report = new SelectionApplyReport(request.robotConfig().getId(), request.robotConfig().getName());

        for (String jobName : request.bindJobFullNames()) {
            report.record(applyOne(request.robotConfig(), jobName, SelectionOperation.BIND));
        }
        for (String jobName : request.unbindJobFullNames()) {
            if (request.bindJobFullNames().contains(jobName)) {
                continue;
            }
            report.record(applyOne(request.robotConfig(), jobName, SelectionOperation.UNBIND));
        }

        return report;
    }

    private static SelectionResult applyOne(LarkRobotConfig robotConfig, String jobFullName, SelectionOperation operation) {
        Job<?, ?> job = Jenkins.get().getItemByFullName(jobFullName, Job.class);
        if (job == null) {
            return SelectionResult.skipped(jobFullName, SelectionAction.JOB_NOT_FOUND, Messages.job_binding_result_job_missing(jobFullName));
        }

        JobBindingState state = resolveState(robotConfig, job);
        if (!state.actionable()) {
            return SelectionResult.skipped(job.getFullName(), state.skipAction(), state.reason(job.getFullName()));
        }

        try {
            if (SelectionOperation.BIND.equals(operation)) {
                applyBind(robotConfig, job, state);
            } else {
                applyUnbind(robotConfig, job, state);
            }
            return SelectionResult.changed(job.getFullName(), operation.changedAction(state), operation.successMessage(job.getFullName(), state));
        } catch (IOException ex) {
            String detail = StringUtils.defaultIfBlank(ex.getMessage(), ex.getClass().getSimpleName());
            return SelectionResult.failed(
                    job.getFullName(),
                    operation.failedAction(),
                    Messages.job_binding_result_apply_failed(detail)
            );
        }
    }

    private static void applyBind(LarkRobotConfig robotConfig, Job<?, ?> job, JobBindingState state) throws IOException {
        LarkJobProperty property = job.getProperty(LarkJobProperty.class);
        List<LarkNotifierConfig> existingConfigs = property == null || property.getLarkNotifierConfigs() == null
                ? new ArrayList<>()
                : new ArrayList<>(property.getLarkNotifierConfigs());

        if (JobBindingState.DISABLED.equals(state)) {
            for (LarkNotifierConfig notifierConfig : existingConfigs) {
                if (StringUtils.equals(robotConfig.getId(), notifierConfig.getRobotId())) {
                    notifierConfig.setChecked(true);
                    notifierConfig.setDisabled(false);
                    break;
                }
            }
        } else if (indexOfRobotConfig(existingConfigs, robotConfig.getId()) < 0) {
            existingConfigs.add(createBoundNotifierConfig(robotConfig));
        }

        replaceJobProperty(job, existingConfigs);
    }

    private static void applyUnbind(LarkRobotConfig robotConfig, Job<?, ?> job, JobBindingState state) throws IOException {
        if (JobBindingState.UNBOUND.equals(state)) {
            return;
        }

        LarkJobProperty property = job.getProperty(LarkJobProperty.class);
        List<LarkNotifierConfig> existingConfigs = property == null || property.getLarkNotifierConfigs() == null
                ? new ArrayList<>()
                : new ArrayList<>(property.getLarkNotifierConfigs());
        existingConfigs.removeIf(config -> StringUtils.equals(robotConfig.getId(), config.getRobotId()));
        replaceJobProperty(job, existingConfigs);
    }

    private static void replaceJobProperty(Job<?, ?> job, List<LarkNotifierConfig> notifierConfigs) throws IOException {
        try (BulkChange bulkChange = new BulkChange(job)) {
            job.removeProperty(LarkJobProperty.class);
            if (!notifierConfigs.isEmpty()) {
                job.addProperty(new LarkJobProperty(notifierConfigs));
            }
            bulkChange.commit();
        }
    }

    private static int indexOfRobotConfig(List<LarkNotifierConfig> notifierConfigs, String robotId) {
        for (int i = 0; i < notifierConfigs.size(); i++) {
            if (StringUtils.equals(robotId, notifierConfigs.get(i).getRobotId())) {
                return i;
            }
        }
        return -1;
    }

    private static LarkNotifierConfig createBoundNotifierConfig(LarkRobotConfig robotConfig) {
        LarkNotifierConfig notifierConfig = new LarkNotifierConfig(robotConfig);
        notifierConfig.setChecked(true);
        notifierConfig.setDisabled(false);
        return notifierConfig;
    }

    private static boolean hasFreestylePostBuildNotifier(Job<?, ?> job) {
        if (job instanceof AbstractProject<?, ?> project) {
            return project.getPublishersList().get(LarkNotifier.class) != null;
        }
        return false;
    }

    private static String buildInvalidRequestMessage(Exception ex) {
        String detail = StringUtils.defaultIfBlank(ex.getMessage(), ex.getClass().getSimpleName());
        return Messages.job_binding_request_invalid(detail);
    }

    private enum JobFilterState {
        ALL("all"),
        BOUND("bound"),
        DISABLED("disabled"),
        UNBOUND("unbound"),
        SKIPPED("skipped");

        private final String value;

        JobFilterState(String value) {
            this.value = value;
        }

        private static JobFilterState fromValue(String value) {
            if (StringUtils.isBlank(value)) {
                return ALL;
            }
            for (JobFilterState state : values()) {
                if (StringUtils.equalsIgnoreCase(state.value, StringUtils.trimToEmpty(value))) {
                    return state;
                }
            }
            return ALL;
        }

        private boolean matches(JobBindingRow row) {
            return switch (this) {
                case BOUND -> JobBindingState.BOUND.getValue().equals(row.getState());
                case DISABLED -> JobBindingState.DISABLED.getValue().equals(row.getState());
                case UNBOUND -> JobBindingState.UNBOUND.getValue().equals(row.getState());
                case SKIPPED -> !row.isActionable();
                default -> true;
            };
        }

        private String getValue() {
            return value;
        }
    }

    private enum JobBindingState {
        BOUND("bound", true, true),
        UNBOUND("unbound", false, true),
        DISABLED("disabled", false, true),
        CONFLICT("conflict", false, false),
        UNSUPPORTED("unsupported", false, false),
        FORBIDDEN("forbidden", false, false);

        private final String value;
        private final boolean initiallyChecked;
        private final boolean actionable;

        JobBindingState(String value, boolean initiallyChecked, boolean actionable) {
            this.value = value;
            this.initiallyChecked = initiallyChecked;
            this.actionable = actionable;
        }

        private String getValue() {
            return value;
        }

        private boolean initiallyChecked() {
            return initiallyChecked;
        }

        private boolean actionable() {
            return actionable;
        }

        private SelectionAction skipAction() {
            return switch (this) {
                case FORBIDDEN -> SelectionAction.PERMISSION_DENIED;
                case UNSUPPORTED -> SelectionAction.MULTIBRANCH_UNSUPPORTED;
                case CONFLICT -> SelectionAction.FREESTYLE_PUBLISHER_UNSUPPORTED;
                default -> SelectionAction.NO_CHANGE;
            };
        }

        private String reason(String jobFullName) {
            return switch (this) {
                case DISABLED -> Messages.job_binding_state_disabled(jobFullName);
                case FORBIDDEN -> Messages.job_binding_result_permission_denied(jobFullName);
                case UNSUPPORTED -> Messages.job_binding_result_multibranch_unsupported(jobFullName);
                case CONFLICT -> Messages.job_binding_result_freestyle_publisher_unsupported(jobFullName);
                default -> null;
            };
        }
    }

    private enum SelectionOperation {
        BIND,
        UNBIND;

        private SelectionAction changedAction(JobBindingState state) {
            if (BIND.equals(this)) {
                return JobBindingState.DISABLED.equals(state) ? SelectionAction.ENABLE_BINDING : SelectionAction.ADD_BINDING;
            }
            return SelectionAction.REMOVE_BINDING;
        }

        private SelectionAction failedAction() {
            return BIND.equals(this) ? SelectionAction.ADD_BINDING : SelectionAction.REMOVE_BINDING;
        }

        private String successMessage(String jobFullName, JobBindingState state) {
            if (BIND.equals(this)) {
                return JobBindingState.DISABLED.equals(state)
                        ? Messages.job_binding_result_will_enable(jobFullName)
                        : Messages.job_binding_result_will_bind(jobFullName);
            }
            return Messages.job_binding_result_will_unbind(jobFullName);
        }
    }

    private enum SelectionAction {
        ADD_BINDING,
        ENABLE_BINDING,
        REMOVE_BINDING,
        JOB_NOT_FOUND,
        PERMISSION_DENIED,
        MULTIBRANCH_UNSUPPORTED,
        FREESTYLE_PUBLISHER_UNSUPPORTED,
        NO_CHANGE
    }

    private record LoadRequest(LarkRobotConfig robotConfig,
                               String keyword,
                               JobFilterState stateFilter) {
    }

    private record SelectionRequest(LarkRobotConfig robotConfig,
                                    List<String> bindJobFullNames,
                                    List<String> unbindJobFullNames) {
    }

    /**
     * JSON payload returned by the job binding search endpoint.
     */
    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class BindingDataset {
        private final String robotId;
        private final String robotName;
        private final String providerDisplayName;
        private final Summary summary;
        private final List<JobBindingRow> jobs;
        private final String stateFilter;
        private final String keyword;
        private final int filteredCount;

    }

    /**
     * Aggregated binding counts for the selected robot.
     */
    @Getter
    public static final class Summary {
        private int totalJobCount;
        private int boundJobCount;
        private int unboundJobCount;
        private int disabledJobCount;
        private int actionableJobCount;
        private int skippedJobCount;

        private static Summary from(List<JobBindingRow> rows) {
            Summary summary = new Summary();
            summary.totalJobCount = rows.size();
            for (JobBindingRow row : rows) {
                switch (row.getState()) {
                    case "bound":
                        summary.boundJobCount++;
                        break;
                    case "disabled":
                        summary.disabledJobCount++;
                        break;
                    case "unbound":
                        summary.unboundJobCount++;
                        break;
                    default:
                        summary.skippedJobCount++;
                        break;
                }
                if (row.isActionable()) {
                    summary.actionableJobCount++;
                }
            }
            return summary;
        }

    }

    /**
     * One job row rendered by the management page.
     */
    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class JobBindingRow {
        private final String jobFullName;
        private final String jobDisplayName;
        private final String jobDescription;
        private final String jobType;
        private final String state;
        private final boolean currentlySelected;
        private final boolean actionable;
        private final String reason;

    }

    /**
     * Result summary returned after applying pending binding changes.
     */
    @Getter
    public static final class SelectionApplyReport {
        private final String robotId;
        private final String robotName;
        private final List<SelectionResult> entries = new ArrayList<>();
        private int changedJobCount;
        private int skippedJobCount;
        private int failedJobCount;

        private SelectionApplyReport(String robotId, String robotName) {
            this.robotId = robotId;
            this.robotName = robotName;
        }

        private void record(SelectionResult result) {
            entries.add(result);
            switch (result.getStatus()) {
                case "changed":
                    changedJobCount++;
                    break;
                case "failed":
                    failedJobCount++;
                    break;
                default:
                    skippedJobCount++;
                    break;
            }
        }

    }

    /**
     * Per-job result entry for one binding operation.
     */
    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class SelectionResult {
        private final String jobFullName;
        private final String action;
        private final String status;
        private final String message;

        private static SelectionResult changed(String jobFullName, SelectionAction action, String message) {
            return new SelectionResult(jobFullName, action.name().toLowerCase(Locale.ENGLISH), "changed", message);
        }

        private static SelectionResult skipped(String jobFullName, SelectionAction action, String message) {
            return new SelectionResult(jobFullName, action.name().toLowerCase(Locale.ENGLISH), "skipped", message);
        }

        private static SelectionResult failed(String jobFullName, SelectionAction action, String message) {
            return new SelectionResult(jobFullName, action.name().toLowerCase(Locale.ENGLISH), "failed", message);
        }

    }
}
