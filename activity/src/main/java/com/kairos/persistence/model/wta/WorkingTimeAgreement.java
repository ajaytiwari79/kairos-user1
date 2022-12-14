package com.kairos.persistence.model.wta;

import com.kairos.persistence.model.common.MongoBaseEntity;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

//@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonIgnoreProperties(ignoreUnknown = true)

/**
 * @Author pradeep singh
 *
 * @Modified added organization and staff for personal copy
 */
@Document
public class WorkingTimeAgreement extends MongoBaseEntity implements Comparable<WorkingTimeAgreement>{

    private static final long serialVersionUID = -4070274897457370782L;
    @NotNull(message = "error.WorkingTimeAgreement.name.notnull")
    private String name;

    private String description;
    // This will be only used when the countryId will update the WTA a new Copy of WTA will be assigned to organization having state disabled
    private boolean disabled;

    private Expertise expertise;

    private OrganizationType organizationType;

    private OrganizationType organizationSubType;

    private Long employmentId;

    private Long countryId;

    private WTAOrganization organization;

    private List<BigInteger> ruleTemplateIds;

    // to make a history
    private BigInteger parentId;

    private BigInteger countryParentWTA;

    private BigInteger organizationParentId;// wta id of parent organization and this must not be changable


    private List<BigInteger> tags = new ArrayList<>();

    private LocalDate startDate;
    private LocalDate endDate;
    private Date expiryDate;

    public List<BigInteger> getRuleTemplateIds() {
        return ruleTemplateIds=Optional.ofNullable(ruleTemplateIds).orElse(new ArrayList<>());
    }


    public Long getEmploymentId() {
        return employmentId;
    }

    public void setEmploymentId(Long employmentId) {
        this.employmentId = employmentId;
    }

    public void setRuleTemplateIds(List<BigInteger> ruleTemplateIds) {
        this.ruleTemplateIds = ruleTemplateIds;
    }


    public WTAOrganization getOrganization() {
        return organization;
    }

    public void setOrganization(WTAOrganization organization) {
        this.organization = organization;
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

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }


    public BigInteger getParentId() {
        return parentId;
    }

    public void setParentId(BigInteger parentId) {
        this.parentId = parentId;
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

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public OrganizationType getOrganizationType() {
        return organizationType;
    }

    public void setOrganizationType(OrganizationType organizationType) {
        this.organizationType = organizationType;
    }

    public OrganizationType getOrganizationSubType() {
        return organizationSubType;
    }

    public void setOrganizationSubType(OrganizationType organizationSubType) {
        this.organizationSubType = organizationSubType;
    }

    public List<BigInteger> getTags() {
        return tags;
    }

    public void setTags(List<BigInteger> tags) {
        this.tags = tags;
    }

    public BigInteger getCountryParentWTA() {
        return countryParentWTA;
    }

    public void setCountryParentWTA(BigInteger countryParentWTA) {
        this.countryParentWTA = countryParentWTA;
    }

    public BigInteger getOrganizationParentId() {
        return organizationParentId;
    }

    public void setOrganizationParentId(BigInteger organizationParentId) {
        this.organizationParentId = organizationParentId;
    }

    public boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isDisabled() {
        return disabled;
    }


    public WorkingTimeAgreement() {
        //default
    }


    @Override
    public int compareTo(WorkingTimeAgreement workingTimeAgreement) {
        return this.startDate.compareTo(workingTimeAgreement.startDate);
    }
}
