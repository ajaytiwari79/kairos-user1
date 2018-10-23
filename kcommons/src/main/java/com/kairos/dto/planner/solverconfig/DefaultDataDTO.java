package com.kairos.dto.planner.solverconfig;

import com.kairos.dto.activity.period.PlanningPeriodDTO;
import com.kairos.dto.activity.phase.PhaseDTO;
import com.kairos.dto.user.organization.OrganizationServiceDTO;

import java.util.List;

public class DefaultDataDTO {

    private List<OrganizationServiceDTO> organizationServiceDTOS;
    private List<PhaseDTO>  phaseDTOS;
    private List<PlanningPeriodDTO> planningPeriodDTOS;

    public List<OrganizationServiceDTO> getOrganizationServiceDTOS() {
        return organizationServiceDTOS;
    }

    public void setOrganizationServiceDTOS(List<OrganizationServiceDTO> organizationServiceDTOS) {
        this.organizationServiceDTOS = organizationServiceDTOS;
    }

    public List<PhaseDTO> getPhaseDTOS() {
        return phaseDTOS;
    }

    public void setPhaseDTOS(List<PhaseDTO> phaseDTOS) {
        this.phaseDTOS = phaseDTOS;
    }

    public List<PlanningPeriodDTO> getPlanningPeriodDTOS() {
        return planningPeriodDTOS;
    }

    public void setPlanningPeriodDTOS(List<PlanningPeriodDTO> planningPeriodDTOS) {
        this.planningPeriodDTOS = planningPeriodDTOS;
    }

    /***********************Builder*****************************/
    public DefaultDataDTO setOrganizationServiceDTOSBuilder(List<OrganizationServiceDTO> organizationServiceDTOS) {
        this.organizationServiceDTOS = organizationServiceDTOS;
        return this;
    }
    public DefaultDataDTO setPhaseDTOSBuilder(List<PhaseDTO> phaseDTOS) {
        this.phaseDTOS = phaseDTOS;
        return this;
    }

    public DefaultDataDTO setPlanningPeriodDTOSBuilder(List<PlanningPeriodDTO> planningPeriodDTOS) {
        this.planningPeriodDTOS = planningPeriodDTOS;
        return this;
    }
}
