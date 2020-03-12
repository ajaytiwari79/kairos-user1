package com.kairos.shiftplanning.constraints.unitconstraint;

import com.kairos.shiftplanning.constraints.Constraint;
import com.kairos.shiftplanning.constraints.ScoreLevel;
import com.kairos.shiftplanning.domain.activity.Activity;
import com.kairos.shiftplanning.domain.shift.ShiftImp;
import com.kairos.shiftplanning.domain.staff.Employee;
import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public class AbsenceAndPresenceAtSameTime implements Constraint {

    private ScoreLevel level;
    private int weight;
    @Override
    public int checkConstraints(Activity activity, ShiftImp shift) {
        return 0;
    }

    @Override
    public <T extends Constraint> int checkConstraints(T t, List<ShiftImp> shifts) {
        Map<Employee,List<ShiftImp>> employeeListMap = shifts.stream().collect(Collectors.groupingBy(ShiftImp::getEmployee));
        for (Map.Entry<Employee, List<ShiftImp>> employeeListEntry : employeeListMap.entrySet()) {
            List<ShiftImp> shiftImps = employeeListEntry.getValue();
            shiftImps.sort(Comparator.comparing(ShiftImp::getStart));
            for (int i = 1; i < shiftImps.size(); i++) {
                ShiftImp shiftImp = shiftImps.get(i-1);
                ShiftImp currentShift = shiftImps.get(i);

            }
        }
        return 0;
    }
}
