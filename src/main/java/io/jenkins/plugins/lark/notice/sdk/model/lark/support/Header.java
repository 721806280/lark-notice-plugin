package io.jenkins.plugins.lark.notice.sdk.model.lark.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Header
 *
 * @author xm.z
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Header implements Serializable {

    private String template;

    private TagContent title;

}

