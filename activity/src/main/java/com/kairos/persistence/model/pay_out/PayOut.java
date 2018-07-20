package com.kairos.persistence.model.pay_out;

import com.kairos.enums.payout.PayOutStatus;
import com.kairos.persistence.model.common.MongoBaseEntity;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

/*
* Created By Pradeep singh rajawat
*  Date-27/01/2018
*
* */
public class PayOut extends MongoBaseEntity{

    private Long unitPositionId;
    private Long staffId;
    private BigInteger shiftId;
    //In minutes
    private int totalPayOutMin;
    private int contractualMin;
    private int scheduledMin;
    private int payOutMinWithoutCta;
    private int payOutMinWithCta;
    private LocalDate date;
    private PayOutStatus payOutStatus;
    private boolean paidout;
    private List<PayOutCTADistribution> payOutCTADistributionList;


    public PayOut(BigInteger shiftId,Long unitPositionId, Long staffId, LocalDate date) {
        this.shiftId = shiftId;
        this.unitPositionId = unitPositionId;
        this.staffId = staffId;
        this.date = date;
        this.payOutStatus = PayOutStatus.APPROVED;
    }

    public boolean isPaidout() {
        return paidout;
    }

    public void setPaidout(boolean paidout) {
        this.paidout = paidout;
    }

    public PayOutStatus getPayOutStatus() {
        return payOutStatus;
    }

    public void setPayOutStatus(PayOutStatus payOutStatus) {
        this.payOutStatus = payOutStatus;
    }

    public BigInteger getShiftId() {
        return shiftId;
    }

    public void setShiftId(BigInteger shiftId) {
        this.shiftId = shiftId;
    }

    public List<PayOutCTADistribution> getPayOutCTADistributionList() {
        return payOutCTADistributionList;
    }

    public void setPayOutCTADistributionList(List<PayOutCTADistribution> payOutCTADistributionList) {
        this.payOutCTADistributionList = payOutCTADistributionList;
    }


    public PayOut() {
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getScheduledMin() {
        return scheduledMin;
    }

    public void setScheduledMin(int scheduledMin) {
        this.scheduledMin = scheduledMin;
    }

    public int getPayOutMinWithoutCta() {
        return payOutMinWithoutCta;
    }

    public void setPayOutMinWithoutCta(int payOutMinWithoutCta) {
        this.payOutMinWithoutCta = payOutMinWithoutCta;
    }

    public int getPayOutMinWithCta() {
        return payOutMinWithCta;
    }

    public void setPayOutMinWithCta(int payOutMinWithCta) {
        this.payOutMinWithCta = payOutMinWithCta;
    }

    public Long getUnitPositionId() {
        return unitPositionId;
    }

    public void setUnitPositionId(Long unitPositionId) {
        this.unitPositionId = unitPositionId;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public int getTotalPayOutMin() {
        return totalPayOutMin;
    }

    public void setTotalPayOutMin(int totalPayOutMin) {
        this.totalPayOutMin = totalPayOutMin;
    }

    public int getContractualMin() {
        return contractualMin;
    }

    public void setContractualMin(int contractualMin) {
        this.contractualMin = contractualMin;
    }


}
