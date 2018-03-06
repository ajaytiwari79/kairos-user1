package com.kairos.response.dto.web.access_group;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.persistence.model.enums.OrganizationCategory;

/**
 * Created by prerna on 5/3/18.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CountryAccessGroupDTO {

    private Long id;
    private String name;
    private String description;
    private OrganizationCategory organizationCategory;

    public CountryAccessGroupDTO(){
        // default constructor
    }

    public CountryAccessGroupDTO(String name, String description, OrganizationCategory category){
        this.name = name;
        this.description = description;
        this.organizationCategory = category;
    }

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

    public OrganizationCategory getOrganizationCategory() {
        return organizationCategory;
    }

    public void setOrganizationCategory(OrganizationCategory organizationCategory) {
        this.organizationCategory = organizationCategory;
    }
}
