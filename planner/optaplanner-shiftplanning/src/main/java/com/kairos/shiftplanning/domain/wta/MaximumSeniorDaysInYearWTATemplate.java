package com.kairos.shiftplanning.domain.wta;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.shiftplanning.constraints.ScoreLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Created by Pradeep singh on 5/8/17.
 * TEMPLATE20
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MaximumSeniorDaysInYearWTATemplate implements ConstraintHandler {

    private long intervalLength;
    private String intervalUnit;
    private long validationStartDateMillis;
    private long daysLimit;
    private String activityCode;
    private int weight;
    private ScoreLevel level;
    private String templateType;


}
