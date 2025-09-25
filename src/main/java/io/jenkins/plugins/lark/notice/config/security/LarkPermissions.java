package io.jenkins.plugins.lark.notice.config.security;

import hudson.security.Permission;
import hudson.security.PermissionGroup;
import hudson.security.PermissionScope;
import io.jenkins.plugins.lark.notice.Messages;
import jenkins.model.Jenkins;

/**
 * Defines the permission group and individual permissions for the Lark Notice plugin.
 * <p>
 * This class declares:
 * <ul>
 *   <li>A permission group titled "Lark Notice", which groups all related permissions.</li>
 *   <li>A {@code configure} permission that allows users to manage Lark robot settings,
 *       test webhook connections, and adjust notification policies.</li>
 * </ul>
 * <p>
 * The {@link #CONFIGURE} permission is granted only to users with administrative or
 * system management privileges (it depends on {@link Jenkins#MANAGE}).
 *
 * @author xm.z
 */
public class LarkPermissions {

    /**
     * The permission group for all Lark Notice-related permissions.
     * This group appears in Jenkins' global security configuration UI
     * under the name defined by {@code Messages.permissions_group_title()}.
     */
    public static final PermissionGroup GROUP = new PermissionGroup(LarkPermissions.class, Messages._plugin_display_name());

    /**
     * Permission to configure, test, and manage Lark Notice settings and robot integrations.
     * <p>
     * Users with this permission can:
     * <ul>
     *   <li>View and edit Lark robot webhook URLs and security policies (e.g., keyword or signature verification),</li>
     *   <li>Test connectivity to Lark bots,</li>
     *   <li>Manage notification rules for build events (e.g., success, failure, unstable).</li>
     * </ul>
     * <p>
     * This permission is scoped to the Jenkins master and depends on {@link Jenkins#MANAGE},
     * meaning only users who can manage Jenkins system configuration can be granted this permission.
     */
    public static final Permission CONFIGURE = new Permission(GROUP, "Configure", Messages._permissions_description(), Jenkins.MANAGE, PermissionScope.JENKINS);

}