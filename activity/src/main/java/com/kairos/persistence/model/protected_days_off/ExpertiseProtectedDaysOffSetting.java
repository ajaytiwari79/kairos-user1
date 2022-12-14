package com.kairos.persistence.model.protected_days_off;

import com.kairos.enums.protected_days_off.HolidayType;
import com.kairos.persistence.model.common.MongoBaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class ExpertiseProtectedDaysOffSetting extends MongoBaseEntity {

    private Long countryId;
    private Long unitId;
    private Long expertiseId;
    private Set<HolidayType> includeHolidayType;
}
