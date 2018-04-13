package com.kairos.activity.persistence.model.wta.templates.template_types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.activity.persistence.enums.WTATemplateType;
import com.kairos.activity.persistence.model.wta.templates.WTABaseRuleTemplate;
import org.springframework.data.mongodb.core.mapping.Document;


import java.math.BigInteger;
import java.util.List;

/**
 * Created by pawanmandhan on 5/8/17.
 * TEMPLATE5
 */
@Document
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShiftLengthWTATemplate extends WTABaseRuleTemplate {

    private long timeLimit;
    private boolean checkAgainstTimeRules;
    private WTATemplateType wtaTemplateType = WTATemplateType.SHIFT_LENGTH;;
    private List<BigInteger> dayTypes;



    public List<BigInteger> getDayTypes() {
        return dayTypes;
    }

    public void setDayTypes(List<BigInteger> dayTypes) {
        this.dayTypes = dayTypes;
    }

    public WTATemplateType getWtaTemplateType() {
        return wtaTemplateType;
    }

    public void setWtaTemplateType(WTATemplateType wtaTemplateType) {
        this.wtaTemplateType = wtaTemplateType;
    }
    public long getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(long timeLimit) {
        this.timeLimit = timeLimit;
    }

    public boolean isCheckAgainstTimeRules() {
        return checkAgainstTimeRules;
    }

    public void setCheckAgainstTimeRules(boolean checkAgainstTimeRules) {
        this.checkAgainstTimeRules = checkAgainstTimeRules;
    }

    public ShiftLengthWTATemplate() {

    }

    public ShiftLengthWTATemplate(String name, boolean minimum, String description, long timeLimit, boolean checkAgainstTimeRules, List<BigInteger> dayTypes) {
        super(name, minimum, description);
        this.timeLimit = timeLimit;
        this.checkAgainstTimeRules = checkAgainstTimeRules;
        this.dayTypes = dayTypes;
    }
}