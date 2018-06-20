package com.kairos.dto.solverconfig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.enums.solver_config.PlanningType;

import java.math.BigInteger;
import java.util.List;

/**
 * @author pradeep
 * @date - 20/6/18
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SolverConfigDTO {

    private BigInteger id;
    private String name;
    private boolean isTemplate;
    private PlanningType phase;
    private BigInteger parentSolverConfigId;
    private Integer terminationTime;
    private List<ConstraintValueDTO> constraints;
    private boolean isDefault;

    public List<ConstraintValueDTO> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<ConstraintValueDTO> constraints) {
        this.constraints = constraints;
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isTemplate() {
        return isTemplate;
    }

    public void setTemplate(boolean template) {
        isTemplate = template;
    }

    public PlanningType getPhase() {
        return phase;
    }

    public void setPhase(PlanningType phase) {
        this.phase = phase;
    }


    public BigInteger getParentSolverConfigId() {
        return parentSolverConfigId;
    }

    public void setParentSolverConfigId(BigInteger parentSolverConfigId) {
        this.parentSolverConfigId = parentSolverConfigId;
    }

    public Integer getTerminationTime() {
        return terminationTime;
    }

    public void setTerminationTime(Integer terminationTime) {
        this.terminationTime = terminationTime;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
