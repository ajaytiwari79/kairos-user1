package com.kairos.persistence.model.activity;

import com.kairos.dto.activity.activity.activity_tabs.PhaseSettingsActivityTab;
import com.kairos.enums.OrganizationHierarchy;
import com.kairos.enums.TimeTypeEnum;
import com.kairos.enums.TimeTypes;
import com.kairos.persistence.model.activity.tabs.SkillActivityTab;
import com.kairos.persistence.model.activity.tabs.TimeCalculationActivityTab;
import com.kairos.persistence.model.activity.tabs.rules_activity_tab.RulesActivityTab;
import com.kairos.persistence.model.common.MongoBaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Document(collection = "time_Type")
@Getter
@Setter
public class TimeType extends MongoBaseEntity{

    private Long countryId;
    private TimeTypes timeTypes;
    private BigInteger upperLevelTimeTypeId;
    private String label;
    private boolean leafNode;
    private String description;
    private List<BigInteger> childTimeTypeIds = new ArrayList<>();
    private String backgroundColor;
    private TimeTypeEnum secondLevelType;
    private Set<OrganizationHierarchy> activityCanBeCopiedForOrganizationHierarchy;
    private boolean partOfTeam;
    private boolean allowChildActivities;
    private boolean allowedConflicts;
    private RulesActivityTab rulesActivityTab;
    private TimeCalculationActivityTab timeCalculationActivityTab;
    private SkillActivityTab skillActivityTab;
    private PhaseSettingsActivityTab phaseSettingsActivityTab;
    private List<Long> expertises;
    private List<Long> organizationTypes;
    private List<Long> organizationSubTypes;
    private List<Long> regions;
    private List<Long> levels;
    private List<Long> employmentTypes;
    private boolean breakNotHeldValid;

    public TimeType() {}

    public TimeType(TimeTypes timeTypes, String label, String description,String backgroundColor,TimeTypeEnum secondLevelType,Long countryId,Set<OrganizationHierarchy> activityCanBeCopiedForOrganizationHierarchy) {
        this.timeTypes = timeTypes;
        this.label = label;
        this.description = description;
        this.backgroundColor=backgroundColor;
        this.leafNode = true;
        this.secondLevelType=secondLevelType;
        this.countryId=countryId;
        this.activityCanBeCopiedForOrganizationHierarchy = activityCanBeCopiedForOrganizationHierarchy;
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }


    public List<BigInteger> getChildTimeTypeIds() {
        return childTimeTypeIds;
    }

    public void setChildTimeTypeIds(List<BigInteger> childTimeTypeIds) {
        this.childTimeTypeIds = childTimeTypeIds;
    }

    public TimeTypes getTimeTypes() {
        return timeTypes;
    }

    public void setTimeTypes(TimeTypes timeTypes) {
        this.timeTypes = timeTypes;
    }

    public BigInteger getUpperLevelTimeTypeId() {
        return upperLevelTimeTypeId;
    }

    public void setUpperLevelTimeTypeId(BigInteger upperLevelTimeTypeId) {
        this.upperLevelTimeTypeId = upperLevelTimeTypeId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isLeafNode() {
        return leafNode;
    }

    public void setLeafNode(boolean leafNode) {
        this.leafNode = leafNode;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public TimeTypeEnum getSecondLevelType() {
        return secondLevelType;
    }

    public void setSecondLevelType(TimeTypeEnum secondLevelType) {
        this.secondLevelType = secondLevelType;
    }

    public Set<OrganizationHierarchy> getActivityCanBeCopiedForOrganizationHierarchy() {
        return activityCanBeCopiedForOrganizationHierarchy;
    }

    public void setActivityCanBeCopiedForOrganizationHierarchy(Set<OrganizationHierarchy> activityCanBeCopiedForOrganizationHierarchy) {
        this.activityCanBeCopiedForOrganizationHierarchy = activityCanBeCopiedForOrganizationHierarchy;
    }

    public boolean isPartOfTeam() {
        return partOfTeam;
    }

    public void setPartOfTeam(boolean partOfTeam) {
        this.partOfTeam = partOfTeam;
    }

    public boolean isAllowChildActivities() {
        return allowChildActivities;
    }

    public void setAllowChildActivities(boolean allowChildActivities) {
        this.allowChildActivities = allowChildActivities;
    }

    public boolean isAllowedConflicts() {
        return allowedConflicts;
    }

    public void setAllowedConflicts(boolean allowedConflicts) {
        this.allowedConflicts = allowedConflicts;
    }
}
