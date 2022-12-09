package io.jenkins.plugins.sdk.entity.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RichText
 *
 * @author xm.z
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RichText {

    @JsonProperty(value = "zh_cn")
    private Content zhCn;

}