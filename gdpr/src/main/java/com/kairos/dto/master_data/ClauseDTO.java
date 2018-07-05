package com.kairos.dto.master_data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kairos.dto.OrganizationSubTypeDTO;
import com.kairos.dto.OrganizationTypeDTO;
import com.kairos.dto.ServiceCategoryDTO;
import com.kairos.dto.SubServiceCategoryDTO;
import com.kairos.utils.custome_annotation.NotNullOrEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ClauseDTO {


    @NotNullOrEmpty(message = "Title cannot be empty ")
    @Pattern(message = "Numbers and Special characters are not allowed", regexp = "^[a-zA-Z\\s]+$")
    private String title;

    @Valid
    @NotEmpty(message = "Tags  can't be empty")
    private List<ClauseTagDTO> tags = new ArrayList<>();

    @NotNullOrEmpty(message = "description  can't be  Empty ")
    private String description;

    @Valid
    @NotNull(message = "Organization  Type  can't be  null")
    @NotEmpty(message = "Organization  Type  can't be  empty")
    private List<OrganizationTypeDTO> organizationTypes;

    @Valid
    @NotNull(message = "Organization Sub Type  can't be  null")
    @NotEmpty(message = "Organization Sub Type  can't be  empty")
    private List<OrganizationSubTypeDTO> organizationSubTypes;

    @Valid
    @NotNull(message = "Service Type  can't be  null")
    @NotEmpty(message = "Service  Type  can't be  empty")
    private List<ServiceCategoryDTO> organizationServices;

    @Valid
    @NotNull(message = "Service Sub Type  can't be  null")
    @NotEmpty(message = "Service Sub Type  can't be  empty")
    private List<SubServiceCategoryDTO> organizationSubServices;

    @NotNull(message = "Account Type cannot be null")
    @NotEmpty
    private Set<BigInteger> accountTypes;

    private BigInteger templateType;

    private List<Long> organizationList;

    public List<Long> getOrganizationList() {
        return organizationList;
    }

    public void setOrganizationList(List<Long> organizationList) {
        this.organizationList = organizationList;
    }

    public BigInteger getTemplateType() { return templateType; }

    public void setTemplateType(BigInteger templateType) { this.templateType = templateType; }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ClauseTagDTO> getTags() {
        return tags;
    }

    public void setTags(List<ClauseTagDTO> tags) {
        this.tags = tags;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<OrganizationTypeDTO> getOrganizationTypes() {
        return organizationTypes;
    }

    public void setOrganizationTypes(List<OrganizationTypeDTO> organizationTypes) {
        this.organizationTypes = organizationTypes;
    }

    public List<OrganizationSubTypeDTO> getOrganizationSubTypes() {
        return organizationSubTypes;
    }

    public void setOrganizationSubTypes(List<OrganizationSubTypeDTO> organizationSubTypes) {
        this.organizationSubTypes = organizationSubTypes;
    }

    public List<ServiceCategoryDTO> getOrganizationServices() {
        return organizationServices;
    }

    public void setOrganizationServices(List<ServiceCategoryDTO> organizationServices) {
        this.organizationServices = organizationServices;
    }

    public List<SubServiceCategoryDTO> getOrganizationSubServices() {
        return organizationSubServices;
    }

    public void setOrganizationSubServices(List<SubServiceCategoryDTO> organizationSubServices) {
        this.organizationSubServices = organizationSubServices;
    }

    public Set<BigInteger> getAccountTypes() {
        return accountTypes;
    }

    public void setAccountTypes(Set<BigInteger> accountTypes) {
        this.accountTypes = accountTypes;
    }
}
