package com.kairos.dto.activity.wta;

import com.kairos.enums.TimeTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.ObjectUtils.isNullOrElse;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkTimeAgreementBalance {

    private List<WorkTimeAgreementRuleTemplateBalancesDTO> workTimeAgreementRuleTemplateBalances;

    public List<WorkTimeAgreementRuleTemplateBalancesDTO> getWorkTimeAgreementRuleTemplateBalances() {
//        Map<String,List<WorkTimeAgreementRuleTemplateBalancesDTO>> workTimeAgreementBalanceDtoListMap =workTimeAgreementRuleTemplateBalances.stream().collect(Collectors.groupingBy(WorkTimeAgreementRuleTemplateBalancesDTO::getTimeType,Collectors.toList()));
//        List<WorkTimeAgreementRuleTemplateBalancesDTO> workTimeAgreementRuleTemplateBalancesDTOS = new ArrayList<>();
        return isNullOrElse(workTimeAgreementRuleTemplateBalances,new ArrayList());
    }

}
