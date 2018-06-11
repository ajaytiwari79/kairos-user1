package com.kairos.activity.shift;

import java.math.BigInteger;
import java.util.List;

public class ShiftResponse {
    private BigInteger shiftId;
    private String shiftName;
    private List<String> messages;
    private boolean success = false;

    public ShiftResponse() {
        //
    }

    public BigInteger getShiftId() {
        return shiftId;
    }

    public void setShiftId(BigInteger shiftId) {
        this.shiftId = shiftId;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public String getShiftName() {
        return shiftName;
    }

    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ShiftResponse(BigInteger shiftId, String shiftName, List<String> messages,Boolean success) {
        this.shiftId = shiftId;
        this.shiftName = shiftName;
        this.messages = messages;
        this.success=success;
    }

}
