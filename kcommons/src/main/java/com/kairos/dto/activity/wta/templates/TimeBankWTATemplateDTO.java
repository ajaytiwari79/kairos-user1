package com.kairos.dto.activity.wta.templates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.dto.activity.wta.basic_details.WTABaseRuleTemplateDTO;
import com.kairos.enums.TimeBankLimitsType;
import com.kairos.enums.wta.MinMaxSetting;
import com.kairos.enums.wta.WTATemplateType;
import lombok.Getter;
import lombok.Setter;


/**
 * Created by pavan on 20/2/18.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.ALWAYS)
@Getter
@Setter
public class TimeBankWTATemplateDTO extends WTABaseRuleTemplateDTO {

    private float recommendedValue;
    private MinMaxSetting minMaxSetting;
    private boolean staffCanIgnoreForWeeklyEmployment;
    private boolean managementCanIgnoreForWeeklyEmployment;
    private int factorOfWeeklyEmploymentForStaff;
    private int factorOfWeeklyEmploymentForManagement;
    private TimeBankLimitsType timeBankLimitsType;

    public TimeBankWTATemplateDTO() {
        this.wtaTemplateType = WTATemplateType.TIME_BANK;
    }
}
