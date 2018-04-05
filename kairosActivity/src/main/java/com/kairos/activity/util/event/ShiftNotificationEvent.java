package com.kairos.activity.util.event;

import com.kairos.activity.persistence.model.activity.Shift;

import java.util.Date;

public class ShiftNotificationEvent {
   private Long unitId;
   private Date currentDate;
   private Shift shift;
   private boolean shiftUpdated;
   private Shift previousStateShift;

    public ShiftNotificationEvent() {
    }

    public ShiftNotificationEvent(Long unitId, Date currentDate, Shift shift,
      boolean shiftUpdated, Shift previousStateShift) {
        this.unitId = unitId;
        this.currentDate = currentDate;
        this.shift = shift;
        this.shiftUpdated = shiftUpdated;
        this.previousStateShift = previousStateShift;
    }

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public Date getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public boolean isShiftUpdated() {
        return shiftUpdated;
    }

    public void setShiftUpdated(boolean shiftUpdated) {
        this.shiftUpdated = shiftUpdated;
    }

    public Shift getPreviousStateShift() {
        return previousStateShift;
    }

    public void setPreviousStateShift(Shift previousStateShift) {
        this.previousStateShift = previousStateShift;
    }
}
