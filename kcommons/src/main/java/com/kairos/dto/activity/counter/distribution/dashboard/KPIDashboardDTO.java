package com.kairos.dto.activity.counter.distribution.dashboard;

import com.kairos.dto.activity.counter.enums.ConfLevel;

import java.util.List;

public class KPIDashboardDTO {
    //private BigInteger id;
    private String parentModuleId;
    private String moduleId;
    private String name;
    private Long countryId;
    private Long unitId;
    private Long staffId;
    private ConfLevel level;
    private boolean enable=true;
    private boolean active=true;
    private boolean defaultTab;
    private List<Long> unitIds;


//    public BigInteger getId() {
//        return id;
//    }
//
//    public void setId(BigInteger id) {
//        this.id = id;
//    }

    public String getParentModuleId() {
        return parentModuleId;
    }

    public void setParentModuleId(String parentModuleId) {
        this.parentModuleId = parentModuleId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public ConfLevel getLevel() {
        return level;
    }

    public void setLevel(ConfLevel level) {
        this.level = level;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isDefaultTab() {
        return defaultTab;
    }

    public void setDefaultTab(boolean defaultTab) {
        this.defaultTab = defaultTab;
    }

    public List<Long> getUnitIds() {
        return unitIds;
    }

    public void setUnitIds(List<Long> unitIds) {
        this.unitIds = unitIds;
    }
}
