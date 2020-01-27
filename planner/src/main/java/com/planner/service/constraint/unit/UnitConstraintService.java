package com.planner.service.constraint.unit;

import com.kairos.commons.planning_setting.PlanningSetting;
import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.commons.utils.ObjectUtils;
import com.kairos.dto.activity.activity.ActivityConstraintDTO;
import com.kairos.dto.planner.constarints.unit.UnitConstraintDTO;
import com.kairos.enums.constraint.ConstraintSubType;
import com.planner.domain.constraint.unit.UnitConstraint;
import com.planner.repository.constraint.UnitConstraintRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static com.kairos.enums.constraint.ConstraintLevel.HARD;
import static com.kairos.enums.constraint.ConstraintLevel.SOFT;
import static com.kairos.enums.constraint.ConstraintSubType.ACTIVITY_SHORTEST_DURATION_RELATIVE_TO_SHIFT_LENGTH;
import static com.kairos.enums.constraint.ConstraintSubType.ACTIVITY_VALID_DAYTYPE;
import static org.mockito.ArgumentMatchers.isNotNull;

@Service
public class UnitConstraintService {

    @Inject
    private UnitConstraintRepository unitConstraintRepository;

    public UnitConstraintDTO createUnitConstraint(UnitConstraintDTO unitConstraintDTO){
        UnitConstraint unitConstraint = unitConstraintRepository.findByUnitIdAndConstraintSubTypeAndDeletedFalse(unitConstraintDTO.getUnitId(),unitConstraintDTO.getConstraintSubType());
        if(ObjectUtils.isNotNull(unitConstraint)){
            if(!unitConstraint.getMandatory()) {
                unitConstraint.setPlanningSetting(unitConstraintDTO.getPlanningSetting());
            }
        }else{
            unitConstraint = ObjectMapperUtils.copyPropertiesByMapper(unitConstraintDTO, UnitConstraint.class);

        }
        unitConstraintRepository.save(unitConstraint);
        return unitConstraintDTO;
    }


    public List<UnitConstraintDTO> getAllUnitConstraintByUnitId(Long unitId){
        List<UnitConstraint> unitConstraints = unitConstraintRepository.findAllByUnitIdAndDeletedFalse(unitId);
        if(ObjectUtils.isCollectionNotEmpty(unitConstraints)) {
            return ObjectMapperUtils.copyPropertiesOfCollectionByMapper(unitConstraints, UnitConstraintDTO.class);
        }else{
          return getunitconstraints();
        }
    }

    public List<UnitConstraintDTO> getunitconstraints(){
        List<UnitConstraintDTO> unitConstraintDTOS = new ArrayList<>();
        for(int i=0;i < ConstraintSubType.values().length;i++) {
            PlanningSetting planningSetting1 = new PlanningSetting(HARD, 2);
            UnitConstraintDTO unitConstraintDTO1 = new UnitConstraintDTO(planningSetting1, ConstraintSubType.values()[i]);
            unitConstraintDTOS.add(unitConstraintDTO1);
        }
        return unitConstraintDTOS;
    }






}
