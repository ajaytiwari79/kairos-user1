package com.kairos.service.priority_group.priority_group_rules;

import com.kairos.commons.utils.DateUtils;
import com.kairos.dto.activity.open_shift.priority_group.PriorityGroupDTO;
import com.kairos.dto.user.staff.employment.StaffEmploymentQueryResult;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class UnitExperienceAndAssignedOpenShiftRule implements PriorityGroupRuleFilter{

    private Map<Long,Integer> assignedOpenShiftMap;

    public UnitExperienceAndAssignedOpenShiftRule(Map<Long,Integer> assignedOpenShiftMap) {
        this.assignedOpenShiftMap = assignedOpenShiftMap;
    }
    @Override
    public void filter(Map<BigInteger, List<StaffEmploymentQueryResult>> openShiftStaffMap, PriorityGroupDTO priorityGroupDTO) {

        final AtomicInteger thresholdShiftCount = new AtomicInteger();
        int experienceInDays = 0;

        if(Optional.ofNullable(priorityGroupDTO.getStaffExcludeFilter().getNumberOfShiftAssigned()).isPresent()) {
            thresholdShiftCount.set(priorityGroupDTO.getStaffExcludeFilter().getNumberOfShiftAssigned());

        }
        if(Optional.ofNullable(priorityGroupDTO.getStaffExcludeFilter().getUnitExperienceInWeek()).isPresent()) {
            experienceInDays = priorityGroupDTO.getStaffExcludeFilter().getUnitExperienceInWeek()*7;
        }
        Long startDate = DateUtils.getLongFromLocalDate(LocalDate.now().minusDays(experienceInDays));
        for(Map.Entry<BigInteger,List<StaffEmploymentQueryResult>> entry: openShiftStaffMap.entrySet()) {
            entry.getValue().removeIf(staffEmployment-> (Optional.ofNullable(priorityGroupDTO.getStaffExcludeFilter().getNumberOfShiftAssigned()).isPresent()&&
                    assignedOpenShiftMap.containsKey(staffEmployment.getEmploymentId())
                    &&(assignedOpenShiftMap.get(staffEmployment.getEmploymentId())>thresholdShiftCount.get()))||
                    (Optional.ofNullable(priorityGroupDTO.getStaffExcludeFilter().getUnitExperienceInWeek()).isPresent()&&staffEmployment.getStartDate()>startDate));
        }
    }

   }
