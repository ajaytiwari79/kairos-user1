package com.kairos.persistence.model.shift;

import com.kairos.enums.shift.TodoStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.Date;

/**
 * Created by pradeep
 * Created at 13/6/19
 **/
@Getter
@Setter
public class RequestAbsence {
    private BigInteger shiftId;
    private BigInteger activityId;
    private String activityName;
    private Date startDate;
    private Date endDate;
    private TodoStatus todoStatus;
    private Long reasonCodeId;
    private String remarks;
    private String methodForCalculatingTime;
}
