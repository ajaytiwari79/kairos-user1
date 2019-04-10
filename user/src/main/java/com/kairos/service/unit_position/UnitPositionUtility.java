package com.kairos.service.unit_position;

import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.dto.user.employment.EmploymentLinesDTO;
import com.kairos.dto.activity.shift.FunctionDTO;
import com.kairos.persistence.model.user.expertise.Response.ExpertisePlannedTimeQueryResult;
import com.kairos.persistence.model.user.unit_position.query_result.StaffUnitPositionDetails;
import com.kairos.persistence.model.user.unit_position.query_result.EmploymentLinesQueryResult;
import com.kairos.persistence.model.user.unit_position.query_result.UnitPositionQueryResult;

import java.util.List;
import java.util.Optional;

/**
 * CreatedBy vipulpandey on 27/10/18
 **/
public class UnitPositionUtility {
    public static com.kairos.dto.activity.shift.StaffUnitPositionDetails convertUnitPositionObject(UnitPositionQueryResult unitPosition) {
        com.kairos.dto.activity.shift.StaffUnitPositionDetails unitPositionDetails = new com.kairos.dto.activity.shift.StaffUnitPositionDetails();
        unitPositionDetails.setExpertise(ObjectMapperUtils.copyPropertiesByMapper(unitPosition.getExpertise(), com.kairos.dto.activity.shift.Expertise.class));
        EmploymentLinesQueryResult currentEmploymentLine = ObjectMapperUtils.copyPropertiesByMapper(unitPosition.getEmploymentLines().get(0), EmploymentLinesQueryResult.class);
        unitPositionDetails.setEmploymentType(ObjectMapperUtils.copyPropertiesByMapper(currentEmploymentLine.getEmploymentType(), com.kairos.dto.activity.shift.EmploymentType.class));
        unitPositionDetails.setId(unitPosition.getId());
        unitPositionDetails.setStartDate(unitPosition.getStartDate());

        unitPositionDetails.setAppliedFunctions(ObjectMapperUtils.copyPropertiesOfListByMapper(unitPosition.getAppliedFunctions(),FunctionDTO.class));
        unitPositionDetails.setEndDate(unitPosition.getEndDate());
        unitPositionDetails.setEmploymentLines(ObjectMapperUtils.copyPropertiesOfListByMapper(unitPosition.getEmploymentLines(), EmploymentLinesDTO.class));
        unitPositionDetails.setFullTimeWeeklyMinutes(currentEmploymentLine.getFullTimeWeeklyMinutes());
        unitPositionDetails.setTotalWeeklyMinutes(currentEmploymentLine.getTotalWeeklyMinutes());
        unitPositionDetails.setTotalWeeklyHours(currentEmploymentLine.getTotalWeeklyHours());
        unitPositionDetails.setWorkingDaysInWeek(currentEmploymentLine.getWorkingDaysInWeek());
        unitPositionDetails.setAvgDailyWorkingHours(currentEmploymentLine.getAvgDailyWorkingHours());
        unitPositionDetails.setHourlyCost(currentEmploymentLine.getHourlyCost());
        unitPositionDetails.setPublished(unitPosition.getPublished());
        unitPositionDetails.setEditable(unitPosition.getEditable());
        unitPositionDetails.setAccumulatedTimebankMinutes(unitPosition.getAccumulatedTimebankMinutes());
        unitPositionDetails.setAccumulatedTimebankDate(unitPosition.getAccumulatedTimebankDate());
        return unitPositionDetails;
    }

    public static void convertStaffUnitPositionObject(StaffUnitPositionDetails unitPosition, com.kairos.dto.activity.shift.StaffUnitPositionDetails unitPositionDetails,List<ExpertisePlannedTimeQueryResult> expertisePlannedTimes ) {
        unitPositionDetails.setExpertise(ObjectMapperUtils.copyPropertiesByMapper(unitPosition.getExpertise(), com.kairos.dto.activity.shift.Expertise.class));
        unitPositionDetails.setStaff(ObjectMapperUtils.copyPropertiesByMapper(unitPosition.getStaff(), com.kairos.dto.user.staff.staff.Staff.class));
        EmploymentLinesQueryResult currentEmploymentLine = ObjectMapperUtils.copyPropertiesByMapper(unitPosition.getEmploymentLines().get(0), EmploymentLinesQueryResult.class);
        com.kairos.dto.activity.shift.EmploymentType employmentType =ObjectMapperUtils.copyPropertiesByMapper(currentEmploymentLine.getEmploymentType(), com.kairos.dto.activity.shift.EmploymentType.class);
        Optional<ExpertisePlannedTimeQueryResult> plannedTimeQueryResult=expertisePlannedTimes.stream().filter(
                current -> current.getEmploymentTypes().stream()
                        .anyMatch(employmentTypeOfExpertise -> employmentType.getId().equals(employmentTypeOfExpertise.getId()))).findAny();
        if (plannedTimeQueryResult.isPresent()){
            unitPositionDetails.setExcludedPlannedTime(plannedTimeQueryResult.get().getExcludedPlannedTime());
            unitPositionDetails.setIncludedPlannedTime(plannedTimeQueryResult.get().getIncludedPlannedTime());
        }
        unitPositionDetails.setEmploymentType(employmentType);
        unitPositionDetails.setId(unitPosition.getId());
        unitPositionDetails.setStartDate(unitPosition.getStartDate());
        unitPositionDetails.setAppliedFunctions(unitPosition.getAppliedFunctions());
        unitPositionDetails.setEndDate(unitPosition.getEndDate());
        unitPositionDetails.setFullTimeWeeklyMinutes(currentEmploymentLine.getFullTimeWeeklyMinutes());
        unitPositionDetails.setTotalWeeklyMinutes(currentEmploymentLine.getTotalWeeklyHours()*60+currentEmploymentLine.getTotalWeeklyMinutes());
        unitPositionDetails.setWorkingDaysInWeek(currentEmploymentLine.getWorkingDaysInWeek());
        unitPositionDetails.setAvgDailyWorkingHours(currentEmploymentLine.getAvgDailyWorkingHours());

    }

}
