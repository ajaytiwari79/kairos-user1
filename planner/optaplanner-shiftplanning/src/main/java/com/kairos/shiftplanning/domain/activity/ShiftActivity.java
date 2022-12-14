package com.kairos.shiftplanning.domain.activity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kairos.commons.utils.DateTimeInterval;
import com.kairos.enums.cta.AccountType;
import com.kairos.enums.shift.ShiftStatus;
import com.kairos.shiftplanning.domain.shift.PlannedTime;
import com.kairos.shiftplanning.domain.staff.CTARuleTemplate;
import com.kairos.shiftplanning.domain.staff.PayoutDistribution;
import com.kairos.shiftplanning.domain.staff.TimeBankDistribution;
import com.kairos.shiftplanning.utils.ShiftPlanningUtility;
import com.kairos.shiftplanning.utils.ZonedDateTimeDeserializer;
import lombok.*;

import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.*;

import static com.kairos.commons.utils.ObjectUtils.isNullOrElse;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
@EqualsAndHashCode
public class ShiftActivity implements Comparable<ShiftActivity>{
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime startDate;
    private Activity activity;
    private BigInteger activityId;
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime endDate;
    private List<PlannedTime> plannedTimes;
    private int scheduledMinutes;
    private int durationMinutes;
    @Builder.Default
    private Set<ShiftStatus> status = new HashSet<>();
    private boolean breakNotHeld;
    @Builder.Default
    private List<TimeBankDistribution> timeBankDistributions = new ArrayList<>();
    @Builder.Default
    private List<PayoutDistribution> payoutDistributions = new ArrayList<>();
    private int totalTimebankBonus;
    private int totalPayoutBonus;
    private int scheduledMinutesOfTimebank;
    private int scheduledMinutesOfPayout;
    private int plannedMinutesOfTimebank;
    private int plannedMinutesOfPayout;

    public List<TimeBankDistribution> getTimeBankDistributions() {
        this.timeBankDistributions = isNullOrElse(timeBankDistributions,new ArrayList<>());
        return this.timeBankDistributions;
    }

    public List<PayoutDistribution> getPayoutDistributions() {
        this.payoutDistributions = isNullOrElse(payoutDistributions,new ArrayList<>());
        return this.payoutDistributions;
    }

    public boolean isChanged(ShiftActivity shiftActivity){
        return !this.startDate.equals(shiftActivity.startDate) || !this.endDate.equals(shiftActivity.endDate) || !activity.getId().equals(shiftActivity.getActivity().getId());
    }

    public DateTimeInterval getInterval() {
        return new DateTimeInterval(this.startDate,this.endDate);
    }

    @Override
    public String toString() {
        return activity.getName() + "-" + getIntervalAsString()+" - "+ plannedTimes.toString();
    }

    public String getIntervalAsString() {
        return ShiftPlanningUtility.getIntervalAsString(getInterval());
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    @Override
    public int compareTo(ShiftActivity shiftActivity) {
        return this.startDate.compareTo(shiftActivity.startDate);
    }
    
    public void updateTimeBankOrPayoutBonus(CTARuleTemplate ctaRuleTemplate,int value){
        if(AccountType.TIMEBANK_ACCOUNT.equals(ctaRuleTemplate.getPlannedTimeWithFactor().getAccountType())){
            Optional<TimeBankDistribution> optionalTimeBankDistributionDTO = getTimeBankDistributions().stream().filter(distributionDTO -> distributionDTO.getCtaRuleTemplateId().equals(ctaRuleTemplate.getId())).findAny();
            if (optionalTimeBankDistributionDTO.isPresent()) {
                optionalTimeBankDistributionDTO.get().setMinutes(optionalTimeBankDistributionDTO.get().getMinutes() + value);
            } else {
                TimeBankDistribution timeBankDistribution = new TimeBankDistribution(ctaRuleTemplate.getName(), ctaRuleTemplate.getId(), value);
                getTimeBankDistributions().add(timeBankDistribution);
            }
            totalTimebankBonus+=value;
            plannedMinutesOfTimebank+=value;
        }else {
            Optional<PayoutDistribution> optionalPayoutDistribution = getPayoutDistributions().stream().filter(distributionDTO -> distributionDTO.getCtaRuleTemplateId().equals(ctaRuleTemplate.getId())).findAny();
            if (optionalPayoutDistribution.isPresent()) {
                optionalPayoutDistribution.get().setMinutes(optionalPayoutDistribution.get().getMinutes() + value);
            } else {
                PayoutDistribution payoutDistribution = new PayoutDistribution(ctaRuleTemplate.getName(), ctaRuleTemplate.getId(), value);
                getPayoutDistributions().add(payoutDistribution);
            }
            totalPayoutBonus+=value;
            plannedMinutesOfPayout+=value;
        }
    }
}
