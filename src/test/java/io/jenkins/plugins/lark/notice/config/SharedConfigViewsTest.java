package io.jenkins.plugins.lark.notice.config;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Verifies that shared Jelly fragments are packaged next to {@link SharedConfigViews}
 * so Stapler can resolve them from the HPI.
 */
public class SharedConfigViewsTest {

    @Test
    public void sharedJellyFragmentsShouldBeAvailableFromMarkerClass() {
        assertNotNull(SharedConfigViews.class.getResource("/io/jenkins/plugins/lark/notice/config/global-config-form.jelly"));
        assertNotNull(SharedConfigViews.class.getResource("/io/jenkins/plugins/lark/notice/config/global-config-sections.jelly"));
        assertNotNull(SharedConfigViews.class.getResource("/io/jenkins/plugins/lark/notice/config/notifier-config-section.jelly"));
        assertNotNull(SharedConfigViews.class.getResource("/io/jenkins/plugins/lark/notice/config/notice-occasion-options.jelly"));
    }
}
