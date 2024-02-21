package io.jenkins.plugins.lark.notice.sdk.model.lark.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Header
 *
 * @author xm.z
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Header {

    private String template;

    private TagContent title;

}

