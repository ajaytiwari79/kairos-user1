package com.kairos.persistence.model.wta;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kairos.activity.wta.templates.WTABaseRuleTemplate;
import com.kairos.response.dto.web.OrganizationTypeDTO;
import com.kairos.response.dto.web.experties.ExpertiseResponseDTO;
import com.kairos.response.dto.web.tag.TagDTO;
import com.kairos.activity.wta.WTABaseRuleTemplateDTO;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author pradeep
 * @date - 13/4/18
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WTAQueryResultDTO {

    private BigInteger parentWTA;

    private BigInteger countryParentWTA;

    private BigInteger organizationParentWTA;


    private Date startDate;
    private Date endDate;
    private Long expiryDate;
    private String name;
    private String description;
    private BigInteger id;
    private ExpertiseResponseDTO expertise;
    private OrganizationTypeDTO organizationType;
    private OrganizationTypeDTO organizationSubType;

    private List<WTABaseRuleTemplate> ruleTemplates;

    private List<TagDTO> tags = new ArrayList<>();

    public List<WTABaseRuleTemplate> getRuleTemplates() {
        return ruleTemplates;
    }

    public void setRuleTemplates(List<WTABaseRuleTemplate> ruleTemplates) {
        this.ruleTemplates = ruleTemplates;
    }

    public BigInteger getCountryParentWTA() {
        return countryParentWTA;
    }

    public void setCountryParentWTA(BigInteger countryParentWTA) {
        this.countryParentWTA = countryParentWTA;
    }

    public BigInteger getOrganizationParentWTA() {
        return organizationParentWTA;
    }

    public void setOrganizationParentWTA(BigInteger organizationParentWTA) {
        this.organizationParentWTA = organizationParentWTA;
    }

    public List<TagDTO> getTags() {
        return tags;
    }

    public void setTags(List<TagDTO> tags) {
        this.tags = tags;
    }

    public BigInteger getParentWTA() {
        return parentWTA;
    }

    public void setParentWTA(BigInteger parentWTA) {
        this.parentWTA = parentWTA;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Long getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Long expiryDate) {
        this.expiryDate = expiryDate;
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

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
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

}
