package io.jenkins.plugins.model;

import lombok.*;

/**
 * RunUser
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
     * <p>当前执行人名称</p>
     */
    private String name;

    /**
     * <p>当前执行人配置的手机号</p>
     */
    private String mobile;
}
