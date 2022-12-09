package io.jenkins.plugins.sdk.entity;

import io.jenkins.plugins.sdk.entity.support.Card;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 卡片消息 类型
 *
 * @author xm.z
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionCard {

    /**
     * 卡片消息 类型
     */
    private Card card;

}
