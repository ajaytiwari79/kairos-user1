package com.kairos.persistence.model.country.default_data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.persistence.model.common.UserBaseEntity;
import com.kairos.persistence.model.country.Country;
import org.apache.commons.lang.StringUtils;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import javax.validation.constraints.NotBlank;

import static com.kairos.persistence.model.constants.RelationshipConstants.BELONGS_TO;


/**
 * Created by oodles on 9/1/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@NodeEntity
public class BusinessType extends UserBaseEntity {

    private static final long serialVersionUID = -5108345322170237054L;
    @NotBlank(message = "error.BusinessType.name.notEmpty")
    private String name;
    private String description;
    @Relationship(type = BELONGS_TO)
    private Country country;
    private boolean isEnabled = true;

    public BusinessType() {
    }

    public BusinessType(@NotBlank(message = "error.BusinessType.name.notEmpty") String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = StringUtils.trim(name);
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = StringUtils.trim(description);
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
