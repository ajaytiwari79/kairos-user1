package com.kairos.persistence.model.activity.tabs;

import com.kairos.enums.PriorityFor;
import com.kairos.enums.TimeTypeEnum;
import com.kairos.enums.TimeTypes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

/**
 * Created by pawanmandhan on 22/8/17.
 */
@Getter
@Setter
@NoArgsConstructor
public class ActivityBalanceSettings {

    private Integer addTimeTo;
    private BigInteger timeTypeId;
    private TimeTypeEnum timeType; // This is used to verify the activity is of  paid break or unpaid break
    private boolean onCallTimePresent ;
    private Boolean negativeDayBalancePresent;
    private PriorityFor priorityFor;
    private TimeTypes timeTypes;
    private String path;
    public ActivityBalanceSettings(boolean onCallTimePresent, Boolean negativeDayBalancePresent) {
        this.onCallTimePresent = onCallTimePresent;
        this.negativeDayBalancePresent = negativeDayBalancePresent;
    }

}