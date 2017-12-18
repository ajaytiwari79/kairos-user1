package com.kairos.persistence.model.user.agreement.cta;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.persistence.model.common.UserBaseEntity;
import com.kairos.persistence.model.organization.OrganizationType;
import com.kairos.persistence.model.user.country.Country;
import com.kairos.persistence.model.user.expertise.Expertise;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.DateLong;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.kairos.persistence.model.constants.RelationshipConstants.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@NodeEntity
public class CostTimeAgreement extends UserBaseEntity {
    private String name;
    private String description;
    @Relationship(type = HAS_EXPERTISE_IN)
    private Expertise expertise;
    @Relationship(type = HAS_SUB_TYPE)
    private OrganizationType organizationType;
    @Relationship(type = BELONGS_TO_ORG_SUB_TYPE)
    private OrganizationType organizationSubType;
    @Relationship(type = BELONGS_TO)
    private Country country;
    @Relationship(type = HAS_PARENT_CTA)
    private CostTimeAgreement parent;
    @Relationship(type = HAS_RULE_TEMPLATE)
    private List<RuleTemplate> ruleTemplates=new ArrayList<>();
    @DateLong
    private Date startDate;
    @DateLong
    private Date endDate;
    private boolean disabled;

    public CostTimeAgreement() {
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

    public List<RuleTemplate> getRuleTemplates() {
        return ruleTemplates;
    }

    public void setRuleTemplates(List<RuleTemplate> ruleTemplates) {
        this.ruleTemplates = ruleTemplates;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public CostTimeAgreement getParent() {
        return parent;
    }

    public void setParent(CostTimeAgreement parent) {
        this.parent = parent;
    }
    public boolean hasParent(){
        return true;
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


    public void addRuleTemplate(RuleTemplate ruleTemplate) {
        if (ruleTemplate == null)
            throw new NullPointerException("Can't add null ruleTemplateCategory");
        getRuleTemplates().add(ruleTemplate);

    }

    public void removeOrganizationType(RuleTemplate ruleTemplate) {
        if (ruleTemplate == null)
            getRuleTemplates().remove(ruleTemplate);
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CostTimeAgreement that = (CostTimeAgreement) o;

        return new EqualsBuilder()
                .append(disabled, that.disabled)
                .append(name, that.name)
                .append(description, that.description)
                .append(expertise, that.expertise)
                .append(organizationType, that.organizationType)
                .append(organizationSubType, that.organizationSubType)
                .append(country, that.country)
                .append(parent, that.parent)
                .append(ruleTemplates, that.ruleTemplates)
                .append(startDate, that.startDate)
                .append(endDate, that.endDate)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(description)
                .append(expertise)
                .append(organizationType)
                .append(organizationSubType)
                .append(country)
                .append(parent)
                .append(ruleTemplates)
                .append(startDate)
                .append(endDate)
                .append(disabled)
                .toHashCode();
    }
}
