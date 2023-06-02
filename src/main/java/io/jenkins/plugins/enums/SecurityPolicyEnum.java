package io.jenkins.plugins.enums;

import io.jenkins.plugins.Messages;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 安全策略
 *
 * @author xm.z
 */
@Getter
@ToString
@AllArgsConstructor
public enum SecurityPolicyEnum {

    /**
     * 关键字
     */
    KEY(Messages.security_policy_type_key()),

    /**
     * 加签
     */
    SECRET(Messages.security_policy_type_secret());

    private final String desc;

}