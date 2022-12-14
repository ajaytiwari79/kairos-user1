package com.kairos.dto.activity.unit_settings;

import com.kairos.enums.ProtectedDaysOffUnitSettings;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDate;

/**
 * Created By G.P.Ranjan on 1/7/19
 **/
@Getter
@Setter
@NoArgsConstructor
public class ProtectedDaysOffSettingDTO implements Serializable {
    private static final long serialVersionUID = 9148594489091221237L;
    private BigInteger id;
    private Long unitId;
    private ProtectedDaysOffUnitSettings protectedDaysOffUnitSettings;
    private BigInteger holidayId;
    private LocalDate publicHolidayDate;
    private boolean protectedDaysOff;
    private BigInteger dayTypeId;
    private Long expertiseId;

    public ProtectedDaysOffSettingDTO(BigInteger id, Long unitId, ProtectedDaysOffUnitSettings protectedDaysOffUnitSettings){
        this.id=id;
        this.unitId=unitId;
        this.protectedDaysOffUnitSettings=protectedDaysOffUnitSettings;
    }

    public ProtectedDaysOffSettingDTO(Long unitId, ProtectedDaysOffUnitSettings protectedDaysOffUnitSettings){
        this.unitId=unitId;
        this.protectedDaysOffUnitSettings=protectedDaysOffUnitSettings;
    }
}
