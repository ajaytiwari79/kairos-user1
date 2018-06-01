package com.kairos.activity.persistence.model.unit_settings;

import com.kairos.activity.persistence.model.common.MongoBaseEntity;

import java.math.BigInteger;

public class PhaseSettings extends MongoBaseEntity {
    private BigInteger phaseId;
    private String name;
    private String description;
    private boolean staffEligibleForUnderStaffing;
    private boolean staffEligibleForOverStaffing;
    private boolean managementEligibleForUnderStaffing;
    private boolean managementEligibleForOverStaffing;
    private Long unitId;

    public PhaseSettings() {
        //Default Constructor
    }

    public BigInteger getPhaseId() {
        return phaseId;
    }

    public void setPhaseId(BigInteger phaseId) {
        this.phaseId = phaseId;
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

    public boolean isStaffEligibleForUnderStaffing() {
        return staffEligibleForUnderStaffing;
    }

    public void setStaffEligibleForUnderStaffing(boolean staffEligibleForUnderStaffing) {
        this.staffEligibleForUnderStaffing = staffEligibleForUnderStaffing;
    }

    public boolean isStaffEligibleForOverStaffing() {
        return staffEligibleForOverStaffing;
    }

    public void setStaffEligibleForOverStaffing(boolean staffEligibleForOverStaffing) {
        this.staffEligibleForOverStaffing = staffEligibleForOverStaffing;
    }

    public boolean isManagementEligibleForUnderStaffing() {
        return managementEligibleForUnderStaffing;
    }

    public void setManagementEligibleForUnderStaffing(boolean managementEligibleForUnderStaffing) {
        this.managementEligibleForUnderStaffing = managementEligibleForUnderStaffing;
    }

    public boolean isManagementEligibleForOverStaffing() {
        return managementEligibleForOverStaffing;
    }

    public void setManagementEligibleForOverStaffing(boolean managementEligibleForOverStaffing) {
        this.managementEligibleForOverStaffing = managementEligibleForOverStaffing;
    }

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }
}
