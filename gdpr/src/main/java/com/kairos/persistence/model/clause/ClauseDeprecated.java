package com.kairos.persistence.model.clause;


import com.kairos.dto.gdpr.OrganizationSubTypeDTO;
import com.kairos.dto.gdpr.OrganizationTypeDTO;
import com.kairos.dto.gdpr.ServiceCategoryDTO;
import com.kairos.dto.gdpr.SubServiceCategoryDTO;
import com.kairos.dto.gdpr.master_data.AccountTypeVO;
import com.kairos.persistence.model.clause_tag.ClauseTagDeprecated;
import org.springframework.data.annotation.Transient;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


public class ClauseDeprecated {

    @NotBlank
    private String title;
    @NotEmpty
    private List<ClauseTagDeprecated> tags = new ArrayList<>();
    @NotNull
    private String description;
    private List<OrganizationTypeDTO> organizationTypes;
    private List<OrganizationSubTypeDTO> organizationSubTypes;
    private List<ServiceCategoryDTO> organizationServices;
    private List<SubServiceCategoryDTO> organizationSubServices;
    private List<AccountTypeVO> accountTypes;
    private Long countryId;
    private List<Long> organizationList;
    private BigInteger parentClauseId;
    private List<BigInteger> templateTypes;
    @Transient
    private Integer orderedIndex;
    @Transient
    private String titleHtml;
    @Transient
    private String descriptionHtml;



    public ClauseDeprecated(Long countryId, String title, String description) {
        this.countryId = countryId;
        this.title = title;
        this.description = description;
    }

    public ClauseDeprecated(@NotBlank String title, @NotNull String description) {
        this.title = title;
        this.description = description;
    }

    public ClauseDeprecated(@NotBlank String title, @NotNull String description, @NotEmpty List<ClauseTagDeprecated> tags) {
        this.title = title;
        this.description = description;
        this.tags=tags;
    }

    public ClauseDeprecated(@NotBlank String title, @NotNull String description, @NotEmpty List<ClauseTagDeprecated> tags, List<BigInteger> templateTypes) {
        this.title = title;
        this.description = description;
        this.tags=tags;
        this.templateTypes=templateTypes;
    }

    public ClauseDeprecated(String title, String description, Long countryId, List<OrganizationTypeDTO> organizationTypes, List<OrganizationSubTypeDTO> organizationSubTypes, List<ServiceCategoryDTO> organizationServices, List<SubServiceCategoryDTO> organizationSubServices) {
        this.title = title;
        this.description = description;
        this.organizationTypes = organizationTypes;
        this.organizationSubTypes = organizationSubTypes;
        this.organizationServices = organizationServices;
        this.organizationSubServices = organizationSubServices;
        this.countryId = countryId;
    }

    public ClauseDeprecated() {
    }

    public List<Long> getOrganizationList() {
        return organizationList;
    }

    public void setOrganizationList(List<Long> organizationList) {
        this.organizationList = organizationList;
    }

    public List<BigInteger> getTemplateTypes() {
        return templateTypes;
    }

    public void setTemplateTypes(List<BigInteger> templateTypes) {
        this.templateTypes = templateTypes;
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

    public BigInteger getParentClauseId() {
        return parentClauseId;
    }

    public void setParentClauseId(BigInteger parentClauseId) {
        this.parentClauseId = parentClauseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ClauseTagDeprecated> getTags() {
        return tags;
    }

    public void setTags(List<ClauseTagDeprecated> tags) {
        this.tags = tags;
    }

    public String getDescription() {
        return description;
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

    public List<OrganizationSubTypeDTO> getOrganizationSubTypeDTOS() {
        return organizationSubTypes;
    }

    public void setOrganizationSubTypeDTOS(List<OrganizationSubTypeDTO> organizationSubTypes) {
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

    public List<AccountTypeVO> getAccountTypes() {
        return accountTypes;
    }

    public void setAccountTypes(List<AccountTypeVO> accountTypes) {
        this.accountTypes = accountTypes;
    }

    public Integer getOrderedIndex() {
        return orderedIndex;
    }

    public void setOrderedIndex(Integer orderedIndex) {
        this.orderedIndex = orderedIndex;
    }

    public String getTitleHtml() {
        return titleHtml;
    }

    public void setTitleHtml(String titleHtml) {
        this.titleHtml = titleHtml;
    }

    public String getDescriptionHtml() {
        return descriptionHtml;
    }

    public void setDescriptionHtml(String descriptionHtml) {
        this.descriptionHtml = descriptionHtml;
    }


}
