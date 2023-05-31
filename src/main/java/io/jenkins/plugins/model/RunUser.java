package io.jenkins.plugins.model;

import lombok.*;

/**
 * 当前执行任务的用户信息
 *
 * @author xm.z
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunUser {

    /**
     * 当前执行任务的用户名称
     */
    private String name;

    /**
     * 当前执行任务的用户配置的手机号
     */
    private String mobile;

}
