package com.kairos.service.shift;

import com.kairos.enums.shift.ViewType;
import com.kairos.persistence.model.common.MongoBaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document
@Getter
@Setter
@NoArgsConstructor
public class ActivityCardInformation extends MongoBaseEntity {
    private boolean countryAdminSetting;
    private Long unitId;
    private Long staffId;
    private Set<ActivityCardInformationSetting> activityCardInformationSettings;
    private ViewType viewType;
    private int maxLimit;




    enum ActivityCardInformationSetting{
        STATUS,PRIORITY,ESCALATION,CHILD,BREAK_TIME,RESTING_HOURS,STOPBRICK
    }
}
