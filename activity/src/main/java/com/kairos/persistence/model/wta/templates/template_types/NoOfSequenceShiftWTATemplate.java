package com.kairos.persistence.model.wta.templates.template_types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.commons.utils.DateTimeInterval;
import com.kairos.commons.utils.DateUtils;
import com.kairos.commons.utils.TimeInterval;
import com.kairos.dto.activity.shift.ShiftWithActivityDTO;
import com.kairos.dto.user.country.time_slot.TimeSlot;
import com.kairos.enums.DurationType;
import com.kairos.enums.wta.PartOfDay;
import com.kairos.enums.wta.WTATemplateType;
import com.kairos.persistence.model.wta.templates.WTABaseRuleTemplate;
import com.kairos.wrapper.wta.RuleTemplateSpecificInfo;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import java.math.BigInteger;
import java.time.Period;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.DateUtils.asLocalDate;
import static com.kairos.commons.utils.ObjectUtils.isNotNull;
import static com.kairos.commons.utils.ObjectUtils.newArrayList;
import static com.kairos.enums.wta.MinMaxSetting.MAXIMUM;
import static com.kairos.utils.worktimeagreement.RuletemplateUtils.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class NoOfSequenceShiftWTATemplate extends WTABaseRuleTemplate{

    //private int sequence;
    @Positive
    private long intervalLength;
    @NotEmpty(message = "message.ruleTemplate.interval.notNull")
    private String intervalUnit;
    private boolean restingTimeAllowed;
    private int restingTime;
    private PartOfDay sequenceShiftFrom;
    private PartOfDay sequenceShiftTo;
    private transient DateTimeInterval interval;

    private List<BigInteger> timeTypeIds = new ArrayList<>();

    public NoOfSequenceShiftWTATemplate() {
        wtaTemplateType=WTATemplateType.NO_OF_SEQUENCE_SHIFT;
    }

    @Override
    public void validateRules(RuleTemplateSpecificInfo infoWrapper) {
        if(!isDisabled() && CollectionUtils.containsAny(timeTypeIds,infoWrapper.getShift().getActivitiesTimeTypeIds())){
            TimeSlot timeSlotWrapper = getTimeSlotWrapper(infoWrapper, infoWrapper.getShift());
            if(isNotNull(timeSlotWrapper)) {
                Integer[] limitAndCounter = getValueByPhaseAndCounter(infoWrapper, getPhaseTemplateValues(), this);
                boolean isValid = getOccurrencesSequenceShift(infoWrapper,limitAndCounter[0]);
                brakeRuleTemplateAndUpdateViolationDetails(infoWrapper,limitAndCounter[1],isValid, this,
                        limitAndCounter[2], DurationType.DAYS.toValue(),String.valueOf(limitAndCounter[0]));
            }
        }
    }

    private boolean getOccurrencesSequenceShift(RuleTemplateSpecificInfo infoWrapper,int value){
        int totalOccurrencesSequenceShift = 0;
        List<ShiftWithActivityDTO> shifts = infoWrapper.getShifts();
        shifts.add(infoWrapper.getShift());
        shifts = infoWrapper.getShifts().stream().sorted(Comparator.comparing(k->k.getStartDate())).collect(Collectors.toList());
        for(int i=0; i<shifts.size()-1; i++){
            TimeSlot timeSlot = getTimeSlotWrapper(infoWrapper, shifts.get(i));
            TimeSlot nextTimeSlot = getTimeSlotWrapper(infoWrapper, shifts.get(i+1));
            List<PartOfDay> partOfDays = newArrayList(sequenceShiftFrom,sequenceShiftTo);
            if(partOfDays.contains(PartOfDay.valueOf(timeSlot.getName().toUpperCase())) && partOfDays.contains(PartOfDay.valueOf(nextTimeSlot.getName().toUpperCase())) && !timeSlot.getName().equals(nextTimeSlot.getName())){
                Period period = Period.between(asLocalDate(shifts.get(i).getStartDate()), asLocalDate(shifts.get(i+1).getStartDate()));
                if(period.getDays() < 2) {
                    totalOccurrencesSequenceShift++;
                    if(!isValid(MAXIMUM, value, totalOccurrencesSequenceShift)){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private TimeSlot getTimeSlotWrapper(RuleTemplateSpecificInfo infoWrapper, ShiftWithActivityDTO shift){
        TimeSlot timeSlotWrapper = null;
        for (String key : infoWrapper.getTimeSlotWrapperMap().keySet()) {
            timeSlotWrapper = infoWrapper.getTimeSlotWrapperMap().get(key);
            int endMinutesOfInterval = (timeSlotWrapper.getEndHour() * 60) + timeSlotWrapper.getEndMinute();
            int startMinutesOfInterval = (timeSlotWrapper.getStartHour() * 60) + timeSlotWrapper.getStartMinute();
            TimeInterval interval = new TimeInterval(startMinutesOfInterval, endMinutesOfInterval);
            int minuteOfTheDay = DateUtils.asZonedDateTime(shift.getStartDate()).get(ChronoField.MINUTE_OF_DAY);
            if (minuteOfTheDay == (int) interval.getStartFrom() || interval.contains(minuteOfTheDay)) {
                break;
            }else{
                timeSlotWrapper = null;
            }
        }
        return timeSlotWrapper;
    }

    public NoOfSequenceShiftWTATemplate(String name, boolean disabled, String description,  PartOfDay sequenceShiftFrom, PartOfDay sequenceShiftTo, long intervalLength, String intervalUnit) {
        this.name = name;
        this.disabled = disabled;
        this.description = description;
        this.intervalLength = intervalLength;
        this.intervalUnit = intervalUnit;
        this.wtaTemplateType = WTATemplateType.NO_OF_SEQUENCE_SHIFT;
        //this.sequence=sequence;
        this.sequenceShiftTo = sequenceShiftTo;
        this.sequenceShiftFrom = sequenceShiftFrom;
    }

    @Override
    public boolean isCalculatedValueChanged(WTABaseRuleTemplate wtaBaseRuleTemplate) {
        NoOfSequenceShiftWTATemplate noOfSequenceShiftWTATemplate = (NoOfSequenceShiftWTATemplate)wtaBaseRuleTemplate;
        return (this != noOfSequenceShiftWTATemplate) && !(restingTimeAllowed == noOfSequenceShiftWTATemplate.restingTimeAllowed &&
                restingTime == noOfSequenceShiftWTATemplate.restingTime &&
                sequenceShiftFrom == noOfSequenceShiftWTATemplate.sequenceShiftFrom &&
                sequenceShiftTo == noOfSequenceShiftWTATemplate.sequenceShiftTo &&
                Objects.equals(timeTypeIds, noOfSequenceShiftWTATemplate.timeTypeIds) && Objects.equals(this.phaseTemplateValues,noOfSequenceShiftWTATemplate.phaseTemplateValues));
    }

}
