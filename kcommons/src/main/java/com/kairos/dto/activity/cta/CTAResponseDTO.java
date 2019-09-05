package com.kairos.dto.activity.cta;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.dto.user.country.experties.ExpertiseResponseDTO;
import com.kairos.dto.user.country.tag.TagDTO;
import com.kairos.dto.user.organization.OrganizationDTO;
import com.kairos.dto.user.organization.OrganizationTypeDTO;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by pavan on 16/4/18.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CTAResponseDTO {
    @NotNull
    private BigInteger id;
    private BigInteger parentId;
    private BigInteger organizationParentId;// cta id of parent organization and this must not be changable
    private String name;
    private String description;
    private ExpertiseResponseDTO expertise;
    private OrganizationTypeDTO organizationType;
    private OrganizationTypeDTO organizationSubType;

    private List<CTARuleTemplateDTO> ruleTemplates = new ArrayList<>();
    private LocalDate startDate;
    private LocalDate endDate;
    private OrganizationDTO organization;
    // Added for version of CTA
    private List<CTAResponseDTO> versions = new ArrayList<>();
    private Map<String, Object> unitInfo;
    private Long employmentId;
    private Boolean disabled;
    private List<TagDTO> tags;

    public CTAResponseDTO() {
        //Default constructor
    }
    public CTAResponseDTO(String name, BigInteger id,BigInteger parentId) {
        this.name = name;
        this.id = id;
        this.parentId = parentId;
    }

    public CTAResponseDTO(@NotNull BigInteger id, String name, ExpertiseResponseDTO expertise, List<CTARuleTemplateDTO> ruleTemplates, LocalDate startDate, LocalDate endDate, Boolean disabled, Long employmentId, String description) {
        this.id = id;
        this.name = name;
        this.expertise = expertise;
        this.ruleTemplates = ruleTemplates;
        this.startDate = startDate;
        this.endDate = endDate;
        this.disabled = disabled;
        this.employmentId = employmentId;
        this.description=description;
    }


    public List<TagDTO> getTags() { return tags; }

    public void setTags(List<TagDTO> tags) { this.tags = tags; }

    public OrganizationDTO getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationDTO organization) {
        this.organization = organization;
    }
    public ExpertiseResponseDTO getExpertise() {
        return expertise;
    }

    public void setExpertise(ExpertiseResponseDTO expertise) {
        this.expertise = expertise;
    }

    public OrganizationTypeDTO getOrganizationType() {
        return organizationType;
    }

    public void setOrganizationType(OrganizationTypeDTO organizationType) {
        this.organizationType = organizationType;
    }

    public OrganizationTypeDTO getOrganizationSubType() {
        return organizationSubType;
    }

    public void setOrganizationSubType(OrganizationTypeDTO organizationSubType) {
        this.organizationSubType = organizationSubType;
    }

    public List<CTARuleTemplateDTO> getRuleTemplates() {
        return ruleTemplates;
    }

    public void setRuleTemplates(List<CTARuleTemplateDTO> ruleTemplates) {
        this.ruleTemplates = ruleTemplates;
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public BigInteger getParentId() {
        return parentId;
    }

    public void setParentId(BigInteger parentId) {
        this.parentId = parentId;
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



    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<CTAResponseDTO> getVersions() {
        return versions;
    }

    public void setVersions(List<CTAResponseDTO> versions) {
        this.versions = versions;
    }

    public Map<String, Object> getUnitInfo() {
        return unitInfo;
    }

    public void setUnitInfo(Map<String, Object> unitInfo) {
        this.unitInfo = unitInfo;
    }


    public Long getEmploymentId() {
        return employmentId;
    }

    public void setEmploymentId(Long employmentId) {
        this.employmentId = employmentId;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public BigInteger getOrganizationParentId() {
        return organizationParentId;
    }

    public void setOrganizationParentId(BigInteger organizationParentId) {
        this.organizationParentId = organizationParentId;
    }
}
