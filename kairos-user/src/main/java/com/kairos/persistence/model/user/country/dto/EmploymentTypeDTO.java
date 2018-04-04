package com.kairos.persistence.model.user.country.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kairos.persistence.model.enums.EmploymentCategory;
import com.kairos.persistence.model.user.country.EmploymentType;
import org.springframework.data.neo4j.annotation.QueryResult;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * Created by prerna on 7/11/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@QueryResult
public class EmploymentTypeDTO {
    private Long id;
    @NotNull(message = "error.EmploymentType.name.notEmptyOrNotNull")
    private String name;
    private String description;
    private boolean allowedForContactPerson;
    private boolean allowedForShiftPlan;
    private boolean allowedForFlexPool;
    private Set<EmploymentCategory> employmentCategories;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isAllowedForContactPerson() {
        return allowedForContactPerson;
    }

    public void setAllowedForContactPerson(boolean allowedForContactPerson) {
        this.allowedForContactPerson = allowedForContactPerson;
    }

    public boolean isAllowedForShiftPlan() {
        return allowedForShiftPlan;
    }

    public void setAllowedForShiftPlan(boolean allowedForShiftPlan) {
        this.allowedForShiftPlan = allowedForShiftPlan;
    }

    public boolean isAllowedForFlexPool() {
        return allowedForFlexPool;
    }

    public void setAllowedForFlexPool(boolean allowedForFlexPool) {
        this.allowedForFlexPool = allowedForFlexPool;
    }

    public Set<EmploymentCategory> getEmploymentCategories() {
        return employmentCategories;
    }

    public void setEmploymentCategories(Set<EmploymentCategory> employmentCategories) {
        this.employmentCategories = employmentCategories;
    }

    public EmploymentType generateEmploymentTypeFromEmploymentTypeDTO() {
        EmploymentType employmentType = new EmploymentType();
        employmentType.setName(this.getName().trim());
        employmentType.setDescription(this.getDescription());
        employmentType.setAllowedForContactPerson(this.isAllowedForContactPerson());
        employmentType.setAllowedForShiftPlan(this.isAllowedForShiftPlan());
        employmentType.setAllowedForFlexPool(this.isAllowedForFlexPool());
        employmentType.setEmploymentCategories(this.getEmploymentCategories());

        return employmentType;
    }
    @AssertTrue(message = "At least one role should be selected")
    public boolean isValid() {
        return (employmentCategories.isEmpty())?false:true;
    }

}
