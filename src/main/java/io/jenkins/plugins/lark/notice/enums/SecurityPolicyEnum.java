package io.jenkins.plugins.lark.notice.enums;

import io.jenkins.plugins.lark.notice.Messages;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Enum representing the types of security policies that can be applied within a system.
 * This enum is used to distinguish between different security mechanisms, such as key-based or secret-based authentication or encryption.
 *
 * @author xm.z
 */
@Getter
@ToString
@AllArgsConstructor
public enum SecurityPolicyEnum {

    /**
     * Represents a security policy based on keys. This can be used for mechanisms where
     * public/private keys or similar key-based strategies are employed for authentication or encryption.
     */
    KEY(Messages.security_policy_type_key()),

    /**
     * Represents a security policy based on secrets. This can be employed in contexts where
     * secret tokens, passwords, or similar confidential information is used for secure access or data protection.
     */
    SECRET(Messages.security_policy_type_secret());

    /**
     * A descriptive text explaining the security policy type.
     */
    private final String desc;

}