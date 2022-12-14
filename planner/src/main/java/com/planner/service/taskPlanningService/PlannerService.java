package com.planner.service.taskPlanningService;

import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.dto.planner.vrp.vrpPlanning.VRPIndictmentDTO;
import com.kairos.dto.planner.vrp.vrpPlanning.VrpTaskPlanningDTO;
import com.planner.domain.vrpPlanning.VRPPlanningSolution;
import com.planner.repository.solver_config.SolverConfigRepository;
import com.planner.repository.vrpPlanning.IndictmentMongoRepository;
import com.planner.repository.vrpPlanning.VRPPlanningMongoRepository;
import com.planner.service.shift_planning.ShiftPlanningInitializationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;


@Service
public class PlannerService {

    private static Logger log= LoggerFactory.getLogger(PlannerService.class);

    @Autowired private SolverConfigRepository solverConfigRepository;
    @Autowired private VRPPlanningMongoRepository vrpPlanningMongoRepository;
    @Autowired private VRPPlannerService vrpPlannerService;
    @Autowired private IndictmentMongoRepository indictmentMongoRepository;
    @Inject private ShiftPlanningInitializationService shiftPlanningInitializationService;


    public boolean submitVRPPlanning(VrpTaskPlanningDTO vrpTaskPlanningDTO){
        VRPPlanningSolution solution = vrpPlanningMongoRepository.getSolutionBySolverConfigId(vrpTaskPlanningDTO.getSolverConfig().getId());
        if(solution!=null){
            vrpPlanningMongoRepository.delete(solution);
        }
        vrpPlannerService.startVRPPlanningSolverOnThisVM(vrpTaskPlanningDTO);
        return true;
    }

    public boolean stopVRPPlanning(BigInteger solverConfigId){
        vrpPlannerService.terminateEarlyVrpPlanningSolver(solverConfigId.toString());
        return true;
    }

    public VrpTaskPlanningDTO getSolutionBySolverConfigId(BigInteger solverConfigId){
        VRPPlanningSolution solution = vrpPlanningMongoRepository.getSolutionBySolverConfigId(solverConfigId);
        return ObjectMapperUtils.copyPropertiesByMapper(solution,VrpTaskPlanningDTO.class);
    }

    public VRPIndictmentDTO getIndictmentBySolverConfigId(BigInteger solverConfigId){
        VRPIndictmentDTO vrpIndictmentDTO = indictmentMongoRepository.getIndictmentBySolverConfigId(solverConfigId);
        return vrpIndictmentDTO;
    }





}
