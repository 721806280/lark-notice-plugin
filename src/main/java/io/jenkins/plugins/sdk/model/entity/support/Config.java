package io.jenkins.plugins.sdk.model.entity.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Config
 *
 * @author xm.z
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Config {

    @JsonProperty(value = "wide_screen_mode")
    private boolean wideScreenMode;

    @JsonProperty(value = "enable_forward")
    private boolean enableForward;

}
