package com.kairos.shiftplanning.constraints.activityConstraint;

import com.kairos.shiftplanning.constraints.Constraint;
import com.kairos.shiftplanning.constraints.ScoreLevel;
import com.kairos.shiftplanning.domain.activity.Activity;
import com.kairos.shiftplanning.domain.activity.ActivityLineInterval;
import com.kairos.shiftplanning.domain.shift.ShiftImp;
import com.kairos.shiftplanning.utils.ShiftPlanningUtility;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MaxShiftOfStaff implements Constraint {

    private int maxAllocationPerShift;
    private ScoreLevel level;
    private int weight;

    public MaxShiftOfStaff(int maxAllocationPerShift, ScoreLevel level, int weight) {
        this.maxAllocationPerShift = maxAllocationPerShift;
        this.level = level;
        this.weight = weight;
    }


    public int checkConstraints(Activity activity, ShiftImp shift){
        List<ActivityLineInterval> alis = shift.getActivityLineIntervals();
        ShiftPlanningUtility.sortActivityLineIntervals(alis);
        int allocatedActivityCount = 0;
        ActivityLineInterval prev=null;
        for(ActivityLineInterval ali:alis){
            if(ali.getActivity().equals(activity) && !ali.getActivity().equals(prev==null?null:prev.getActivity())){
                allocatedActivityCount++;
            }
            prev=ali;
        }
        return allocatedActivityCount > maxAllocationPerShift?allocatedActivityCount-maxAllocationPerShift:0;
    }
}