package com.kairos.activity.persistence.model.wta.templates;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.activity.persistence.model.common.MongoBaseEntity;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Created by vipul on 2/8/17.
 * used to store Rule Template Category
 */


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleTemplateCategory extends MongoBaseEntity {

     @NotNull(message = "error.RuleTemplateCategory.name.notnull")
    private String name;
    private String description;
    private RuleTemplateCategoryType ruleTemplateCategoryType;
    private Long country;
    public RuleTemplateCategory(String name, String description, RuleTemplateCategoryType ruleTemplateCategoryType) {
        this.name = name;
        this.description = description;
        this.deleted = false;
        ruleTemplateCategoryType = ruleTemplateCategoryType;
    }

    public RuleTemplateCategory() {
        //default
    }

    public RuleTemplateCategory(String name) {
        this.name = name;
    }

    private List<BigInteger> tags = new ArrayList<>();


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public RuleTemplateCategoryType getRuleTemplateCategoryType() {
        return ruleTemplateCategoryType;
    }

    public void setRuleTemplateCategoryType(RuleTemplateCategoryType ruleTemplateCategoryType) {
        this.ruleTemplateCategoryType = ruleTemplateCategoryType;
    }

    public Long getCountry() {
        return country;
    }

    public void setCountry(Long country) {
        this.country = country;
    }
    public List<BigInteger> getTags() {
        return tags;
    }

    public void setTags(List<BigInteger> tags) {
        this.tags = tags;
    }




}

