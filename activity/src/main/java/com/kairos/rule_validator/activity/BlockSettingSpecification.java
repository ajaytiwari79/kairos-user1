package com.kairos.rule_validator.activity;

import com.kairos.commons.utils.ObjectUtils;
import com.kairos.dto.activity.shift.BlockSettingDTO;
import com.kairos.dto.activity.shift.ShiftWithActivityDTO;
import com.kairos.rule_validator.AbstractSpecification;
import com.kairos.rule_validator.RuleExecutionType;
import com.kairos.service.shift.ShiftValidatorService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.DateUtils.asDate;
import static com.kairos.commons.utils.ObjectUtils.isNotNull;
import static com.kairos.constants.ActivityMessagesConstants.MESSAGE_BLOCKED_FOR_SHIFT_ENTER_AT_DATE;

/**
 * Created By G.P.Ranjan on 3/12/19
 **/
@NoArgsConstructor
@Getter
@Setter
public class BlockSettingSpecification extends AbstractSpecification<ShiftWithActivityDTO> {

    private BlockSettingDTO blockSetting;

    public BlockSettingSpecification(BlockSettingDTO blockSetting) {
        this.blockSetting = blockSetting;
    }

    @Override
    public boolean isSatisfied(ShiftWithActivityDTO shift) {
        return true;
    }

    @Override
    public void validateRules(ShiftWithActivityDTO shift, RuleExecutionType ruleExecutionType) {
        Set<BigInteger> activityIds = shift.getActivities().stream().map(activity->activity.getActivityId()).collect(Collectors.toSet());
        if(isNotNull(blockSetting) && isNotNull(blockSetting.getBlockDetails()) && CollectionUtils.containsAny(blockSetting.getBlockDetails().get(shift.getStaffId()), activityIds)){
            ShiftValidatorService.throwException(MESSAGE_BLOCKED_FOR_SHIFT_ENTER_AT_DATE,asDate(blockSetting.getDate()));
        }
    }

    @Override
    public List<String> isSatisfiedString(ShiftWithActivityDTO shiftWithActivityDTO) {
        return Collections.emptyList();
    }
}