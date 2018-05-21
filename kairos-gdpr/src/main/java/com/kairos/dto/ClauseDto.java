package com.kairos.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ClauseDto {


    @NotEmpty(message = "error.clause.title.cannotbe.empty")
    @NotNull(message = "error.clause.title.cannotbe.null")
    private String title;

    @NotNull(message = "error.message.list.cannot.be.null")
    @NotEmpty(message = "error.message.list.cannot.be.empty")
    private List<ClauseTagDto> tags = new ArrayList<>();

    @NotEmpty(message = "error.clause.title.cannotbe.empty")
    @NotNull(message = "error.clause.title.cannotbe.null")
    private String description;

    @NotNull(message = "error.message.list.cannot.be.null")
    @NotEmpty(message = "error.message.list.cannot.be.empty")
    private Set<Long> organizationTypes;

    @NotNull(message = "error.message.list.cannot.be.null")
    @NotEmpty(message = "error.message.list.cannot.be.empty")
    private Set <Long> organizationSubTypes;

    @NotNull(message = "error.message.list.cannot.be.null")
    @NotEmpty(message = "error.message.list.cannot.be.empty")
    private Set <Long>organizationServices;

    @NotNull(message = "error.message.list.cannot.be.null")
    @NotEmpty(message = "error.message.list.cannot.be.empty")
    private Set <Long> organizationSubServices;

    private Set<BigInteger> accountType;

    public String getTitle() {
        return title;
    }



    public String getDescription() {
        return description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ClauseTagDto> getTags() {
        return tags;
    }

    public void setTags(List<ClauseTagDto> tags) {
        this.tags = tags;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Long> getOrganizationTypes() {
        return organizationTypes;
    }

    public void setOrganizationTypes(Set<Long> organizationTypes) {
        this.organizationTypes = organizationTypes;
    }

    public Set<Long> getOrganizationSubTypes() {
        return organizationSubTypes;
    }

    public void setOrganizationSubTypes(Set<Long> organizationSubTypes) {
        this.organizationSubTypes = organizationSubTypes;
    }

    public Set<Long> getOrganizationServices() {
        return organizationServices;
    }

    public void setOrganizationServices(Set<Long> organizationServices) {
        this.organizationServices = organizationServices;
    }

    public Set<Long> getOrganizationSubServices() {
        return organizationSubServices;
    }

    public void setOrganizationSubServices(Set<Long> organizationSubServices) {
        this.organizationSubServices = organizationSubServices;
    }

    public Set<BigInteger> getAccountType() {
        return accountType;
    }

    public void setAccountType(Set<BigInteger> accountType) {
        this.accountType = accountType;
    }
}
