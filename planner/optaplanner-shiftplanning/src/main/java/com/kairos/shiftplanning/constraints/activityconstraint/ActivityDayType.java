package com.kairos.shiftplanning.constraints.activityconstraint;

import com.kairos.enums.constraint.ScoreLevel;
import com.kairos.shiftplanning.constraints.ConstraintHandler;
import com.kairos.shiftplanning.domain.activity.Activity;
import com.kairos.shiftplanning.domain.shift.ShiftImp;
import com.kairos.shiftplanning.domain.unit.Unit;
import com.kairos.shiftplanningNewVersion.entity.Shift;
import com.kairos.shiftplanningNewVersion.utils.StaffingLevelPlanningUtility;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

import static com.kairos.shiftplanning.utils.ShiftPlanningUtility.isValidForDayType;

/**
 * @author pradeep
 * @date - 18/12/18
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class ActivityDayType implements ConstraintHandler {


    private ScoreLevel level;
    private int weight;


    public ActivityDayType(ScoreLevel level, int weight) {
        this.level = level;
        this.weight = weight;
    }


    @Override
    public int checkConstraints(Activity activity, ShiftImp shift) {
        List<DayType> dayTypes = activity.getValidDayTypeIds().stream().map(id -> shift.getEmployee().getUnit().getDayTypeMap().get(id)).collect(Collectors.toList());
        return isValidForDayType(shift,dayTypes) ? 0 : 1;
    }

    @Override
    public int verifyConstraints(Activity activity, Shift shift) {
        List<DayType> dayTypes = activity.getValidDayTypeIds().stream().map(id -> shift.getStaff().getUnit().getDayTypeMap().get(id)).collect(Collectors.toList());
        return StaffingLevelPlanningUtility.isValidForDayType(shift,dayTypes) ? 0 : 1;
    }

    @Override
    public int verifyConstraints(Unit unit, Shift shiftImp, List<Shift> shiftImps){return 0;};

}
