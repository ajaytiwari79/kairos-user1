package com.kairos.persistence.model.wta.templates.template_types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

import com.kairos.persistence.model.wta.templates.WTABaseRuleTemplate;
import static com.kairos.enums.wta.WTATemplateType.PROTECTED_DAYS_OFF;

/**
 * Created by pradeep
 * Created at 29/7/19
 **/

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ProtectedDaysOffWTATemplate extends WTABaseRuleTemplate {
    private BigInteger activityId;

    public ProtectedDaysOffWTATemplate() {
        this.wtaTemplateType = PROTECTED_DAYS_OFF;
    }



}
