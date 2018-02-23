package com.kairos.persistence.model.user.position_code;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.persistence.model.common.UserBaseEntity;
import org.hibernate.validator.constraints.NotEmpty;
import org.neo4j.ogm.annotation.NodeEntity;

import javax.validation.constraints.NotNull;

/**
 * Created by pawanmandhan on 27/7/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NodeEntity

public class PositionCode extends UserBaseEntity {


    @NotEmpty(message = "error.PositionCode.name.notempty")
    @NotNull(message = "error.position_code.name.notnull")

    private String name;

    private String description;

    private String timeCareId;



    public PositionCode() {
    }

    public PositionCode(String name) {
        this.name = name;
    }


    public PositionCode(String name, String description, String timeCareId) {
        this.name = name;
        this.description = description;
        this.timeCareId = timeCareId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTimeCareId() {
        return timeCareId;
    }

    public void setTimeCareId(String timeCareId) {
        this.timeCareId = timeCareId;
    }
}
