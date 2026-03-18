package io.jenkins.plugins.lark.notice.config;

import hudson.Extension;
import io.jenkins.plugins.lark.notice.Messages;
import jenkins.model.GlobalConfigurationCategory;

/**
 * Dedicated category for Lark Notice global configuration.
 *
 * @author xm.z
 */
@Extension
public class LarkGlobalConfigurationCategory extends GlobalConfigurationCategory {

    @Override
    public String getShortDescription() {
        return Messages.lark_global_category_description();
    }

    @Override
    public String getDisplayName() {
        return Messages.lark_global_category_display_name();
    }
}
