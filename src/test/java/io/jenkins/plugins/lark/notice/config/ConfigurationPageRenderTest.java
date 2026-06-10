package io.jenkins.plugins.lark.notice.config;

import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.scm.NullSCM;
import io.jenkins.plugins.lark.notice.config.property.LarkJobProperty;
import io.jenkins.plugins.lark.notice.config.property.LarkBranchJobProperty;
import jenkins.branch.BranchProperty;
import jenkins.branch.BranchSource;
import jenkins.branch.DefaultBranchPropertyStrategy;
import jenkins.scm.impl.SingleSCMSource;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.Page;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.util.NameValuePair;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Rendering tests for the plugin configuration pages.
 *
 * @author xm.z
 */
public class ConfigurationPageRenderTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Before
    public void setUp() {
        LarkGlobalConfig.getInstance().setRobotConfigs(new ArrayList<>(List.of(
                createRobot("robot-a"),
                createRobot("robot-b")
        )));
    }

    @Test
    public void globalAndManagementPagesShouldRenderSharedAssets() throws Exception {
        try (JenkinsRule.WebClient webClient = jenkins.createWebClient()) {
            webClient.getOptions().setJavaScriptEnabled(false);

            HtmlPage configurePage = webClient.goTo("configure");
            String configureHtml = configurePage.getWebResponse().getContentAsString();
            assertEquals(0, countMatches(configureHtml, "/plugin/lark-notice/styles/configuration.css"));
            assertEquals(0, countMatches(configureHtml, "/plugin/lark-notice/scripts/robot-config-validator.js"));
            assertFalse(configureHtml.contains("name=\"robotConfigs\""));
            assertFalse(configureHtml.contains("id=\"proxyConfigContainer\""));
            assertFalse(configureHtml.contains("data-validate-button-method=\"test\""));

            HtmlPage managementPage = webClient.goTo("manage/lark");
            String managementHtml = managementPage.getWebResponse().getContentAsString();
            assertEquals(1, countMatches(managementHtml, "/plugin/lark-notice/styles/configuration.css"));
            assertEquals(1, countMatches(managementHtml, "/plugin/lark-notice/scripts/robot-config-validator.js"));
            assertTrue(managementHtml.contains("name=\"larkManagementLinkForm\""));
            assertEquals(0, countMatches(managementHtml, "/plugin/lark-notice/scripts/management-config-tools.js"));
            assertEquals(0, countMatches(managementHtml, "name=\"larkManagementImportForm\""));
            assertTrue(managementHtml.contains("Open Migration Tools"));
            assertTrue(managementHtml.contains("Advanced Settings"));
            assertTrue(managementHtml.contains("name=\"robotConfigs\""));
            assertTrue(managementHtml.contains("id=\"proxyConfigContainer\""));
            assertTrue(managementHtml.contains("class=\"lark-proxy-combo\""));
            assertTrue(managementHtml.contains("class=\"jenkins-select lark-proxy-type-select\""));
            assertTrue(managementHtml.contains("SOCKS"));
            assertTrue(managementHtml.contains("placeholder=\"Server address (for example: proxy.com)\""));
            assertTrue(managementHtml.contains("data-validate-button-method=\"test\""));

            HtmlPage toolsPage = webClient.goTo("manage/lark/tools");
            String toolsHtml = toolsPage.getWebResponse().getContentAsString();
            assertEquals(1, countMatches(toolsHtml, "/plugin/lark-notice/styles/configuration.css"));
            assertEquals(1, countMatches(toolsHtml, "/plugin/lark-notice/scripts/management-config-tools.js"));
            assertTrue(toolsHtml.contains("name=\"larkManagementImportForm\""));
            assertTrue(toolsHtml.contains("lark-config-preview-btn"));

            HtmlPage jobsPage = webClient.goTo("manage/lark/jobs");
            String jobsHtml = jobsPage.getWebResponse().getContentAsString();
            assertEquals(1, countMatches(jobsHtml, "/plugin/lark-notice/scripts/management-job-binding.js"));
            assertEquals(1, countMatches(jobsHtml, "/plugin/lark-notice/styles/management-job-binding.css"));
            assertTrue(jobsHtml.contains("Robot Job Bindings"));
            assertTrue(jobsHtml.contains("Select a Saved Robot"));
            assertTrue(jobsHtml.contains("/manage/lark/jobs?robotId=robot-a"));

            HtmlPage robotJobsPage = webClient.goTo("manage/lark/jobs?robotId=robot-a");
            String robotJobsHtml = robotJobsPage.getWebResponse().getContentAsString();
            assertTrue(robotJobsHtml.contains("lark-robot-job-page"));
            assertTrue(robotJobsHtml.contains("value=\"all\" selected=\"selected\""));
            assertTrue(robotJobsHtml.contains("data-summary-disabled"));
        }
    }

    @Test
    public void globalPagesShouldLoadWhenJavaScriptIsEnabled() throws Exception {
        try (JenkinsRule.WebClient webClient = jenkins.createWebClient()) {

            HtmlPage configurePage = webClient.goTo("configure");
            assertFalse(configurePage.getWebResponse().getContentAsString().contains("name=\"robotConfigs\""));

            HtmlPage managementPage = webClient.goTo("manage/lark");
            assertTrue(managementPage.getWebResponse().getContentAsString().contains("name=\"larkManagementLinkForm\""));

            HtmlPage toolsPage = webClient.goTo("manage/lark/tools");
            assertTrue(toolsPage.getWebResponse().getContentAsString().contains("name=\"larkManagementImportForm\""));

            HtmlPage robotJobsPage = webClient.goTo("manage/lark/jobs?robotId=robot-a");
            assertTrue(robotJobsPage.getWebResponse().getContentAsString().contains("lark-robot-job-page"));
        }
    }

    @Test
    public void sharedConfigFragmentsShouldResolveChineseLocalization() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("ui-i18n");
        project.addProperty(new LarkJobProperty(null));
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.SIMPLIFIED_CHINESE);

            try (JenkinsRule.WebClient webClient = jenkins.createWebClient()) {
                webClient.getOptions().setJavaScriptEnabled(false);

                HtmlPage globalPage = webClient.goTo("configure");
                String globalHtml = globalPage.getWebResponse().getContentAsString();
                assertFalse(globalHtml.contains("通知时机"));
                assertFalse(globalHtml.contains("name=\"robotConfigs\""));

                HtmlPage managementPage = webClient.goTo("manage/lark");
                String managementHtml = managementPage.getWebResponse().getContentAsString();
                assertTrue(managementPage.getTitleText().contains("Lark 机器人配置"));
                assertTrue(managementHtml.contains("通知时机"));
                assertTrue(managementHtml.contains("高级设置"));
                assertTrue(managementHtml.contains("aria-label=\"代理配置：\""));
                assertTrue(managementHtml.contains("placeholder=\"服务器地址（例如: proxy.com）\""));
                assertTrue(managementHtml.contains("机器人"));

                HtmlPage toolsPage = webClient.goTo("manage/lark/tools");
                String toolsHtml = toolsPage.getWebResponse().getContentAsString();
                assertTrue(toolsPage.getTitleText().contains("Lark 配置迁移"));
                assertTrue(toolsHtml.contains("加载数据"));
                assertTrue(toolsHtml.contains("加载当前配置"));

                HtmlPage jobsPage = webClient.goTo("manage/lark/jobs?robotId=robot-a");
                String jobsHtml = jobsPage.getWebResponse().getContentAsString();
                assertTrue(jobsPage.getTitleText().contains("机器人 Job 绑定"));
                assertTrue(jobsHtml.contains("当前机器人"));
                assertTrue(jobsHtml.contains("已禁用"));

                HtmlPage jobConfigurePage = webClient.getPage(project, "configure");
                String jobHtml = jobConfigurePage.getWebResponse().getContentAsString();
                assertTrue(jobHtml.contains("Lark 机器人配置"));
                assertTrue(jobHtml.contains("添加机器人"));
                assertTrue(jobHtml.contains("name=\"notifierConfigs\""));
            }
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    public void freestyleConfigPageShouldRenderNotifierAssetsOnce() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("ui-freestyle");
        project.addProperty(new LarkJobProperty(null));

        try (JenkinsRule.WebClient webClient = jenkins.createWebClient()) {
            webClient.getOptions().setJavaScriptEnabled(false);

            HtmlPage configurePage = webClient.getPage(project, "configure");
            String html = configurePage.getWebResponse().getContentAsString();

            assertEquals(1, countMatches(html, "/plugin/lark-notice/scripts/notifier-config.js"));
            assertEquals(1, countMatches(html, "/plugin/lark-notice/scripts/request-utils.js"));
            assertEquals(1, countMatches(html, "/plugin/lark-notice/scripts/ui-utils.js"));
            assertEquals(1, countMatches(html, "/plugin/lark-notice/styles/configuration.css"));
            assertTrue(html.contains("name=\"notifierConfigs\""));
            assertTrue(html.contains("io.jenkins.plugins.lark.notice.config.LarkNotifierConfig"));
        }
    }

    @Test
    public void jobConfigPageShouldPreserveCustomMessageModeOnReload() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("ui-custom-message-mode");
        LarkNotifierConfig notifierConfig = new LarkNotifierConfig(
                true,
                false,
                true,
                "robot-a",
                "Robot-robot-a",
                false,
                "",
                "custom-title",
                "default-content",
                "custom-message",
                Set.of("START")
        );
        project.addProperty(new LarkJobProperty(List.of(notifierConfig)));

        try (JenkinsRule.WebClient webClient = jenkins.createWebClient()) {
            webClient.getOptions().setJavaScriptEnabled(false);

            HtmlPage configurePage = webClient.getPage(project, "configure");
            String html = configurePage.getWebResponse().getContentAsString();

            HtmlInput rawInput = configurePage.getFirstByXPath("//input[@name='_.raw']");
            HtmlInput robotIdInput = configurePage.getFirstByXPath("//input[@name='_.robotId']");
            assertNotNull(rawInput);
            assertNotNull(robotIdInput);
            assertEquals("true", rawInput.getValueAttribute());
            assertEquals("robot-a", robotIdInput.getValueAttribute());
            assertTrue(html.contains("custom-message"));
        }
    }

    @Test
    public void jobConfigPageShouldPreserveAtAllOnReload() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("ui-at-all");
        LarkNotifierConfig notifierConfig = new LarkNotifierConfig(
                false,
                false,
                true,
                "robot-a",
                "Robot-robot-a",
                true,
                "",
                "title",
                "content",
                "",
                Set.of("START")
        );
        project.addProperty(new LarkJobProperty(List.of(notifierConfig)));

        try (JenkinsRule.WebClient webClient = jenkins.createWebClient()) {
            webClient.getOptions().setJavaScriptEnabled(false);

            HtmlPage configurePage = webClient.getPage(project, "configure");

            HtmlInput atAllInput = configurePage.getFirstByXPath("//input[@name='_.atAll' and @type='checkbox']");
            assertNotNull(atAllInput);
            assertTrue(atAllInput.isChecked());
        }
    }

    @Test
    public void freestylePublisherConfigPageShouldRenderNotifierAssetsOnce() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("ui-publisher");
        project.getPublishersList().add(new LarkNotifier(null));

        try (JenkinsRule.WebClient webClient = jenkins.createWebClient()) {
            webClient.getOptions().setJavaScriptEnabled(false);

            HtmlPage configurePage = webClient.getPage(project, "configure");
            String html = configurePage.getWebResponse().getContentAsString();

            assertEquals(1, countMatches(html, "/plugin/lark-notice/scripts/notifier-config.js"));
            assertEquals(1, countMatches(html, "/plugin/lark-notice/scripts/request-utils.js"));
            assertEquals(1, countMatches(html, "/plugin/lark-notice/scripts/ui-utils.js"));
            assertEquals(1, countMatches(html, "/plugin/lark-notice/styles/configuration.css"));
            assertTrue(html.contains("descriptorId=\"io.jenkins.plugins.lark.notice.config.LarkNotifier\""));
            assertTrue(html.contains("name=\"notifierConfigs\""));
        }
    }

    @Test
    public void freestyleConfigPageShouldDeduplicateNotifierAssetsAcrossPropertyAndPublisher() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("ui-combined");
        project.addProperty(new LarkJobProperty(null));
        project.getPublishersList().add(new LarkNotifier(null));

        try (JenkinsRule.WebClient webClient = jenkins.createWebClient()) {
            webClient.getOptions().setJavaScriptEnabled(false);

            HtmlPage configurePage = webClient.getPage(project, "configure");
            String html = configurePage.getWebResponse().getContentAsString();

            assertEquals(1, countMatches(html, "/plugin/lark-notice/scripts/notifier-config.js"));
            assertEquals(1, countMatches(html, "/plugin/lark-notice/scripts/request-utils.js"));
            assertEquals(1, countMatches(html, "/plugin/lark-notice/scripts/ui-utils.js"));
            assertEquals(1, countMatches(html, "/plugin/lark-notice/styles/configuration.css"));
            assertEquals(2, countMatches(html, "class=\"jenkins-form-item hetero-list-container one-each\""));
        }
    }

    @Test
    public void multibranchConfigPageShouldRenderBranchPropertyNotifierAssetsOnce() throws Exception {
        WorkflowMultiBranchProject project = jenkins.createProject(WorkflowMultiBranchProject.class, "ui-multibranch");
        BranchSource branchSource = new BranchSource(new SingleSCMSource("main", new NullSCM()));
        branchSource.setStrategy(new DefaultBranchPropertyStrategy(new BranchProperty[]{new LarkBranchJobProperty(null)}));
        project.getSourcesList().add(branchSource);

        try (JenkinsRule.WebClient webClient = jenkins.createWebClient()) {
            webClient.getOptions().setJavaScriptEnabled(false);

            HtmlPage configurePage = webClient.getPage(project, "configure");
            String html = configurePage.getWebResponse().getContentAsString();

            assertEquals(1, countMatches(html, "/plugin/lark-notice/scripts/notifier-config.js"));
            assertEquals(1, countMatches(html, "/plugin/lark-notice/scripts/request-utils.js"));
            assertEquals(1, countMatches(html, "/plugin/lark-notice/scripts/ui-utils.js"));
            assertEquals(1, countMatches(html, "/plugin/lark-notice/styles/configuration.css"));
            assertTrue(html.contains("name=\"notifierConfigs\""));
            assertTrue(html.contains("io.jenkins.plugins.lark.notice.config.property.LarkBranchJobProperty"));
        }
    }

    @Test
    public void newJobPageShouldCreateFreestyleProjectAndExposeNotifierConfig() throws Exception {
        createJobFromNewJobPage("new-ui-freestyle", "hudson.model.FreeStyleProject");

        FreeStyleProject project = jenkins.jenkins.getItemByFullName("new-ui-freestyle", FreeStyleProject.class);
        assertNotNull("Expected freestyle project to be created.", project);
        project.addProperty(new LarkJobProperty(null));
        project.getPublishersList().add(new LarkNotifier(null));
        try (JenkinsRule.WebClient webClient = jenkins.createWebClient()) {
            webClient.getOptions().setJavaScriptEnabled(false);
            HtmlPage configurePage = webClient.getPage(project, "configure");
            String html = configurePage.getWebResponse().getContentAsString();

            assertEquals(1, countMatches(html, "/plugin/lark-notice/scripts/notifier-config.js"));
            assertTrue(html.contains("name=\"notifierConfigs\""));
        }
    }

    @Test
    public void newJobPageShouldCreatePipelineProjectAndExposeJobPropertyConfig() throws Exception {
        createJobFromNewJobPage("new-ui-pipeline", "org.jenkinsci.plugins.workflow.job.WorkflowJob");

        Job<?, ?> pipelineJob = jenkins.jenkins.getItemByFullName("new-ui-pipeline", Job.class);
        assertNotNull("Expected pipeline job to be created.", pipelineJob);
        pipelineJob.addProperty(new LarkJobProperty(null));

        try (JenkinsRule.WebClient webClient = jenkins.createWebClient()) {
            webClient.getOptions().setJavaScriptEnabled(false);
            HtmlPage configurePage = webClient.goTo("job/new-ui-pipeline/configure");
            String html = configurePage.getWebResponse().getContentAsString();

            assertEquals(1, countMatches(html, "/plugin/lark-notice/scripts/notifier-config.js"));
            assertTrue(html.contains("name=\"notifierConfigs\""));
        }
    }

    @Test
    public void newJobPageShouldCreateMultibranchProjectAndExposeBranchPropertyConfig() throws Exception {
        createJobFromNewJobPage("new-ui-multibranch", "org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject");

        WorkflowMultiBranchProject project =
                jenkins.jenkins.getItemByFullName("new-ui-multibranch", WorkflowMultiBranchProject.class);
        assertNotNull("Expected multibranch project to be created.", project);
        BranchSource branchSource = new BranchSource(new SingleSCMSource("main", new NullSCM()));
        branchSource.setStrategy(new DefaultBranchPropertyStrategy(new BranchProperty[]{new LarkBranchJobProperty(null)}));
        project.getSourcesList().add(branchSource);

        try (JenkinsRule.WebClient webClient = jenkins.createWebClient()) {
            webClient.getOptions().setJavaScriptEnabled(false);
            HtmlPage configurePage = webClient.getPage(project, "configure");
            String html = configurePage.getWebResponse().getContentAsString();

            assertTrue(html.contains("io.jenkins.plugins.lark.notice.config.property.LarkBranchJobProperty"));
        }
    }

    private static LarkRobotConfig createRobot(String id) {
        return new LarkRobotConfig(
                id,
                "Robot-" + id,
                "https://open.feishu.cn/open-apis/bot/v2/hook/" + id,
                List.of()
        );
    }

    private void createJobFromNewJobPage(String jobName, String jobTypeId) throws Exception {
        try (JenkinsRule.WebClient webClient = jenkins.createWebClient()) {
            webClient.getOptions().setJavaScriptEnabled(false);

            HtmlPage newJobPage = webClient.goTo("view/all/newJob");
            HtmlForm form = newJobPage.getFormByName("createItem");

            org.htmlunit.WebRequest request = form.getWebRequest(null);
            List<NameValuePair> params = new ArrayList<>(request.getRequestParameters());
            params.removeIf(pair -> "name".equals(pair.getName()) || "mode".equals(pair.getName()));
            params.add(new NameValuePair("name", jobName));
            params.add(new NameValuePair("mode", jobTypeId));
            request.setRequestParameters(params);
            Page response = webClient.getPage(webClient.addCrumb(request));
            if (!(response instanceof HtmlPage)) {
                webClient.goTo("job/" + jobName + "/configure");
            }
        }
    }

    private static int countMatches(String text, String needle) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(needle, index)) >= 0) {
            count++;
            index += needle.length();
        }
        return count;
    }
}
