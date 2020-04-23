package com.planner.service.solverconfiguration;

import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.dto.activity.phase.PhaseDTO;
import com.kairos.dto.planner.constarints.ConstraintDTO;
import com.kairos.dto.planner.planninginfo.PlanningProblemDTO;
import com.kairos.dto.planner.solverconfig.DefaultDataDTO;
import com.kairos.dto.planner.solverconfig.SolverConfigDTO;
import com.kairos.dto.planner.solverconfig.country.CountrySolverConfigDTO;
import com.kairos.dto.user.organization.OrganizationServiceDTO;
import com.kairos.enums.constraint.ConstraintSubType;
import com.kairos.enums.constraint.ConstraintType;
import com.planner.component.exception.ExceptionService;
import com.planner.domain.constraint.country.CountryConstraint;
import com.planner.domain.constraint.unit.UnitConstraint;
import com.planner.domain.query_results.organization_service.OrganizationServiceQueryResult;
import com.planner.domain.solverconfig.common.SolverConfig;
import com.planner.domain.solverconfig.country.CountrySolverConfig;
import com.planner.domain.solverconfig.unit.UnitSolverConfig;
import com.planner.repository.constraint.ConstraintsRepository;
import com.planner.repository.planning_problem.PlanningProblemRepository;
import com.planner.repository.shift_planning.ActivityMongoRepository;
import com.planner.repository.shift_planning.UserNeo4jRepo;
import com.planner.repository.solver_config.SolverConfigRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.ObjectUtils.*;
import static com.kairos.enums.TimeTypeEnum.*;
import static com.kairos.enums.constraint.ConstraintSubType.*;
import static com.kairos.enums.constraint.ConstraintSubType.TIME_BANK;
import static com.kairos.enums.constraint.ConstraintType.*;

@Service
public class CountrySolverConfigService {

    @Inject
    private SolverConfigRepository solverConfigRepository;
    @Inject
    private ActivityMongoRepository activityMongoRepository;
    @Inject
    private UserNeo4jRepo userNeo4jRepo;
    @Inject
    private ExceptionService exceptionService;
    @Inject private PlanningProblemRepository planningProblemRepository;
    @Inject private ConstraintsRepository constraintsRepository;

    public CountrySolverConfigDTO createCountrySolverConfig(Long countryId,CountrySolverConfigDTO countrySolverConfigDTO) {
        countrySolverConfigDTO.setCountryId(countryId);
        if (preValidateCountrySolverConfigDTO(countrySolverConfigDTO)) {
            CountrySolverConfig countrySolverConfig = ObjectMapperUtils.copyPropertiesByMapper(countrySolverConfigDTO, CountrySolverConfig.class);
            List<BigInteger> countraintIds = getContraintIds(countrySolverConfigDTO, null);
            countrySolverConfig.setConstraintIds(countraintIds);
            solverConfigRepository.saveEntity(countrySolverConfig);
            copyUnitSolverConfigByOrganizationServiceAndSubService(countrySolverConfig);
            countrySolverConfigDTO.setId(countrySolverConfig.getId());
        }
        return countrySolverConfigDTO;
    }

