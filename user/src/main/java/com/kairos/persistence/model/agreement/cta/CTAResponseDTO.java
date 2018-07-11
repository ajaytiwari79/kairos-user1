package com.kairos.persistence.model.agreement.cta;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kairos.persistence.model.organization.OrganizationType;
import com.kairos.persistence.model.user.expertise.Expertise;
import com.kairos.persistence.model.user.position_code.PositionCode;
import org.neo4j.ogm.annotation.typeconversion.DateLong;
import org.springframework.data.neo4j.annotation.QueryResult;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by pavan on 16/4/18.
 */
@QueryResult
public class CTAResponseDTO {
    @NotNull
    private Long id;
    private Long parentCTAId;
    private String name;
    private String description;
    private Expertise expertise;
    private Long organizationType;
    private Long organizationSubType;
    private List<CTARuleTemplateQueryResult> ruleTemplates = new ArrayList<>();
    private Long startDateMillis;
    private Long endDateMillis;
    // Added for version of CTA
    private List<CTAResponseDTO> versions = new ArrayList<>();
    private Map<String, Object> unitInfo;
    private PositionCode positionCode;
    private Long unitPositionId;
    private Boolean disabled;

    public CTAResponseDTO() {
        //Default constructor
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentCTAId() {
        return parentCTAId;
    }

    public void setParentCTAId(Long parentCTAId) {
        this.parentCTAId = parentCTAId;
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

    public Expertise getExpertise() {
        return expertise;
    }

    public void setExpertise(Expertise expertise) {
        this.expertise = expertise;
    }

    public Long getOrganizationType() {
        return organizationType;
    }

    public void setOrganizationType(Long organizationType) {
        this.organizationType = organizationType;
    }

    public Long getOrganizationSubType() {
        return organizationSubType;
    }

    public void setOrganizationSubType(Long organizationSubType) {
        this.organizationSubType = organizationSubType;
    }

    public List<CTARuleTemplateQueryResult> getRuleTemplates() {
        return ruleTemplates;
    }

    public void setRuleTemplates(List<CTARuleTemplateQueryResult> ruleTemplates) {
        this.ruleTemplates = ruleTemplates;
    }

    public Long getStartDateMillis() {
        return startDateMillis;
    }

    public void setStartDateMillis(Long startDateMillis) {
        this.startDateMillis = startDateMillis;
    }

    public Long getEndDateMillis() {
        return endDateMillis;
    }

    public void setEndDateMillis(Long endDateMillis) {
        this.endDateMillis = endDateMillis;
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

    public PositionCode getPositionCode() {
        return positionCode;
    }

    public void setPositionCode(PositionCode positionCode) {
        this.positionCode = positionCode;
    }

    public Long getUnitPositionId() {
        return unitPositionId;
    }

    public void setUnitPositionId(Long unitPositionId) {
        this.unitPositionId = unitPositionId;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }
}
