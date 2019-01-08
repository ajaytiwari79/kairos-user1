package com.kairos.persistence.model.agreement_template;


import com.kairos.dto.gdpr.OrganizationSubTypeDTO;
import com.kairos.dto.gdpr.OrganizationTypeDTO;
import com.kairos.dto.gdpr.ServiceCategoryDTO;
import com.kairos.dto.gdpr.SubServiceCategoryDTO;
import com.kairos.dto.gdpr.agreement_template.CoverPageVO;
import com.kairos.dto.gdpr.master_data.AccountTypeVO;
import com.kairos.persistence.model.clause_tag.ClauseTag;
import com.kairos.persistence.model.common.MongoBaseEntity;
import org.springframework.data.annotation.Transient;
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
    private List<OrganizationTypeDTO> organizationTypeDTOS;
    private List<OrganizationSubTypeDTO> organizationSubTypeDTOS;
    private List<ServiceCategoryDTO> organizationServices;
    private List<SubServiceCategoryDTO> organizationSubServices;
    private BigInteger templateTypeId;
    private boolean coverPageAdded;
    private boolean includeContentPage;
    private boolean signatureComponentAdded;
    private boolean signatureComponentLeftAlign;
    private boolean signatureComponentRightAlign;
    private String  signatureHtml;
    private CoverPageVO coverPageData = new CoverPageVO();
    @Transient
    private ClauseTag defaultClauseTag;


    public PolicyAgreementTemplate(String name, String description, Long countryId, List<OrganizationTypeDTO> organizationTypeDTOS, List<OrganizationSubTypeDTO> organizationSubTypeDTOS, List<ServiceCategoryDTO> organizationServices, List<SubServiceCategoryDTO> organizationSubServices) {
        this.name = name;
        this.description = description;
        this.countryId = countryId;
        this.organizationTypeDTOS = organizationTypeDTOS;
        this.organizationSubTypeDTOS = organizationSubTypeDTOS;
        this.organizationServices = organizationServices;
        this.organizationSubServices = organizationSubServices;
    }

    public PolicyAgreementTemplate(@NotBlank(message = "Name cannot be empty") String name, @NotBlank(message = "Description cannot be empty") String description, BigInteger templateTypeId) {
        this.name = name;
        this.description = description;
        this.templateTypeId = templateTypeId;
    }

    public boolean isIncludeContentPage() { return includeContentPage; }

    public void setIncludeContentPage(boolean includeContentPage) { this.includeContentPage = includeContentPage; }

    public PolicyAgreementTemplate() {
    }

    public boolean isCoverPageAdded() { return coverPageAdded; }

    public void setCoverPageAdded(boolean coverPageAdded) { this.coverPageAdded = coverPageAdded; }

    public CoverPageVO getCoverPageData() { return coverPageData; }

    public void setCoverPageData(CoverPageVO coverPageData) { this.coverPageData = coverPageData; }

    public BigInteger getTemplateTypeId() { return templateTypeId; }

    public void setTemplateTypeId(BigInteger templateTypeId) { this.templateTypeId = templateTypeId; }

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

    public List<OrganizationTypeDTO> getOrganizationTypeDTOS() {
        return organizationTypeDTOS;
    }

    public PolicyAgreementTemplate setOrganizationTypeDTOS(List<OrganizationTypeDTO> organizationTypeDTOS) { this.organizationTypeDTOS = organizationTypeDTOS;return this; }

    public List<OrganizationSubTypeDTO> getOrganizationSubTypeDTOS() {
        return organizationSubTypeDTOS;
    }

    public PolicyAgreementTemplate setOrganizationSubTypeDTOS(List<OrganizationSubTypeDTO> organizationSubTypeDTOS) { this.organizationSubTypeDTOS = organizationSubTypeDTOS; return this;}

    public List<ServiceCategoryDTO> getOrganizationServices() {
        return organizationServices;
    }

    public PolicyAgreementTemplate setOrganizationServices(List<ServiceCategoryDTO> organizationServices) { this.organizationServices = organizationServices; return this;}

    public List<SubServiceCategoryDTO> getOrganizationSubServices() {
        return organizationSubServices;
    }

    public PolicyAgreementTemplate setOrganizationSubServices(List<SubServiceCategoryDTO> organizationSubServices) { this.organizationSubServices = organizationSubServices; return this;}

    public List<AccountTypeVO> getAccountTypes() { return accountTypes; }

    public PolicyAgreementTemplate setAccountTypes(List<AccountTypeVO> accountTypes) { this.accountTypes = accountTypes;return this; }

    public boolean isSignatureComponentAdded() { return signatureComponentAdded; }

    public void setSignatureComponentAdded(boolean signatureComponentAdded) { this.signatureComponentAdded = signatureComponentAdded; }

    public String getSignatureHtml() { return signatureHtml; }

    public void setSignatureHtml(String signatureHtml) { this.signatureHtml = signatureHtml; }

    public boolean isSignatureComponentLeftAlign() { return signatureComponentLeftAlign; }

    public void setSignatureComponentLeftAlign(boolean signatureComponentLeftAlign) { this.signatureComponentLeftAlign = signatureComponentLeftAlign; }

    public boolean isSignatureComponentRightAlign() { return signatureComponentRightAlign; }

    public void setSignatureComponentRightAlign(boolean signatureComponentRightAlign) { this.signatureComponentRightAlign = signatureComponentRightAlign; }

    public ClauseTag getDefaultClauseTag() { return defaultClauseTag; }

    public void setDefaultClauseTag(ClauseTag defaultClauseTag) { this.defaultClauseTag = defaultClauseTag; }
}
