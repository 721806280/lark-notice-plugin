package io.jenkins.plugins.lark.notice.config;

import hudson.model.FreeStyleProject;
import hudson.scm.NullSCM;
import io.jenkins.plugins.lark.notice.config.property.LarkJobProperty;
import io.jenkins.plugins.lark.notice.config.property.LarkBranchJobProperty;
import jenkins.branch.BranchProperty;
import jenkins.branch.BranchSource;
import jenkins.branch.DefaultBranchPropertyStrategy;
import jenkins.scm.impl.SingleSCMSource;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.htmlunit.html.HtmlPage;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        JenkinsRule.WebClient webClient = jenkins.createWebClient();
        webClient.getOptions().setJavaScriptEnabled(false);

        HtmlPage configurePage = webClient.goTo("configure");
        String configureHtml = configurePage.getWebResponse().getContentAsString();
        assertEquals(1, countMatches(configureHtml, "/plugin/lark-notice/styles/configuration.css"));
        assertEquals(1, countMatches(configureHtml, "/plugin/lark-notice/scripts/robot-config-validator.js"));
        assertTrue(configureHtml.contains("name=\"robotConfigs\""));
        assertTrue(configureHtml.contains("id=\"proxyConfigContainer\""));
        assertTrue(configureHtml.contains("data-validate-button-method=\"test\""));

        HtmlPage managementPage = webClient.goTo("manage/lark");
        String managementHtml = managementPage.getWebResponse().getContentAsString();
        assertEquals(1, countMatches(managementHtml, "/plugin/lark-notice/styles/configuration.css"));
        assertEquals(1, countMatches(managementHtml, "/plugin/lark-notice/scripts/robot-config-validator.js"));
        assertEquals(1, countMatches(managementHtml, "/plugin/lark-notice/scripts/management-config-tools.js"));
        assertTrue(managementHtml.contains("name=\"larkManagementLinkForm\""));
        assertTrue(managementHtml.contains("name=\"larkManagementImportForm\""));
        assertTrue(managementHtml.contains("Replace all settings"));
        assertTrue(managementHtml.contains("lark-config-preview-btn"));
        assertTrue(managementHtml.contains("No-secrets exports are for review or sharing only and cannot be imported."));
        assertTrue(managementHtml.contains("name=\"robotConfigs\""));
        assertTrue(managementHtml.contains("id=\"proxyConfigContainer\""));
        assertTrue(managementHtml.contains("data-validate-button-method=\"test\""));
    }

    @Test
    public void globalPagesShouldLoadWhenJavaScriptIsEnabled() throws Exception {
        JenkinsRule.WebClient webClient = jenkins.createWebClient();

        HtmlPage configurePage = webClient.goTo("configure");
        assertTrue(configurePage.getWebResponse().getContentAsString().contains("name=\"robotConfigs\""));

        HtmlPage managementPage = webClient.goTo("manage/lark");
        assertTrue(managementPage.getWebResponse().getContentAsString().contains("name=\"larkManagementLinkForm\""));
    }

    @Test
    public void sharedConfigFragmentsShouldResolveChineseLocalization() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("ui-i18n");
        project.addProperty(new LarkJobProperty(null));
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.SIMPLIFIED_CHINESE);

            JenkinsRule.WebClient webClient = jenkins.createWebClient();
            webClient.getOptions().setJavaScriptEnabled(false);

            HtmlPage globalPage = webClient.goTo("configure");
            String globalHtml = globalPage.getWebResponse().getContentAsString();
            assertTrue(globalHtml.contains("\u901A\u77E5\u89E6\u53D1\u65F6\u673A"));
            assertTrue(globalHtml.contains("\u673A\u5668\u4EBA"));

            HtmlPage managementPage = webClient.goTo("manage/lark");
            String managementHtml = managementPage.getWebResponse().getContentAsString();
            assertTrue(managementPage.getTitleText().contains("Lark \u673A\u5668\u4EBA\u914D\u7F6E"));
            assertTrue(managementHtml.contains("\u901A\u77E5\u89E6\u53D1\u65F6\u673A"));
            assertTrue(managementHtml.contains("\u673A\u5668\u4EBA"));

            HtmlPage jobConfigurePage = webClient.getPage(project, "configure");
            String jobHtml = jobConfigurePage.getWebResponse().getContentAsString();
            assertTrue(jobHtml.contains("Lark \u673A\u5668\u4EBA\u914D\u7F6E"));
            assertTrue(jobHtml.contains("\u6DFB\u52A0\u673A\u5668\u4EBA"));
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    public void freestyleConfigPageShouldRenderNotifierAssetsOnce() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("ui-freestyle");
        project.addProperty(new LarkJobProperty(null));

        JenkinsRule.WebClient webClient = jenkins.createWebClient();
        webClient.getOptions().setJavaScriptEnabled(false);

        HtmlPage configurePage = webClient.getPage(project, "configure");
        String html = configurePage.getWebResponse().getContentAsString();

        assertEquals(1, countMatches(html, "/plugin/lark-notice/scripts/notifier-config.js"));
        assertEquals(1, countMatches(html, "/plugin/lark-notice/styles/configuration.css"));
        assertTrue(html.contains("name=\"notifierConfigs\""));
        assertTrue(html.contains("io.jenkins.plugins.lark.notice.config.LarkNotifierConfig"));
    }

    @Test
    public void freestylePublisherConfigPageShouldRenderNotifierAssetsOnce() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("ui-publisher");
        project.getPublishersList().add(new LarkNotifier(null));

        JenkinsRule.WebClient webClient = jenkins.createWebClient();
        webClient.getOptions().setJavaScriptEnabled(false);

        HtmlPage configurePage = webClient.getPage(project, "configure");
        String html = configurePage.getWebResponse().getContentAsString();

        assertEquals(1, countMatches(html, "/plugin/lark-notice/scripts/notifier-config.js"));
        assertEquals(1, countMatches(html, "/plugin/lark-notice/styles/configuration.css"));
        assertTrue(html.contains("descriptorId=\"io.jenkins.plugins.lark.notice.config.LarkNotifier\""));
        assertTrue(html.contains("name=\"notifierConfigs\""));
    }

    @Test
    public void freestyleConfigPageShouldDeduplicateNotifierAssetsAcrossPropertyAndPublisher() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("ui-combined");
        project.addProperty(new LarkJobProperty(null));
        project.getPublishersList().add(new LarkNotifier(null));

        JenkinsRule.WebClient webClient = jenkins.createWebClient();
        webClient.getOptions().setJavaScriptEnabled(false);

        HtmlPage configurePage = webClient.getPage(project, "configure");
        String html = configurePage.getWebResponse().getContentAsString();

        assertEquals(1, countMatches(html, "/plugin/lark-notice/scripts/notifier-config.js"));
        assertEquals(1, countMatches(html, "/plugin/lark-notice/styles/configuration.css"));
        assertEquals(2, countMatches(html, "class=\"jenkins-form-item hetero-list-container one-each\""));
    }

    @Test
    public void multibranchConfigPageShouldRenderBranchPropertyNotifierAssetsOnce() throws Exception {
        WorkflowMultiBranchProject project = jenkins.createProject(WorkflowMultiBranchProject.class, "ui-multibranch");
        project.getSourcesList().add(new BranchSource(
                new SingleSCMSource("main", new NullSCM()),
                new DefaultBranchPropertyStrategy(new BranchProperty[]{new LarkBranchJobProperty(null)})
        ));

        JenkinsRule.WebClient webClient = jenkins.createWebClient();
        webClient.getOptions().setJavaScriptEnabled(false);

        HtmlPage configurePage = webClient.getPage(project, "configure");
        String html = configurePage.getWebResponse().getContentAsString();

        assertEquals(1, countMatches(html, "/plugin/lark-notice/scripts/notifier-config.js"));
        assertEquals(1, countMatches(html, "/plugin/lark-notice/styles/configuration.css"));
        assertTrue(html.contains("name=\"notifierConfigs\""));
        assertTrue(html.contains("io.jenkins.plugins.lark.notice.config.property.LarkBranchJobProperty"));
    }

    private static LarkRobotConfig createRobot(String id) {
        return new LarkRobotConfig(
                id,
                "Robot-" + id,
                "https://open.feishu.cn/open-apis/bot/v2/hook/" + id,
                List.of()
        );
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
