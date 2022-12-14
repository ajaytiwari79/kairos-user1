package com.kairos.dto.user.country.experties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.enums.DurationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by pavan on 26/4/18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class AgeRangeDTO implements Comparable<AgeRangeDTO>,Serializable{
    private Long id;
    private int from;
    private Integer to;
    private Integer leavesAllowed;
    //For Groping of staff
    private DurationType durationType;

    @Override
    public int compareTo(AgeRangeDTO o) {
        return this.from-o.from;
    }

    public AgeRangeDTO(int from, Integer to, DurationType durationType){
        this.from = from;
        this.to = to;
        this.durationType = durationType;
    }
}
