package com.kairos.shiftplanning.constraints.activityconstraint;


import com.kairos.enums.TimeTypeEnum;
import com.kairos.shiftplanning.constraints.Constraint;
import com.kairos.shiftplanning.constraints.ScoreLevel;
import com.kairos.shiftplanning.domain.activity.Activity;
import com.kairos.shiftplanning.domain.activity.ShiftActivity;
import com.kairos.shiftplanning.domain.shift.ShiftImp;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/*
* This class represent constraint
* Presence and Absence type of shifts
* should not happen at same time
* */
@Getter
@Setter
@NoArgsConstructor
public class PresenceAndAbsenceAtSameTime implements Constraint {

    private ScoreLevel level;
    private int weight;

    public int checkConstraints(Activity activity, ShiftImp shift){
        Set<TimeTypeEnum> timeTypeEnumSet = shift.getActivityLineIntervals().stream().map(activityLineInterval -> activityLineInterval.getActivity().getTimeType().getTimeTypeEnum()).collect(Collectors.toSet());
        return timeTypeEnumSet.size();
    }

    @Override
    public int checkConstraints(List<ShiftImp> shifts) {
        shifts.sort(Comparator.comparing(ShiftImp::getStartDate));
        List<ShiftActivity> shiftActivities = shifts.stream().flatMap(shiftImp -> shiftImp.getShiftActivities().stream()).sorted(Comparator.comparing(ShiftActivity::getStartTime)).collect(Collectors.toList());
        int contraintPenality = 0;
        for (int i = 1; i < shiftActivities.size(); i++) {
            ShiftActivity shiftActivity = shiftActivities.get(i - 1);
            ShiftActivity nextShiftActivity = shiftActivities.get(i);
            if(shiftActivity.getInterval().overlaps(nextShiftActivity.getInterval()) && shiftActivity.getActivity().isTypePresence() && nextShiftActivity.getActivity().isTypeAbsence()){
                contraintPenality++;
            }
        }
        return contraintPenality;
    }
}
