package com.kairos.persistence.model.wta.templates.template_types;

import com.kairos.commons.utils.DateTimeInterval;
import com.kairos.dto.activity.shift.ShiftActivityDTO;
import com.kairos.dto.activity.shift.WorkTimeAgreementRuleViolation;
import com.kairos.dto.activity.wta.templates.ActivityCareDayCount;
import com.kairos.enums.wta.WTATemplateType;
import com.kairos.persistence.model.activity.Activity;
import com.kairos.persistence.model.wta.templates.WTABaseRuleTemplate;
import com.kairos.wrapper.shift.ShiftWithActivityDTO;
import com.kairos.wrapper.wta.RuleTemplateSpecificInfo;
import org.apache.commons.collections.CollectionUtils;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.kairos.utils.ShiftValidatorService.*;

/**
 * @author pradeep
 * @date - 10/10/18
 */

public class WTAForCareDays extends WTABaseRuleTemplate{

    private List<ActivityCareDayCount> careDayCounts = new ArrayList<>();

    public WTAForCareDays(String name, String description) {
        super(name, description);
    }

    public WTAForCareDays() {
        wtaTemplateType = WTATemplateType.WTA_FOR_CARE_DAYS;
    }

    public List<ActivityCareDayCount> getCareDayCounts() {
        return careDayCounts;
    }

    public void setCareDayCounts(List<ActivityCareDayCount> careDayCounts) {
        this.careDayCounts = careDayCounts;
    }

    @Override
    public void validateRules(RuleTemplateSpecificInfo infoWrapper) {
        if(!isDisabled()) {
            Map<BigInteger,ActivityCareDayCount> careDayCountMap = careDayCounts.stream().collect(Collectors.toMap(ActivityCareDayCount::getActivityId,v->v));
            for (ShiftActivityDTO shiftActivityDTO : infoWrapper.getShift().getActivities()) {
                Activity activity = infoWrapper.getActivityWrapperMap().get(shiftActivityDTO.getActivityId()).getActivity();
                if(careDayCountMap.containsKey(activity.getId())) {
                    ActivityCareDayCount careDayCount = careDayCountMap.get(activity.getId());
                    List<ShiftWithActivityDTO> shifts = new ArrayList<>(infoWrapper.getShifts());
                    shifts.add(infoWrapper.getShift());
                    shifts = getShiftsByIntervalAndActivityIds(activity, infoWrapper.getShift().getStartDate(), shifts, Arrays.asList(careDayCount.getActivityId()));
                    if (careDayCount.getCount() < shifts.size()) {
                        WorkTimeAgreementRuleViolation workTimeAgreementRuleViolation = new WorkTimeAgreementRuleViolation(this.id, this.name, 0, true, false);
                        infoWrapper.getViolatedRules().getWorkTimeAgreements().add(workTimeAgreementRuleViolation);
                        break;
                    }
                }
            }
        }
    }



}
