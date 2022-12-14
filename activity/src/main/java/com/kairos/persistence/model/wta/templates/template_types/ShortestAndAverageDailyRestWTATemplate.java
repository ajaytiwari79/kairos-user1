package com.kairos.persistence.model.wta.templates.template_types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.commons.utils.DateTimeInterval;
import com.kairos.dto.activity.shift.ShiftWithActivityDTO;
import com.kairos.enums.DurationType;
import com.kairos.enums.wta.MinMaxSetting;
import com.kairos.enums.wta.WTATemplateType;
import com.kairos.persistence.model.wta.templates.WTABaseRuleTemplate;
import com.kairos.wrapper.wta.RuleTemplateSpecificInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.*;

import static com.kairos.constants.AppConstants.*;
import static com.kairos.constants.CommonConstants.CAMELCASE_DAYS;
import static com.kairos.utils.worktimeagreement.RuletemplateUtils.*;

/**
 * Created by pawanmandhan on 5/8/17.
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ShortestAndAverageDailyRestWTATemplate extends WTABaseRuleTemplate {

    @Positive(message = "message.ruleTemplate.interval.notNull")
    private long intervalLength;//
    @NotEmpty(message = "message.ruleTemplate.interval.notNull")
    private String intervalUnit;
    private float recommendedValue;
    private Set<BigInteger> plannedTimeIds = new HashSet<>();
    private Set<BigInteger> timeTypeIds = new HashSet<>();
    private transient DateTimeInterval interval;

    public ShortestAndAverageDailyRestWTATemplate(String name,  boolean disabled,
                                                  String description, long intervalLength, String intervalUnit) {
        this.name = name;
        this.disabled = disabled;
        this.description = description;
        this.intervalLength =intervalLength;
        this.intervalUnit=intervalUnit;
        wtaTemplateType = WTATemplateType.SHORTEST_AND_AVERAGE_DAILY_REST;
    }
    public ShortestAndAverageDailyRestWTATemplate() {
        this.wtaTemplateType = WTATemplateType.SHORTEST_AND_AVERAGE_DAILY_REST;
    }

    @Override
    public void validateRules(RuleTemplateSpecificInfo infoWrapper) {
        if(!isDisabled() && isValidForPhase(infoWrapper.getPhaseId(),this.phaseTemplateValues)  && CollectionUtils.containsAny(timeTypeIds,infoWrapper.getShift().getActivitiesTimeTypeIds())){
            DateTimeInterval interval = getIntervalByRuleTemplate(infoWrapper.getShift(),intervalUnit,intervalLength);
            List<ShiftWithActivityDTO> shifts = getShiftsByInterval(interval,infoWrapper.getShifts());
            List<DateTimeInterval> intervals = getIntervals(interval);
            Integer[] limitAndCounter = getValueByPhaseAndCounter(infoWrapper,phaseTemplateValues,this);
            boolean isValid = true;
            for (DateTimeInterval dateTimeInterval : intervals) {
                int totalMin = (int)dateTimeInterval.getMinutes();
                for (ShiftWithActivityDTO shift : shifts) {
                    if(dateTimeInterval.overlaps(shift.getDateTimeInterval())){
                        totalMin -= (int)dateTimeInterval.overlap(shift.getDateTimeInterval()).getMinutes();
                    }
                    if(!isValid(MinMaxSetting.MINIMUM, limitAndCounter[0], totalMin/(60*(int)dateTimeInterval.getDays()))){
                        isValid = false;
                    }
                }
                brakeRuleTemplateAndUpdateViolationDetails(infoWrapper,limitAndCounter[1],isValid, this,limitAndCounter[2], DurationType.HOURS.toValue(),getHoursByMinutes(limitAndCounter[0],this.name));
            }
        }
    }

    public List<ShiftWithActivityDTO> getShiftsByInterval(DateTimeInterval dateTimeInterval, List<ShiftWithActivityDTO> shifts) {
        List<ShiftWithActivityDTO> updatedShifts = new ArrayList<>();
        shifts.forEach(shift -> {
            boolean isValidShift = (org.apache.commons.collections.CollectionUtils.isNotEmpty(timeTypeIds) && org.apache.commons.collections.CollectionUtils.containsAny(timeTypeIds, shift.getActivitiesTimeTypeIds())) && (org.apache.commons.collections.CollectionUtils.isNotEmpty(plannedTimeIds) && org.apache.commons.collections.CollectionUtils.containsAny(plannedTimeIds, shift.getActivitiesPlannedTimeIds()));
            if (isValidShift && (dateTimeInterval.contains(shift.getStartDate()) || dateTimeInterval.getEndLocalDate().equals(shift.getEndLocalDate()))) {
                updatedShifts.add(shift);
            }
        });
        return updatedShifts;
    }


    public ZonedDateTime getNextDateOfInterval(ZonedDateTime dateTime){
        ZonedDateTime zonedDateTime = null;
        switch (intervalUnit){
            case CAMELCASE_DAYS:zonedDateTime = dateTime.plusDays(intervalLength);
                break;
            case WEEKS:zonedDateTime = dateTime.plusWeeks(intervalLength);
                break;
            case MONTHS:zonedDateTime = dateTime.plusMonths(intervalLength);
                break;
            case YEARS:zonedDateTime = dateTime.plusYears(intervalLength);
                break;
            default:
                break;
        }
        return zonedDateTime;
    }

    private List<DateTimeInterval> getIntervals(DateTimeInterval interval){
        List<DateTimeInterval> intervals = new ArrayList<>();
        ZonedDateTime nextEnd = getNextDateOfInterval(interval.getStart());
        intervals.add(new DateTimeInterval(interval.getStart(),nextEnd));
        intervals.add(new DateTimeInterval(nextEnd,getNextDateOfInterval(nextEnd)));
        return intervals;
    }

    @Override
    public boolean isCalculatedValueChanged(WTABaseRuleTemplate wtaBaseRuleTemplate) {
        ShortestAndAverageDailyRestWTATemplate shortestAndAverageDailyRestWTATemplate = (ShortestAndAverageDailyRestWTATemplate) wtaBaseRuleTemplate;
        return (this != shortestAndAverageDailyRestWTATemplate) && !(intervalLength == shortestAndAverageDailyRestWTATemplate.intervalLength &&
                Float.compare(shortestAndAverageDailyRestWTATemplate.recommendedValue, recommendedValue) == 0 &&
                Objects.equals(intervalUnit, shortestAndAverageDailyRestWTATemplate.intervalUnit) &&
                Objects.equals(plannedTimeIds, shortestAndAverageDailyRestWTATemplate.plannedTimeIds) &&
                Objects.equals(timeTypeIds, shortestAndAverageDailyRestWTATemplate.timeTypeIds) && Objects.equals(this.phaseTemplateValues,shortestAndAverageDailyRestWTATemplate.phaseTemplateValues));
    }


}
