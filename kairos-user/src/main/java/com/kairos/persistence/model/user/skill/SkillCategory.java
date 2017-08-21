package com.kairos.persistence.model.user.skill;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import com.kairos.persistence.model.common.UserBaseEntity;
import com.kairos.persistence.model.user.country.Country;

import static com.kairos.persistence.model.constants.RelationshipConstants.*;

/**
 * SkillCategory Domain
 * SkillCategory has relationship with Skill
 */
@NodeEntity
public class SkillCategory extends UserBaseEntity {

    @Relationship(type = BELONGS_TO)
    Country country;

    @NotEmpty(message = "error.SkillCategory.name.notEmpty") @NotNull(message = "error.SkillCategory.name.notnull")
    private String name;

    @NotEmpty(message = "error.SkillCategory.description.notEmpty") @NotNull(message = "error.SkillCategory.description.notnull")
    private String description;


    private boolean isEnabled = true;



    public SkillCategory() {

    }

    public SkillCategory(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Country getCountry() {
        return country;
    }

    public String getDescription() {
        return description;
    }

    public Map<String,Object> retieveDetails() {
        Map<String,Object> objectMap = new HashMap<>();
        objectMap.put("id",this.id);
        objectMap.put("name",this.getName());
        objectMap.put("description",this.description);
        return objectMap;
    }
}
