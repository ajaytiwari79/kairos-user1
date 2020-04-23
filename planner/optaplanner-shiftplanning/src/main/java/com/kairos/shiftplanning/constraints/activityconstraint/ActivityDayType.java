package com.kairos.shiftplanning.constraints.activityconstraint;

import com.kairos.enums.constraint.ScoreLevel;
import com.kairos.shiftplanning.constraints.Constraint;
import com.kairos.shiftplanning.domain.activity.Activity;
import com.kairos.shiftplanning.domain.shift.ShiftImp;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

import static com.kairos.shiftplanning.utils.ShiftPlanningUtility.isValidForDayType;

/**
 * @author pradeep
 * @date - 18/12/18
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class ActivityDayType implements Constraint {

    private List<DayType> dayTypes;
    private ScoreLevel level;
    private int weight;


    public ActivityDayType(List<DayType> dayTypes, ScoreLevel level, int weight) {
        this.dayTypes = dayTypes;
        this.level = level;
        this.weight = weight;
    }


    @Override
    public int checkConstraints(Activity activity, ShiftImp shift) {
        return isValidForDayType(shift,this.dayTypes) ? 0 : 1;
    }

    public int checkConstraints(List<ShiftImp> shifts) {
        return 0;
    }

}
