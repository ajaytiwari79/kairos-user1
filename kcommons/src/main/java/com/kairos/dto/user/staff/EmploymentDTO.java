package com.kairos.dto.user.staff;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kairos.dto.user.employment.EmploymentLinesDTO;
import com.kairos.enums.EmploymentSubType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by vipul on 5/3/18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class EmploymentDTO {
    private Long id;
    private Long expertiseId;
    private String expertiseName;
    private Long startDateMillis;
    private Long endDateMillis;
    private Long lastWorkingDateMillis;
    private int totalWeeklyMinutes;
    private int fullTimeWeeklyMinutes;
    private LocalDate startDate;
    private ExpertiseDTO expertise;
    private float avgDailyWorkingHours;
    private int workingDaysInWeek;
    private float hourlyCost;
    private boolean published;
    private boolean nightWorker;
    private float salary;
    private Long timeCareExternalId;
    private EmploymentSubType employmentSubType;
    private List<EmploymentLinesDTO> employmentLinesDTOS;
    private List<EmploymentLinesDTO> employmentLines;

    @Setter
    @Getter
    @NoArgsConstructor
    class ExpertiseDTO {
        private Long id;
        private String name;
        private String startDate;
        private String endDate;
    }
}
