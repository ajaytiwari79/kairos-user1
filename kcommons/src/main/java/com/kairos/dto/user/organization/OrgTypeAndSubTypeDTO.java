package com.kairos.dto.user.organization;

import java.util.List;

public class OrgTypeAndSubTypeDTO {
    private Long organizationTypeId;
    private String organizationTypeName;
    private Long organizationSubTypeId;
    private String organizationSubTypeName;
    private List<Long> subTypeId; // same as above but its list We will change
    private Long countryId;
    private Long parentOrganizationId;

    public OrgTypeAndSubTypeDTO() {
        //Default Constructor
    }

    public OrgTypeAndSubTypeDTO(Long countryId, Long parentOrganizationId) {
        this.countryId = countryId;
        this.parentOrganizationId = parentOrganizationId;
    }

    public OrgTypeAndSubTypeDTO(Long organizationTypeId, Long organizationSubTypeId, Long countryId) {
        this.organizationTypeId = organizationTypeId;
        this.organizationSubTypeId = organizationSubTypeId;
        this.countryId = countryId;
    }

    public OrgTypeAndSubTypeDTO(Long organizationTypeId, List<Long> subTypeId, Long countryId) {
        this.organizationTypeId = organizationTypeId;
        this.subTypeId = subTypeId;
        this.countryId = countryId;
    }

    public Long getOrganizationTypeId() {
        return organizationTypeId;
    }

    public void setOrganizationTypeId(Long organizationTypeId) {
        this.organizationTypeId = organizationTypeId;
    }

    public Long getOrganizationSubTypeId() {
        return organizationSubTypeId;
    }

    public void setOrganizationSubTypeId(Long organizationSubTypeId) {
        this.organizationSubTypeId = organizationSubTypeId;
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

    public List<Long> getSubTypeId() {
        return subTypeId;
    }

    public void setSubTypeId(List<Long> subTypeId) {
        this.subTypeId = subTypeId;
    }

    public Long getParentOrganizationId() {
        return parentOrganizationId;
    }

    public void setParentOrganizationId(Long parentOrganizationId) {
        this.parentOrganizationId = parentOrganizationId;
    }

    public String getOrganizationTypeName() {
        return organizationTypeName;
    }

    public void setOrganizationTypeName(String organizationTypeName) {
        this.organizationTypeName = organizationTypeName;
    }

    public String getOrganizationSubTypeName() {
        return organizationSubTypeName;
    }

    public void setOrganizationSubTypeName(String organizationSubTypeName) {
        this.organizationSubTypeName = organizationSubTypeName;
    }
}
