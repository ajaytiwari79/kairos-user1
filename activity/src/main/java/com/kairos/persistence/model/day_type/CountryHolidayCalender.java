package com.kairos.persistence.model.day_type;

import com.kairos.dto.user.country.agreement.cta.cta_response.SectorWiseDayTypeInfo;
import com.kairos.enums.PublicHolidayCategory;
import com.kairos.persistence.model.common.MongoBaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigInteger;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


@Getter
@Setter
@Document
public class CountryHolidayCalender extends MongoBaseEntity {
    private static final long serialVersionUID = 2486721542637557549L;
    private String holidayTitle;
    private LocalDate holidayDate;
    private BigInteger dayTypeId;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean reOccuring;
    private String description;
    private String holidayType;
    private boolean isEnabled = true;
    private String googleCalId;
    private Long countryId;
    private List<SectorWiseDayTypeInfo> sectorWiseDayTypeInfo;
    private DayOfWeek dayOfWeek;
    private byte weekNumber;
    private String shortName;
    private PublicHolidayCategory publicHolidayCategory = PublicHolidayCategory.FIXED;
}