    /**
     * copy(create) countrySolverConfig at Unit Level
     * (which is OrganizationSubServices at country Level)
     * by organizationSubServiceId.
     * we need just to find all unitId/s associated with all{OrganizationSubServices}.
     * Then for corresponding unitId/s create this same {@link CountrySolverConfig}
     *
     * @param countrySolverConfig
     */
    private void copyUnitSolverConfigByOrganizationServiceAndSubService(CountrySolverConfig countrySolverConfig) {
        List<Long> applicableUnitIdForSolverConfig = userNeo4jRepo.getUnitIdsByOrganizationSubServiceIds(countrySolverConfig.getOrganizationSubServiceIds());
        List<UnitSolverConfig> unitSolverConfigList = new ArrayList<>();
        PhaseDTO phaseDTO = activityMongoRepository.getOnePhaseById(new BigInteger(countrySolverConfig.getPhaseId().toString()));
        List<PhaseDTO> phaseDTOS = activityMongoRepository.getPhaseByUnitIdAndPhaseEnum(applicableUnitIdForSolverConfig,phaseDTO.getPhaseEnum());
        Map<Long,PhaseDTO> phaseDTOMap = phaseDTOS.stream().collect(Collectors.toMap(k->k.getOrganizationId(),v->v));
        List<UnitSolverConfig> unitSolverConfigs = solverConfigRepository.getAllSolverConfigByParentId(countrySolverConfig.getId());
        Map<Long,UnitSolverConfig> unitSolverConfigMap = unitSolverConfigs.stream().collect(Collectors.toMap(UnitSolverConfig::getUnitId,v->v));
        List<CountryConstraint> solverConfigConstraints = constraintsRepository.findAllCountryConstraintByIds(countrySolverConfig.getConstraintIds());
        if (!applicableUnitIdForSolverConfig.isEmpty()) {
            updateUnitSolverConfig(countrySolverConfig, applicableUnitIdForSolverConfig, unitSolverConfigList, phaseDTOMap, unitSolverConfigMap, solverConfigConstraints);
            for (UnitSolverConfig unitSolverConfig : unitSolverConfigMap.values()) {
                unitSolverConfig.setDeleted(true);
                unitSolverConfigList.add(unitSolverConfig);
            }
            if (isCollectionNotEmpty(unitSolverConfigList)) {
                solverConfigRepository.saveList(unitSolverConfigList);
            }
        }
    }

    private void updateUnitSolverConfig(CountrySolverConfig countrySolverConfig, List<Long> applicableUnitIdForSolverConfig, List<UnitSolverConfig> unitSolverConfigList, Map<Long, PhaseDTO> phaseDTOMap, Map<Long, UnitSolverConfig> unitSolverConfigMap, List<CountryConstraint> solverConfigConstraints) {
        for (Long unitId : applicableUnitIdForSolverConfig) {
            if (!unitSolverConfigMap.containsKey(unitId) && phaseDTOMap.containsKey(unitId)) {
                UnitSolverConfig unitSolverConfig = ObjectMapperUtils.copyPropertiesByMapper(countrySolverConfig, UnitSolverConfig.class);
                unitSolverConfig.setId(null);//Unset Id
                unitSolverConfig.setUnitId(unitId);
                List<UnitConstraint> unitConstraints = ObjectMapperUtils.copyCollectionPropertiesByMapper(solverConfigConstraints,UnitConstraint.class);
                if(isCollectionNotEmpty(unitConstraints)) {
                    constraintsRepository.saveList(unitConstraints);
                }
                List<BigInteger> unitContraintIds = unitConstraints.stream().map(unitConstraint -> unitConstraint.getId()).collect(Collectors.toList());
                unitSolverConfig.setConstraintIds(unitContraintIds);
                unitSolverConfig.setParentCountrySolverConfigId(countrySolverConfig.getId());
                unitSolverConfig.setPhaseId(phaseDTOMap.get(unitId).getId().longValue());
                unitSolverConfigList.add(unitSolverConfig);
            }else {
                unitSolverConfigMap.remove(unitId);
            }
        }
    }

    public void mapSolverConfigToOrganization(BigInteger solverConfigId,List<Long> organizationSubServiceIds){
        CountrySolverConfig countrySolverConfig = (CountrySolverConfig)solverConfigRepository.getSolverConfigById(solverConfigId,true);
        countrySolverConfig.setOrganizationSubServiceIds(organizationSubServiceIds);
        copyUnitSolverConfigByOrganizationServiceAndSubService(countrySolverConfig);
    }

