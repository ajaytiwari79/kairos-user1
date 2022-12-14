package com.kairos.persistence.model.wta.templates.template_types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.commons.config.ApplicationContextProviderNonManageBean;
import com.kairos.commons.utils.DateTimeInterval;
import com.kairos.dto.activity.activity.activity_tabs.CutOffIntervalUnit;
import com.kairos.dto.activity.shift.ShiftWithActivityDTO;
import com.kairos.dto.activity.shift.WorkTimeAgreementRuleViolation;
import com.kairos.dto.activity.wta.templates.ActivityCutOffCount;
import com.kairos.dto.user.expertise.CareDaysDTO;
import com.kairos.enums.DurationType;
import com.kairos.enums.TimeTypeEnum;
import com.kairos.enums.shift.ShiftOperationType;
import com.kairos.enums.wta.WTATemplateType;
import com.kairos.persistence.model.activity.ActivityWrapper;
import com.kairos.persistence.model.wta.templates.WTABaseRuleTemplate;
import com.kairos.service.wta.WorkTimeAgreementBalancesCalculationService;
import com.kairos.wrapper.wta.RuleTemplateSpecificInfo;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.DateUtils.asDate;
import static com.kairos.commons.utils.DateUtils.asLocalDate;
import static com.kairos.commons.utils.ObjectUtils.isCollectionNotEmpty;
import static com.kairos.commons.utils.ObjectUtils.isNotNull;
import static com.kairos.utils.worktimeagreement.RuletemplateUtils.getCareDays;

/**
 * Created by pavan on 23/4/18.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ChildCareDaysCheckWTATemplate extends WTABaseRuleTemplate {
    private List<BigInteger> activityIds = new ArrayList<>();
    private float recommendedValue;
    private CutOffIntervalUnit cutOffIntervalUnit;
    private int transferLeaveCount;
    private int borrowLeaveCount;
    private List<ActivityCutOffCount> activityCutOffCounts = new ArrayList<>();
    private transient DateTimeInterval interval;


    public ChildCareDaysCheckWTATemplate() {
        this.wtaTemplateType = WTATemplateType.CHILD_CARE_DAYS_CHECK;
    }

    @Override
    public void validateRules(RuleTemplateSpecificInfo infoWrapper) {
        if (!isDisabled() && validateRulesChildCareDayCheck(infoWrapper.getActivityWrapperMap()) && CollectionUtils.containsAny(activityIds,infoWrapper.getShift().getActivityIds()) && !ShiftOperationType.DELETE.equals(infoWrapper.getShiftOperationType())) {
            WorkTimeAgreementBalancesCalculationService workTimeAgreementService= ApplicationContextProviderNonManageBean.getApplicationContext().getBean(WorkTimeAgreementBalancesCalculationService.class);
            if (isCollectionNotEmpty(infoWrapper.getChildCareDays())) {
                long leaveCount = calculateChildCareDaysLeaveCount(infoWrapper.getChildCareDays(), infoWrapper.getStaffChildAges());
                if (isNotNull(interval)) {
                    List<ShiftWithActivityDTO> shifts = infoWrapper.getShifts().stream().filter(shift -> CollectionUtils.containsAny(shift.getActivityIds(), activityIds) && interval.contains(shift.getStartDate())).collect(Collectors.toList());
                    ActivityCutOffCount activityLeaveCount = this.getActivityCutOffCounts().stream().filter(activityCutOffCount -> new DateTimeInterval(activityCutOffCount.getStartDate(), activityCutOffCount.getEndDate()).containsAndEqualsEndDate(asDate(asLocalDate(infoWrapper.getShift().getStartDate())))).findFirst().orElse(new ActivityCutOffCount());
                    if (leaveCount + activityLeaveCount.getTransferLeaveCount() - activityLeaveCount.getBorrowLeaveCount() < (shifts.size() + 1)) {
                        boolean isLeaveAvailable = workTimeAgreementService.isLeaveCountAvailable(infoWrapper.getActivityWrapperMap().get(activityIds.get(0)).getActivity(), infoWrapper.getShift(), interval, infoWrapper.getLastPlanningPeriodEndDate(), this, leaveCount,infoWrapper.getStaffAge(),infoWrapper.getSeniorCareDays(),infoWrapper.getChildCareDays());
                        if (!isLeaveAvailable) {
                            WorkTimeAgreementRuleViolation workTimeAgreementRuleViolation =
                                    new WorkTimeAgreementRuleViolation(this.id, this.name, null, true, false, null,
                                            DurationType.DAYS.toValue(), String.valueOf(leaveCount));
                            infoWrapper.getViolatedRules().getWorkTimeAgreements().add(workTimeAgreementRuleViolation);
                        }
                    }
                }
            }
        }

    }


    private boolean validateRulesChildCareDayCheck(Map<BigInteger, ActivityWrapper> activityWrapperMap) {
        for(BigInteger activityId : activityWrapperMap.keySet()){
            if(!TimeTypeEnum.PAID_BREAK.equals(activityWrapperMap.get(activityId).getTimeTypeInfo().getSecondLevelType()) && isNotNull(activityWrapperMap.get(activityId).getActivity().getActivityRulesSettings().getCutOffIntervalUnit())){
                return true;
            }
        }
        return false;
    }

    public long calculateChildCareDaysLeaveCount(List<CareDaysDTO> careDaysDTOS, List<Integer> staffChildAges){
        long leaveCount = 0L;
        if (isCollectionNotEmpty(staffChildAges)) {
            for (Integer staffChildAge : staffChildAges) {
                CareDaysDTO careDaysDTO = getCareDays(careDaysDTOS,staffChildAge);
                if(isNotNull(careDaysDTO)){
                    leaveCount+=careDaysDTO.getLeavesAllowed();
                }
            }
        }
        return leaveCount;
    }

    public ChildCareDaysCheckWTATemplate(String name, boolean disabled, String description) {
        super(name, description);
        this.wtaTemplateType = WTATemplateType.CHILD_CARE_DAYS_CHECK;
        this.disabled = disabled;
    }

    @Override
    public boolean isCalculatedValueChanged(WTABaseRuleTemplate wtaBaseRuleTemplate) {
        ChildCareDaysCheckWTATemplate childCareDaysCheckWTATemplate = (ChildCareDaysCheckWTATemplate) wtaBaseRuleTemplate;
        return (this != childCareDaysCheckWTATemplate) && !(
                Float.compare(childCareDaysCheckWTATemplate.recommendedValue, recommendedValue) == 0 &&
                        Objects.equals(activityIds, childCareDaysCheckWTATemplate.activityIds) &&
                        cutOffIntervalUnit == childCareDaysCheckWTATemplate.cutOffIntervalUnit && Objects.equals(this.phaseTemplateValues,childCareDaysCheckWTATemplate.phaseTemplateValues));
    }

}
