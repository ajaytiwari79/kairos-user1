package com.kairos.service.counter;

import com.kairos.commons.utils.DateTimeInterval;
import com.kairos.dto.activity.shift.AuditShiftDTO;
import com.kairos.dto.activity.shift.ShiftWithActivityDTO;
import com.kairos.dto.user.country.agreement.cta.cta_response.CountryHolidayCalenderDTO;
import com.kairos.dto.user.country.tag.TagDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.DateUtils.*;
import static com.kairos.commons.utils.ObjectUtils.isCollectionNotEmpty;
import static com.kairos.commons.utils.ObjectUtils.isNull;
import static com.kairos.dto.activity.counter.enums.XAxisConfig.HOURS;
import static com.kairos.enums.shift.ShiftType.PRESENCE;

@Service
public class KPICalculationHelperService {

    public int calculateCareBubble(KPIBuilderCalculationService.KPICalculationRelatedInfo kpiCalculationRelatedInfo, DateTimeInterval dateTimeInterval, Long staffId) {
        int calculateValue = 0;
        List<AuditShiftDTO> shifts = kpiCalculationRelatedInfo.getShiftAuditByStaffIdAndInterval(staffId, dateTimeInterval);
        Optional<TagDTO> optionalTagDTO = kpiCalculationRelatedInfo.getStaffKPIFilterDTO(staffId).stream().flatMap(staffKpiFilterDTO -> staffKpiFilterDTO.getTags().stream()).filter(tagDTO -> kpiCalculationRelatedInfo.getTagIds().contains(tagDTO.getId())).findFirst();
        if (optionalTagDTO.isPresent()) {
            DateTimeInterval interval = optionalTagDTO.get().getOverlapInterval(dateTimeInterval);
            for (AuditShiftDTO shift : shifts) {
                if (shift.isChanged() && interval.contains(shift.getActivities().get(0).getStartDate())) {
                    calculateValue += HOURS.equals(kpiCalculationRelatedInfo.getXAxisConfigs().get(0)) ? shift.getChangedHours() : 1;
                }
            }
        }
        return HOURS.equals(kpiCalculationRelatedInfo.getXAxisConfigs().get(0)) ? getHourByMinutes(calculateValue) : calculateValue;
    }

    public long getWorkedOnPublicHolidayCount(Long staffId, DateTimeInterval dateTimeInterval, KPIBuilderCalculationService.KPICalculationRelatedInfo kpiCalculationRelatedInfo) {
        int workedOnPublicHolidayCount = 0;
        List<ShiftWithActivityDTO> shiftWithActivityDTOS = kpiCalculationRelatedInfo.getShiftsByStaffIdAndInterval(staffId, dateTimeInterval, false);
        shiftWithActivityDTOS = shiftWithActivityDTOS.stream().filter(shift -> PRESENCE.equals(shift.getShiftType())).collect(Collectors.toList());
        if (isCollectionNotEmpty(kpiCalculationRelatedInfo.getHolidayCalenders()) && isCollectionNotEmpty(shiftWithActivityDTOS)) {
            List<ShiftWithActivityDTO> shiftsInHoliday = shiftWithActivityDTOS.stream().filter(shift -> shiftInHoliday(shift, kpiCalculationRelatedInfo.getHolidayCalenders())).collect(Collectors.toList());
            workedOnPublicHolidayCount = shiftsInHoliday.size();
        }
        return workedOnPublicHolidayCount;
    }

    private boolean shiftInHoliday(ShiftWithActivityDTO shift, List<CountryHolidayCalenderDTO> holidayCalenders) {
        for (CountryHolidayCalenderDTO holidayCalender : holidayCalenders) {
            if (holidayCalender.getHolidayDate().isEqual(asLocalDate(shift.getStartDate())) &&
                    (isNull(holidayCalender.getStartTime()) ||
                            (!holidayCalender.getStartTime().isAfter(asLocalTime(shift.getStartDate())) && !holidayCalender.getEndTime().isBefore(asLocalTime(shift.getStartDate()))))
            ) {
                return true;
            }
        }
        return false;
    }
}