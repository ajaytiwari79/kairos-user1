package com.kairos.dto.activity.activity;

import com.kairos.commons.planning_setting.ConstraintSetting;
import com.kairos.enums.constraint.ConstraintSubType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
public class ActivityConstraintDTO {

    private BigInteger activityId;
    private ConstraintSetting constraintSetting;
    private ConstraintSubType constraintSubType;
    private Boolean mandatory;

    public ActivityConstraintDTO(ConstraintSetting constraintSetting, ConstraintSubType constraintSubType){
        this.constraintSetting = constraintSetting;
        this.constraintSubType = constraintSubType;
    }
}
