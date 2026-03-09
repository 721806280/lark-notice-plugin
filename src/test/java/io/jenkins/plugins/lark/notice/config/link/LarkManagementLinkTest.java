package io.jenkins.plugins.lark.notice.config.link;

import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import io.jenkins.plugins.lark.notice.config.security.LarkPermissions;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

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
}
