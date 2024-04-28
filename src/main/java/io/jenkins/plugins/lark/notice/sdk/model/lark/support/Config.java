package io.jenkins.plugins.lark.notice.sdk.model.lark.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Config
 *
 * @author xm.z
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Config implements Serializable {

    @JsonProperty(value = "wide_screen_mode")
    private boolean wideScreenMode;

    @JsonProperty(value = "enable_forward")
    private boolean enableForward;

}
