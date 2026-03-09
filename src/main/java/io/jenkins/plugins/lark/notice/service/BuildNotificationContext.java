package io.jenkins.plugins.lark.notice.service;

import hudson.EnvVars;
import io.jenkins.plugins.lark.notice.model.BuildJobModel;
import io.jenkins.plugins.lark.notice.model.RunUser;

/**
 * Runtime context for one build notification flow.
 *
 * @param executor build executor information
 * @param model    build metadata model used for message rendering
 * @param envVars  resolved build environment variables
 * @author xm.z
 */
public record BuildNotificationContext(RunUser executor, BuildJobModel model, EnvVars envVars) {
}
