package com.kairos.wrappers.task_demand;

import java.util.Date;

public class TaskDemandRequestWrapper {
   private Long citizenId;
   private Long unitId;
   private Long timeSlotId;
   private Date startDate;
   private Date endDate;
    public TaskDemandRequestWrapper(){
        //default constructor
    }

    public TaskDemandRequestWrapper(Long citizenId, Long unitId, Long timeSlotId, Date startDate, Date endDate) {
        this.citizenId = citizenId;
        this.unitId = unitId;
        this.timeSlotId = timeSlotId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getCitizenId() {
        return citizenId;
    }

    public void setCitizenId(Long citizenId) {
        this.citizenId = citizenId;
    }

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public Long getTimeSlotId() {
        return timeSlotId;
    }

    public void setTimeSlotId(Long timeSlotId) {
        this.timeSlotId = timeSlotId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
