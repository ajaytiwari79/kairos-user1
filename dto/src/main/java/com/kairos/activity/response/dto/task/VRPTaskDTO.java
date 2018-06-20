package com.kairos.activity.response.dto.task;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.activity.response.dto.TaskTypeDTO;
import com.kairos.client.dto.TaskAddress;

import java.math.BigInteger;

/**
 * @author pradeep
 * @date - 14/6/18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VRPTaskDTO {
    private BigInteger id;
    private TaskAddress address;
    //Vrp settings
    private Integer installationNumber;
    private Long citizenId;
    private String skill;
    private BigInteger taskTypeId;
    private String citizenName;
    private TaskTypeDTO taskType;
    private Long unitId;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public TaskTypeDTO getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskTypeDTO taskType) {
        this.taskType = taskType;
    }

    public String getCitizenName() {
        return citizenName;
    }

    public void setCitizenName(String citizenName) {
        this.citizenName = citizenName;
    }

    public BigInteger getTaskTypeId() {
        return taskTypeId;
    }

    public void setTaskTypeId(BigInteger taskTypeId) {
        this.taskTypeId = taskTypeId;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public Long getCitizenId() {
        return citizenId;
    }

    public void setCitizenId(Long citizenId) {
        this.citizenId = citizenId;
    }

    public TaskAddress getAddress() {
        return address;
    }

    public void setAddress(TaskAddress address) {
        this.address = address;
    }

    public Integer getInstallationNumber() {
        return installationNumber;
    }

    public void setInstallationNumber(Integer installationNumber) {
        this.installationNumber = installationNumber;
    }
}
