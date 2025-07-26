package io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.person;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 人员列表中的单个人员项
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonSimpleElement {

    /**
     * 人员 ID，支持 open_id、user_id、union_id
     */
    private String id;

    public static PersonSimpleElement of(String id) {
        PersonSimpleElement personSimpleElement = new PersonSimpleElement();
        personSimpleElement.setId(id);
        return personSimpleElement;
    }
}