    /**
     * copy(create) countrySolverConfig at country Level itself
     * Here TypeCasting is not required because coming DTO might get changed,so we require only
     * id field from previous saved solver-config.
     */
    public CountrySolverConfigDTO copyCountrySolverConfig(Long countryId,CountrySolverConfigDTO countrySolverConfigDTO) {
        SolverConfig solverConfig = solverConfigRepository.findByIdNotDeleted(countrySolverConfigDTO.getId());
        countrySolverConfigDTO.setCountryId(countryId);
        if (solverConfig != null && preValidateCountrySolverConfigDTO(countrySolverConfigDTO)) {
            CountrySolverConfig countrySolverConfig = ObjectMapperUtils.copyPropertiesByMapper(countrySolverConfigDTO, CountrySolverConfig.class);
            countrySolverConfig.setId(null);//UnSet
            List<CountryConstraint> solverConfigConstraints = constraintsRepository.findAllCountryConstraintByIds(solverConfig.getConstraintIds());
            List<CountryConstraint> countryConstraints = new ArrayList<>();
            for (CountryConstraint solverConfigConstraint : solverConfigConstraints) {
                countryConstraints.add(new CountryConstraint(solverConfigConstraint.getConstraintLevel(),solverConfigConstraint.getPenalty(),solverConfigConstraint.getName()));
            }
            constraintsRepository.saveList(countryConstraints);
            List<BigInteger> countraintids = countryConstraints.stream().map(countryConstraint -> countryConstraint.getId()).collect(Collectors.toList());
            countrySolverConfig.setConstraintIds(countraintids);
            countrySolverConfig.setParentSolverConfigId(countrySolverConfigDTO.getId());
            solverConfigRepository.saveEntity(countrySolverConfig);
            copyUnitSolverConfigByOrganizationServiceAndSubService(countrySolverConfig);
            countrySolverConfigDTO.setId(countrySolverConfig.getId());
        }
        return countrySolverConfigDTO;
    }

    public List<CountrySolverConfigDTO> getAllCountrySolverConfigByCountryId(Long countryId) {
        List<SolverConfigDTO> solverConfigDTOS = solverConfigRepository.getAllSolverConfigWithConstraints(true,countryId);
        return solverConfigDTOS.stream().map(solverConfigDTO -> (CountrySolverConfigDTO)solverConfigDTO).collect(Collectors.toList());
    }


    public CountrySolverConfigDTO updateCountrySolverConfig(Long countryId,CountrySolverConfigDTO countrySolverConfigDTO) {
        countrySolverConfigDTO.setCountryId(countryId);
        SolverConfig solverConfig = solverConfigRepository.findByIdNotDeleted(countrySolverConfigDTO.getId());
        if (solverConfig != null && preValidateCountrySolverConfigDTO(countrySolverConfigDTO)) {
            CountrySolverConfig countrySolverConfig = ObjectMapperUtils.copyPropertiesByMapper(countrySolverConfigDTO, CountrySolverConfig.class);
            List<BigInteger> countraintids = getContraintIds(countrySolverConfigDTO, solverConfig);
            countrySolverConfig.setConstraintIds(countraintids);
            solverConfigRepository.saveEntity(countrySolverConfig);
            copyUnitSolverConfigByOrganizationServiceAndSubService(countrySolverConfig);
        }
        return countrySolverConfigDTO;
    }

    private List<BigInteger> getContraintIds(CountrySolverConfigDTO countrySolverConfigDTO, SolverConfig solverConfig) {
        Map<BigInteger, CountryConstraint> countryConstraintDTOMap = new HashMap<>();
        if(isNotNull(solverConfig)){
            List<CountryConstraint> solverConfigConstraints = constraintsRepository.findAllCountryConstraintByIds(solverConfig.getConstraintIds());
            countryConstraintDTOMap = solverConfigConstraints.stream().collect(Collectors.toMap(k->k.getId(), v->v));
        }
        List<CountryConstraint> countryConstraints = new ArrayList<>();
        for (ConstraintDTO constraintDTO : countrySolverConfigDTO.getConstraints()) {
            if(countryConstraintDTOMap.containsKey(constraintDTO.getId())) {
                CountryConstraint countryConstraint = countryConstraintDTOMap.get(constraintDTO.getId());
                countryConstraint.setConstraintLevel(constraintDTO.getConstraintLevel());
                countryConstraint.setPenalty(constraintDTO.getPenalty());
                countryConstraints.add(countryConstraint);
            }
            else {
                countryConstraints.add(new CountryConstraint(constraintDTO.getConstraintLevel(),constraintDTO.getPenalty(),constraintDTO.getName()));
            }
        }
        if(isCollectionNotEmpty(countryConstraints)) {
            constraintsRepository.saveList(countryConstraints);
        }

        return countryConstraints.stream().map(countryConstraint -> countryConstraint.getId()).collect(Collectors.toList());
    }

