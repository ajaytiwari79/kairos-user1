package com.kairos.persistence.model.wta.templates.template_types;

import com.kairos.commons.config.ApplicationContextProviderNonManageBean;
import com.kairos.commons.utils.DateTimeInterval;
import com.kairos.dto.activity.activity.activity_tabs.CutOffIntervalUnit;
import com.kairos.dto.activity.shift.ShiftWithActivityDTO;
import com.kairos.dto.activity.shift.WorkTimeAgreementRuleViolation;
import com.kairos.dto.activity.wta.AgeRange;
import com.kairos.dto.activity.wta.templates.ActivityCutOffCount;
import com.kairos.dto.user.expertise.CareDaysDTO;
import com.kairos.enums.DurationType;
import com.kairos.enums.shift.ShiftOperationType;
import com.kairos.enums.wta.WTATemplateType;
import com.kairos.persistence.model.wta.templates.WTABaseRuleTemplate;
import com.kairos.service.wta.WorkTimeAgreementBalancesCalculationService;
import com.kairos.wrapper.wta.RuleTemplateSpecificInfo;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.DateUtils.asDate;
import static com.kairos.commons.utils.DateUtils.asLocalDate;
import static com.kairos.commons.utils.ObjectUtils.isCollectionNotEmpty;
import static com.kairos.commons.utils.ObjectUtils.isNotNull;
import static com.kairos.utils.worktimeagreement.RuletemplateUtils.getCareDays;


/**
 * Created by pavan on 24/4/18.
 */
@Getter
@Setter
public class SeniorDaysPerYearWTATemplate extends WTABaseRuleTemplate {
    private List<AgeRange> ageRange;
    private List<BigInteger> activityIds = new ArrayList<>();
    private CutOffIntervalUnit cutOffIntervalUnit;
    private int transferLeaveCount;
    private int borrowLeaveCount;
    private float recommendedValue;
    private List<ActivityCutOffCount> activityCutOffCounts = new ArrayList<>();
    private transient DateTimeInterval interval;

    public SeniorDaysPerYearWTATemplate() {
        this.wtaTemplateType = WTATemplateType.SENIOR_DAYS_PER_YEAR;
        //Default Constructor
    }

    @Override
    public void validateRules(RuleTemplateSpecificInfo infoWrapper) {
        WorkTimeAgreementBalancesCalculationService workTimeAgreementService = ApplicationContextProviderNonManageBean.getApplicationContext().getBean(WorkTimeAgreementBalancesCalculationService.class);
        if (isCollectionNotEmpty(activityIds) && infoWrapper.getActivityWrapperMap().containsKey(activityIds.get(0)) && !isDisabled() && !ShiftOperationType.DELETE.equals(infoWrapper.getShiftOperationType())) {
            CareDaysDTO careDays = getCareDays(infoWrapper.getSeniorCareDays(), infoWrapper.getStaffAge());
            if (isNotNull(careDays)) {
                int leaveCount = careDays.getLeavesAllowed();
                if (isNotNull(interval)) {
                    List<ShiftWithActivityDTO> shifts = infoWrapper.getShifts().stream().filter(shift -> CollectionUtils.containsAny(shift.getActivityIds(), activityIds) && interval.contains(shift.getStartDate())).collect(Collectors.toList());
                    ActivityCutOffCount activityLeaveCount = this.getActivityCutOffCounts().stream().filter(activityCutOffCount -> new DateTimeInterval(activityCutOffCount.getStartDate(), activityCutOffCount.getEndDate()).containsAndEqualsEndDate(asDate(asLocalDate(infoWrapper.getShift().getStartDate())))).findFirst().orElse(new ActivityCutOffCount());
                    if (leaveCount + activityLeaveCount.getTransferLeaveCount() - activityLeaveCount.getBorrowLeaveCount() < (shifts.size() + 1)) {
                        boolean isLeaveAvailable = workTimeAgreementService.isLeaveCountAvailable(infoWrapper.getActivityWrapperMap().get(activityIds.get(0)).getActivity() , infoWrapper.getShift(), interval, infoWrapper.getLastPlanningPeriodEndDate(), this, leaveCount,infoWrapper.getStaffAge(),infoWrapper.getSeniorCareDays(),infoWrapper.getChildCareDays());
                        if (!isLeaveAvailable) {
                            WorkTimeAgreementRuleViolation workTimeAgreementRuleViolation =
                                    new WorkTimeAgreementRuleViolation(this.id, this.name, null, true, false, null,
                                            DurationType.DAYS.toValue(), String.valueOf(leaveCount));
                            infoWrapper.getViolatedRules().getWorkTimeAgreements().add(workTimeAgreementRuleViolation);
                        }
                    }
                }
            } else if (CollectionUtils.containsAny(infoWrapper.getShift().getActivityIds(), activityIds)) {
                WorkTimeAgreementRuleViolation workTimeAgreementRuleViolation =
                        new WorkTimeAgreementRuleViolation(this.id, this.name, null, true, false, null,
                                DurationType.DAYS.toValue(), String.valueOf(0));
                infoWrapper.getViolatedRules().getWorkTimeAgreements().add(workTimeAgreementRuleViolation);
            }
        }
    }


    public SeniorDaysPerYearWTATemplate(String name, boolean disabled, String description, List<AgeRange> ageRange) {
        super(name, description);
        this.disabled = disabled;
        this.ageRange = ageRange;
        this.wtaTemplateType = WTATemplateType.SENIOR_DAYS_PER_YEAR;
    }

    @Override
    public boolean isCalculatedValueChanged(WTABaseRuleTemplate wtaBaseRuleTemplate) {
        SeniorDaysPerYearWTATemplate seniorDaysPerYearWTATemplate = (SeniorDaysPerYearWTATemplate) wtaBaseRuleTemplate;
        return (this != seniorDaysPerYearWTATemplate) && !(
                Float.compare(seniorDaysPerYearWTATemplate.recommendedValue, recommendedValue) == 0 &&
                        Objects.equals(ageRange, seniorDaysPerYearWTATemplate.ageRange) &&
                        Objects.equals(activityIds, seniorDaysPerYearWTATemplate.activityIds) &&
                        cutOffIntervalUnit == seniorDaysPerYearWTATemplate.cutOffIntervalUnit && Objects.equals(this.phaseTemplateValues, seniorDaysPerYearWTATemplate.phaseTemplateValues));
    }


}
