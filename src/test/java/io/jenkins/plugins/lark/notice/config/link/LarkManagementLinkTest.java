package io.jenkins.plugins.lark.notice.config.link;

import io.jenkins.plugins.lark.notice.config.LarkRetryConfig;
import io.jenkins.plugins.lark.notice.config.LarkRobotConfig;
import io.jenkins.plugins.lark.notice.config.LarkSecurityPolicyConfig;
import io.jenkins.plugins.lark.notice.config.snapshot.LarkConfigSnapshotMapper;
import io.jenkins.plugins.lark.notice.tools.JsonUtils;
import net.sf.json.JSONObject;
import org.htmlunit.HttpMethod;
import org.htmlunit.Page;
import org.htmlunit.WebRequest;
import org.htmlunit.util.NameValuePair;
import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import io.jenkins.plugins.lark.notice.config.security.LarkPermissions;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the Lark management link metadata and bindings.
 *
 * @author xm.z
 */
public class LarkManagementLinkTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void managementLinkShouldExposeStableMetadata() {
        LarkManagementLink link = new LarkManagementLink();

        assertEquals("/plugin/lark-notice/images/logo.png", link.getIconFileName());
        assertEquals("lark", link.getUrlName());
        assertEquals(LarkManagementLink.Category.UNCATEGORIZED, link.getCategory());
        assertSame(LarkPermissions.CONFIGURE, link.getRequiredPermission());
    }

    @Test
    public void managementLinkShouldResolveGlobalConfigSingleton() {
        LarkManagementLink link = new LarkManagementLink();

        assertSame(LarkGlobalConfig.getInstance(), link.getGlobalConfig());
    }

    @Test
    public void exportEndpointShouldReturnJsonSnapshot() throws Exception {
        LarkGlobalConfig.getInstance().setRobotConfigs(new ArrayList<>(List.of(createRobot("export-robot"))));

        JenkinsRule.WebClient webClient = jenkins.createWebClient();
        Page page = webClient.getPage(new URL(jenkins.getURL(), "manage/lark/export?includeSecrets=true"));

        String content = page.getWebResponse().getContentAsString();
        assertTrue(content.contains("\"schemaVersion\""));
        assertTrue(content.contains("\"webhook\""));
        assertTrue(content.contains("export-robot"));
        assertTrue(page.getWebResponse().getResponseHeaderValue("Content-Disposition").contains("lark-notice-config-"));
    }

    @Test
    public void importEndpointShouldReplaceCurrentConfiguration() throws Exception {
        LarkGlobalConfig.getInstance().setVerbose(false);
        LarkGlobalConfig.getInstance().setRobotConfigs(new ArrayList<>());

        String payload = JsonUtils.toJson(createImportPayload());
        JenkinsRule.WebClient webClient = jenkins.createWebClient();
        WebRequest request = new WebRequest(new URL(jenkins.getURL(), "manage/lark/import"), HttpMethod.POST);
        request.setRequestParameters(List.of(new NameValuePair("payload", payload)));
        webClient.addCrumb(request);

        Page page = webClient.getPage(request);
        JSONObject response = JSONObject.fromObject(page.getWebResponse().getContentAsString());

        assertTrue(response.getBoolean("ok"));
        assertTrue(LarkGlobalConfig.getInstance().isVerbose());
        assertEquals(1, LarkGlobalConfig.getInstance().getRobotConfigs().size());
        assertEquals("import-robot", LarkGlobalConfig.getInstance().getRobotConfigs().get(0).getId());
    }

    @Test
    public void previewImportEndpointShouldReturnSummaryForMergeMode() throws Exception {
        LarkGlobalConfig.getInstance().setRobotConfigs(new ArrayList<>(List.of(
                createRobot("existing-robot"),
                createRobot("import-robot")
        )));

        String payload = JsonUtils.toJson(LarkConfigSnapshotMapper.toSnapshot(
                new LarkGlobalConfig(
                        null,
                        true,
                        Set.of("SUCCESS"),
                        new ArrayList<>(List.of(createRobot("import-robot"), createRobot("new-robot")))
                ),
                true
        ));
        JenkinsRule.WebClient webClient = jenkins.createWebClient();
        WebRequest request = new WebRequest(new URL(jenkins.getURL(), "manage/lark/previewImport"), HttpMethod.POST);
        request.setRequestParameters(List.of(
                new NameValuePair("payload", payload),
                new NameValuePair("mode", "merge")
        ));
        webClient.addCrumb(request);

        Page page = webClient.getPage(request);
        JSONObject response = JSONObject.fromObject(page.getWebResponse().getContentAsString());
        JSONObject data = response.getJSONObject("data");

        assertTrue(response.getBoolean("ok"));
        assertEquals("merge", data.getString("mode"));
        assertEquals(2, data.getInt("currentRobotCount"));
        assertEquals(2, data.getInt("importedRobotCount"));
        assertEquals(1, data.getInt("addedRobotCount"));
        assertEquals(1, data.getInt("updatedRobotCount"));
        assertEquals(1, data.getInt("retainedRobotCount"));
        assertEquals(0, data.getInt("removedRobotCount"));
    }

    @Test
    public void importEndpointShouldMergeRobotsById() throws Exception {
        LarkRobotConfig existingRobot = createRobot("existing-robot");
        existingRobot.setName("Existing Robot");
        LarkGlobalConfig.getInstance().setVerbose(false);
        LarkGlobalConfig.getInstance().setRobotConfigs(new ArrayList<>(List.of(
                existingRobot,
                createRobot("shared-robot")
        )));

        String payload = JsonUtils.toJson(LarkConfigSnapshotMapper.toSnapshot(
                new LarkGlobalConfig(
                        null,
                        true,
                        Set.of("FAILURE"),
                        new ArrayList<>(List.of(createRobot("shared-robot"), createRobot("new-robot")))
                ),
                true
        ));
        JenkinsRule.WebClient webClient = jenkins.createWebClient();
        WebRequest request = new WebRequest(new URL(jenkins.getURL(), "manage/lark/import"), HttpMethod.POST);
        request.setRequestParameters(List.of(
                new NameValuePair("payload", payload),
                new NameValuePair("mode", "merge")
        ));
        webClient.addCrumb(request);

        Page page = webClient.getPage(request);
        JSONObject response = JSONObject.fromObject(page.getWebResponse().getContentAsString());

        assertTrue(response.getBoolean("ok"));
        assertTrue(LarkGlobalConfig.getInstance().isVerbose());
        assertEquals(Set.of("FAILURE"), LarkGlobalConfig.getInstance().getNoticeOccasions());
        assertEquals(3, LarkGlobalConfig.getInstance().getRobotConfigs().size());
        assertEquals("existing-robot", LarkGlobalConfig.getInstance().getRobotConfigs().get(0).getId());
        assertEquals("Existing Robot", LarkGlobalConfig.getInstance().getRobotConfigs().get(0).getName());
        assertEquals("shared-robot", LarkGlobalConfig.getInstance().getRobotConfigs().get(1).getId());
        assertEquals("new-robot", LarkGlobalConfig.getInstance().getRobotConfigs().get(2).getId());
    }

    private static io.jenkins.plugins.lark.notice.config.snapshot.LarkConfigSnapshot createImportPayload() {
        LarkGlobalConfig source = new LarkGlobalConfig(
                null,
                true,
                Set.of("SUCCESS"),
                new ArrayList<>(List.of(createRobot("import-robot")))
        );
        return io.jenkins.plugins.lark.notice.config.snapshot.LarkConfigSnapshotMapper.toSnapshot(source, true);
    }

    private static LarkRobotConfig createRobot(String id) {
        LarkRobotConfig robotConfig = new LarkRobotConfig(
                id,
                "Robot " + id,
                "https://open.feishu.cn/open-apis/bot/v2/hook/" + id,
                List.of(new LarkSecurityPolicyConfig("KEY", "keyword-" + id, "Keyword"))
        );
        robotConfig.setRetryConfig(new LarkRetryConfig(true, 3, 500, 5000, 2.0d, 0.2d));
        return robotConfig;
    }
}