    public boolean deleteCountrySolverConfig(BigInteger solverConfigId) {
        return solverConfigRepository.safeDeleteById(solverConfigId);
    }

    public DefaultDataDTO getDefaultData(Long countryId) {
        List<PlanningProblemDTO> planningProblemDTOS = ObjectMapperUtils.copyCollectionPropertiesByMapper(planningProblemRepository.findAll(),PlanningProblemDTO.class);
        return new DefaultDataDTO()
                .setOrganizationServicesBuilder(getOrganizationServicesAndItsSubServices(countryId))
                .setPhaseDTOSBuilder(getAllPhases(countryId)).setTimeTypeEnumSBuilder(newArrayList(PRESENCE,ABSENCE,PAID_BREAK,UNPAID_BREAK))
                .setConstraintTypesBuilder(getConstraintTypes()).setPlanningProblemsBuilder(planningProblemDTOS);
    }

    public Map<ConstraintType, Set<ConstraintSubType>> getConstraintTypes(){
        Map<ConstraintType, Set<ConstraintSubType>> constraintTypeSetMap = new HashMap<>(ConstraintType.values().length);
        constraintTypeSetMap.put(ACTIVITY,
                newHashSet(ACTIVITY_SHORTEST_DURATION_RELATIVE_TO_SHIFT_LENGTH,
                MAXIMUM_ALLOCATIONS_PER_SHIFT_FOR_THIS_ACTIVITY_PER_STAFF,
                ACTIVITY_VALID_DAYTYPE,
                ACTIVITY_MUST_CONTINUOUS_NUMBER_OF_HOURS));
        constraintTypeSetMap.put(WTA,newHashSet( AVERAGE_SCHEDULED_TIME,
                CONSECUTIVE_WORKING_PARTOFDAY,
                DAYS_OFF_IN_PERIOD,
                NUMBER_OF_PARTOFDAY,
                SHIFT_LENGTH,
                NUMBER_OF_SHIFTS_IN_INTERVAL,
                TIME_BANK,
                VETO_PER_PERIOD,
                DAILY_RESTING_TIME,
                DURATION_BETWEEN_SHIFTS,
                REST_IN_CONSECUTIVE_DAYS_AND_NIGHTS,
                WEEKLY_REST_PERIOD,
                NUMBER_OF_WEEKEND_SHIFT_IN_PERIOD,
                SHORTEST_AND_AVERAGE_DAILY_REST,
                SENIOR_DAYS_PER_YEAR,
                CHILD_CARE_DAYS_CHECK,
                DAYS_OFF_AFTER_A_SERIES,
                NO_OF_SEQUENCE_SHIFT,
                EMPLOYEES_WITH_INCREASE_RISK,
                WTA_FOR_CARE_DAYS));
        constraintTypeSetMap.put(SHIFT,new HashSet<>());
        return constraintTypeSetMap;
    }

    private List<OrganizationServiceDTO> getOrganizationServicesAndItsSubServices(Long countryId) {
        List<OrganizationServiceQueryResult> organizationServiceQueryResults = userNeo4jRepo.getAllOrganizationServices(countryId);
        return ObjectMapperUtils.copyCollectionPropertiesByMapper(organizationServiceQueryResults, OrganizationServiceDTO.class);
    }

    private List<PhaseDTO> getAllPhases(Long countryId) {
        return activityMongoRepository.getAllPhasesByCountryId(countryId);
    }

    private boolean preValidateCountrySolverConfigDTO(CountrySolverConfigDTO countrySolverConfigDTO) {
        if(solverConfigRepository.isNameExistsById(countrySolverConfigDTO.getName(), countrySolverConfigDTO.getId(), true, countrySolverConfigDTO.getCountryId())){
            exceptionService.dataNotFoundByIdException("message.name.alreadyExists");
            return false;
        }
        return true;
    }

}
