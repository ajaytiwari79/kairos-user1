package com.kairos.rule_validator.activity;

import com.kairos.commons.utils.DateUtils;
import com.kairos.dto.activity.shift.ShiftWithActivityDTO;
import com.kairos.rule_validator.AbstractSpecification;
import com.kairos.rule_validator.RuleExecutionType;
import com.kairos.service.exception.ExceptionService;
import com.kairos.service.shift.ShiftValidatorService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.kairos.constants.ActivityMessagesConstants.MESSAGE_ACTIVITY_DAYTYPE;

/**
 * Created by oodles on 30/11/17.
 */
public class DayTypeSpecification extends AbstractSpecification<ShiftWithActivityDTO> {


    private Set<DayOfWeek> validDays;
    private Date shiftStartDateTime;
    @Autowired
    private ExceptionService exceptionService;

    public DayTypeSpecification(Set<DayOfWeek> validDays, Date shiftStartDateTime) {
        this.validDays = validDays;
        this.shiftStartDateTime = shiftStartDateTime;
    }

    @Override
    public boolean isSatisfied(ShiftWithActivityDTO shift) {
        return validDays.contains(DateUtils.asLocalDate(shiftStartDateTime).getDayOfWeek());
    }

    @Override
    public void validateRules(ShiftWithActivityDTO shift, RuleExecutionType ruleExecutionType) {
        if(!validDays.contains(DateUtils.asLocalDate(shiftStartDateTime).getDayOfWeek())){
            ShiftValidatorService.throwException(MESSAGE_ACTIVITY_DAYTYPE);
        }
    }

    @Override
    public List<String> isSatisfiedString(ShiftWithActivityDTO shift) {
        return Collections.emptyList();
    }

}
