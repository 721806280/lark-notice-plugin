package io.jenkins.plugins.lark.notice.enums;

import io.jenkins.plugins.lark.notice.Messages;
import lombok.ToString;

/**
 * Enum representing the types of security policies that can be applied within a system.
 * This enum is used to distinguish between different security mechanisms, such as key-based or secret-based authentication or encryption.
 *
 * @author xm.z
 */
@ToString
public enum SecurityPolicyEnum {

    /**
     * Security policy with SSL verification disabled.
     */
    NO_SSL,

    /**
     * Represents a security policy based on keys. This can be used for mechanisms where
     * public/private keys or similar key-based strategies are employed for authentication or encryption.
     */
    KEY,

    /**
     * Represents a security policy based on secrets. This can be employed in contexts where
     * secret tokens, passwords, or similar confidential information is used for secure access or data protection.
     */
    SECRET;

    public String getDesc() {
        return switch (this) {
            case NO_SSL -> Messages.security_policy_type_no_ssl();
            case KEY -> Messages.security_policy_type_key();
            case SECRET -> Messages.security_policy_type_secret();
        };
    }

}
