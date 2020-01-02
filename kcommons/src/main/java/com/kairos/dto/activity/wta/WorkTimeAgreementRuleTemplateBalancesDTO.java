package com.kairos.dto.activity.wta;

import com.kairos.dto.activity.activity.activity_tabs.CutOffIntervalUnit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkTimeAgreementRuleTemplateBalancesDTO {

    private BigInteger activityId;
    private String name;
    private String timeTypeColor;
    private List<IntervalBalance> intervalBalances;
    private CutOffIntervalUnit cutOffIntervalUnit;
    boolean borrowLeave;
}
