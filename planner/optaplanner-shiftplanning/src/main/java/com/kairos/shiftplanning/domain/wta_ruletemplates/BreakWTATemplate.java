package com.kairos.shiftplanning.domain.wta_ruletemplates;
/*
 *Created By Pavan on 25/10/18
 *
 */

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.dto.activity.wta.templates.BreakAvailabilitySettings;
import com.kairos.enums.wta.WTATemplateType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class BreakWTATemplate extends WTABaseRuleTemplate {

    private short breakGapMinutes;
    private Set<BreakAvailabilitySettings> breakAvailability;

    public BreakWTATemplate(String name, String description, short breakGapMinutes, Set<BreakAvailabilitySettings> breakAvailability) {
        super(name, description);
        this.breakGapMinutes = breakGapMinutes;
        this.breakAvailability = breakAvailability;
    }

    public BreakWTATemplate() {
        this.wtaTemplateType = WTATemplateType.WTA_FOR_BREAKS_IN_SHIFT;
    }


}
