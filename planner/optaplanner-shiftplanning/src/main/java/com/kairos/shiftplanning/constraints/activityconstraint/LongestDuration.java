package com.kairos.shiftplanning.constraints.activityconstraint;

import com.kairos.enums.constraint.ScoreLevel;
import com.kairos.shiftplanning.constraints.ConstraintHandler;
import com.kairos.shiftplanning.domain.activity.Activity;
import com.kairos.shiftplanning.domain.shift.ShiftImp;
import com.kairos.shiftplanning.domain.unit.Unit;
import com.kairos.shiftplanningNewVersion.entity.Shift;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class LongestDuration implements ConstraintHandler {


    //By percent
    private int longestDuration;
    private ScoreLevel level;
    private int weight;

    public LongestDuration(int longestDuration, ScoreLevel level, int weight) {
        this.longestDuration = longestDuration;
        this.level = level;
        this.weight = weight;
    }

    public int checkConstraints(Activity activity, ShiftImp shift){

        return 0;
    }

    @Override
    public int checkConstraints(List<ShiftImp> shifts){
        return 0;
    }

    @Override
    public int verifyConstraints(Activity activity, Shift shift){

        return 0;
    }

    @Override
    public int verifyConstraints(List<Shift> shifts){
        return 0;
    }

    @Override
    public int verifyConstraints(Unit unit, Shift shiftImp, List<Shift> shiftImps){return 0;};


}
