package io.jenkins.plugins.lark.notice.sdk.model.lark.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * RichText
 *
 * @author xm.z
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RichText implements Serializable {

    @JsonProperty(value = "zh_cn")
    private Content zhCn;

}