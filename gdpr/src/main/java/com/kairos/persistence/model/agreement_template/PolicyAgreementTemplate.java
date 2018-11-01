package com.kairos.persistence.model.agreement_template;


import com.kairos.dto.gdpr.OrganizationSubType;
import com.kairos.dto.gdpr.OrganizationType;
import com.kairos.dto.gdpr.ServiceCategory;
import com.kairos.dto.gdpr.SubServiceCategory;
import com.kairos.dto.gdpr.master_data.AccountTypeVO;
import com.kairos.persistence.model.common.MongoBaseEntity;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Document
public class PolicyAgreementTemplate extends MongoBaseEntity {

    @NotBlank(message = "Name cannot be empty")
    private String name;
    @NotBlank(message = "Description cannot be empty")
    private String description;
    private Long countryId;
    private List<AccountTypeVO> accountTypes;
    private List<BigInteger> agreementSections=new ArrayList<>();
    private List<OrganizationType> organizationTypes;
    private List<OrganizationSubType> organizationSubTypes;
    private List<ServiceCategory> organizationServices;
    private List<SubServiceCategory> organizationSubServices;
    private BigInteger templateType;
    private String coverPageContent;
    private String coverPageTitle;
    private String coverPageLogoUrl;

    public PolicyAgreementTemplate(String name, String description, Long countryId, List<OrganizationType> organizationTypes, List<OrganizationSubType> organizationSubTypes, List<ServiceCategory> organizationServices, List<SubServiceCategory> organizationSubServices) {
        this.name = name;
        this.description = description;
        this.countryId = countryId;
        this.organizationTypes = organizationTypes;
        this.organizationSubTypes = organizationSubTypes;
        this.organizationServices = organizationServices;
        this.organizationSubServices = organizationSubServices;
    }
    public PolicyAgreementTemplate() {
    }


    public BigInteger getTemplateType() { return templateType; }

    public PolicyAgreementTemplate setTemplateType(BigInteger templateType) { this.templateType = templateType; return this; }

    public String getName() {
        return name;
    }

    public PolicyAgreementTemplate setName(String name) { this.name = name;return this; }

    public String getDescription() {
        return description;
    }

    public PolicyAgreementTemplate setDescription(String description) { this.description = description;return this; }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

    public List<BigInteger> getAgreementSections() {
        return agreementSections;
    }

    public void setAgreementSections(List<BigInteger> agreementSections) { this.agreementSections = agreementSections; }

    public List<OrganizationType> getOrganizationTypes() {
        return organizationTypes;
    }

    public PolicyAgreementTemplate setOrganizationTypes(List<OrganizationType> organizationTypes) { this.organizationTypes = organizationTypes;return this; }

    public List<OrganizationSubType> getOrganizationSubTypes() {
        return organizationSubTypes;
    }

    public PolicyAgreementTemplate setOrganizationSubTypes(List<OrganizationSubType> organizationSubTypes) { this.organizationSubTypes = organizationSubTypes; return this;}

    public List<ServiceCategory> getOrganizationServices() {
        return organizationServices;
    }

    public PolicyAgreementTemplate setOrganizationServices(List<ServiceCategory> organizationServices) { this.organizationServices = organizationServices; return this;}

    public List<SubServiceCategory> getOrganizationSubServices() {
        return organizationSubServices;
    }

    public PolicyAgreementTemplate setOrganizationSubServices(List<SubServiceCategory> organizationSubServices) { this.organizationSubServices = organizationSubServices; return this;}

    public List<AccountTypeVO> getAccountTypes() { return accountTypes; }

    public PolicyAgreementTemplate setAccountTypes(List<AccountTypeVO> accountTypes) { this.accountTypes = accountTypes;return this; }

    public String getCoverPageContent() {
        return coverPageContent;
    }

    public void setCoverPageContent(String coverPageContent) {
        this.coverPageContent = coverPageContent;
    }

    public String getCoverPageTitle() {
        return coverPageTitle;
    }

    public void setCoverPageTitle(String coverPageTitle) {
        this.coverPageTitle = coverPageTitle;
    }

    public String getCoverPageLogoUrl() {
        return coverPageLogoUrl;
    }

    public void setCoverPageLogoUrl(String coverPageLogoUrl) {
        this.coverPageLogoUrl = coverPageLogoUrl;
    }
}
