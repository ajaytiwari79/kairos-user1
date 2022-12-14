package com.kairos.dto.activity.shift;

import com.kairos.commons.utils.DateTimeInterval;
import com.kairos.constants.CommonConstants;
import com.kairos.enums.audit_logging.LoggingType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.kairos.commons.utils.ObjectUtils.*;
import static java.lang.Math.abs;

@Getter
@Setter
public class AuditShiftDTO {
    private Date startDate;
    private Date old_startDate;
    private Date endDate;
    private Date old_endDate;
    private Long staffId;
    private List<AuditShiftActivityDTO> activities;
    private List<AuditShiftActivityDTO> old_activities;
    private LoggingType loggingType;
    private boolean management;

    public boolean isChanged(){
        boolean isChanged;
        if(!management){
            isChanged = false;
        }else {
            activities = isNullOrElse(activities, new ArrayList<>());
            old_activities = isNullOrElse(old_activities, new ArrayList<>());
            isChanged = newHashSet(LoggingType.DELETED,LoggingType.CREATED).contains(this.loggingType) || activities.size() != old_activities.size();
            for (int i = 0; i < activities.size(); i++) {
                if (!isChanged) {
                    AuditShiftActivityDTO activityDTO = activities.get(i);
                    AuditShiftActivityDTO shiftActivityDTO = old_activities.get(i);
                    isChanged = activityDTO.isChanged(shiftActivityDTO);
                }
            }
        }
        return isChanged;
    }

    public int getChangedHours() {
        Collections.sort(activities);
        int changeMinutes = 0;
        if(LoggingType.DELETED.equals(loggingType)){
            this.startDate = activities.get(0).getStartDate();
            this.endDate = activities.get(activities.size()-1).getEndDate();
            changeMinutes = (int)new DateTimeInterval(startDate,endDate).getMinutes();
        }else if(LoggingType.CREATED.equals(loggingType)){
            changeMinutes = getMinutesOfActivity(activities);
        }else {
            for (int i = 0; i < activities.size(); i++) {
                DateTimeInterval shiftActivityInterval = activities.get(i).getInterval();
                if(i<old_activities.size()){
                    DateTimeInterval oldShiftActivityInterval = old_activities.get(i).getInterval();
                    int totalMinutes = (oldShiftActivityInterval.equals(shiftActivityInterval) && old_activities.get(i).getActivityId().equals(activities.get(i).getActivityId())) ? 0 : getChangedActivity(activities.get(i),old_activities.get(i));
                    changeMinutes+=abs(totalMinutes);
                }else {
                    changeMinutes += shiftActivityInterval.getMinutes();
                }
            }
        }
        return changeMinutes;
    }

    private int getChangedActivity(AuditShiftActivityDTO auditShiftActivityDTO, AuditShiftActivityDTO oldauditShiftActivityDTO) {
        long differenceInMillis = (auditShiftActivityDTO.getStartDate().getTime() - oldauditShiftActivityDTO.getStartDate().getTime()) + (auditShiftActivityDTO.getEndDate().getTime() - oldauditShiftActivityDTO.getEndDate().getTime());
        return (int)(differenceInMillis/ CommonConstants.ONE_MINUTE_MILLIS);
    }

    private int getMinutesOfActivity(List<AuditShiftActivityDTO> activities) {
        int intervalMinutes = 0;
        if(isCollectionNotEmpty(activities)){
            Collections.sort(activities);
            this.startDate = activities.get(0).getStartDate();
            this.endDate = activities.get(activities.size() - 1).getEndDate();
            intervalMinutes = (int)new DateTimeInterval(startDate, endDate).getMinutes();
        }
        return intervalMinutes;
    }
}
