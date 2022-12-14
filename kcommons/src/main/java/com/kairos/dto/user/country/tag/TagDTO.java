package com.kairos.dto.user.country.tag;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.commons.utils.DateTimeInterval;
import com.kairos.enums.MasterDataTypeEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import static com.kairos.commons.utils.ObjectUtils.isNull;

/**
 * Created by prerna on 10/11/17.
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
public class TagDTO  implements Serializable {

    private Long id;
    private String name;

    private MasterDataTypeEnum masterDataType;

    private boolean countryTag;

    private long countryId;

    private long organizationId;

    private Long orgTypeId;

    private List<Long> orgSubTypeIds;

    private PenaltyScoreDTO penaltyScore;

    private String color;

    private String shortName;
    private String ultraShortName;
    private Date startDate;
    private Date endDate;

    public TagDTO(String name, MasterDataTypeEnum masterDataType){
        this.name = name;
        this.masterDataType = masterDataType;
    }

    public TagDTO(Long id, String name, MasterDataTypeEnum masterDataType){
        this.id = id;
        this.name = name;
        this.masterDataType = masterDataType;
    }

    public DateTimeInterval getOverlapInterval(DateTimeInterval dateTimeInterval){
        Date intervalStartDate = isNull(this.startDate) ? dateTimeInterval.getStartDate() : this.startDate.before(startDate) ? dateTimeInterval.getStartDate() : this.startDate;
        Date intervalEndDate = isNull(this.endDate) ? dateTimeInterval.getEndDate() : this.endDate.after(endDate) ? dateTimeInterval.getEndDate() : this.endDate;
        return new DateTimeInterval(intervalStartDate,intervalEndDate);
    }


}
