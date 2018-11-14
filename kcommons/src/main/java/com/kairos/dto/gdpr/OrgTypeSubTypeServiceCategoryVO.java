package com.kairos.dto.gdpr;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties
public class OrgTypeSubTypeServiceCategoryVO {

    private Long id;
    private String name;
    private Long countryId;
    private List<OrganizationSubType> organizationSubTypes;
    private List<ServiceCategory> organizationServices;
    private List<SubServiceCategory> organizationSubServices;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public List<OrganizationSubType> getOrganizationSubTypes() { return organizationSubTypes; }

    public void setOrganizationSubTypes(List<OrganizationSubType> organizationSubTypes) { this.organizationSubTypes = organizationSubTypes; }

    public List<ServiceCategory> getOrganizationServices() { return organizationServices; }

    public void setOrganizationServices(List<ServiceCategory> organizationServices) { this.organizationServices = organizationServices; }

    public List<SubServiceCategory> getOrganizationSubServices() { return organizationSubServices; }

    public void setOrganizationSubServices(List<SubServiceCategory> organizationSubServices) { this.organizationSubServices = organizationSubServices; }

    public Long getCountryId() { return countryId; }

    public void setCountryId(Long countryId) { this.countryId = countryId; }
}
