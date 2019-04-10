package com.kairos.persistence.model.time_bank;

import com.kairos.persistence.model.common.MongoBaseEntity;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

/*
* Created By Pradeep singh rajawat
*  Date-27/01/2018
*
* */
@Document(collection = "dailyTimeBankEntries")
public class DailyTimeBankEntry extends MongoBaseEntity{

    private Long employmentId;
    private Long staffId;
    //In minutes
    private int totalTimeBankMin; //It is Delta timebank
    private int contractualMin;
    private int scheduledMin;
    private int timeBankMinWithoutCta;
    private int timeBankMinWithCta;      //It is the sum of timeBankCTADistributionList
    private long accumultedTimeBankMin;
    private LocalDate date;
    private List<TimeBankCTADistribution> timeBankCTADistributionList;
    private int deltaAccumulatedTimebankMinutes;


    public DailyTimeBankEntry(Long employmentId, Long staffId, LocalDate date) {
        this.employmentId = employmentId;
        this.staffId = staffId;
        this.date = date;
    }

    public List<TimeBankCTADistribution> getTimeBankCTADistributionList() {
        return timeBankCTADistributionList;
    }

    public void setTimeBankCTADistributionList(List<TimeBankCTADistribution> timeBankCTADistributionList) {
        this.timeBankCTADistributionList = timeBankCTADistributionList;
    }


    public DailyTimeBankEntry() {
    }


    public long getAccumultedTimeBankMin() {
        return accumultedTimeBankMin;
    }

    public void setAccumultedTimeBankMin(long accumultedTimeBankMin) {
        this.accumultedTimeBankMin = accumultedTimeBankMin;
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

    public int getTimeBankMinWithoutCta() {
        return timeBankMinWithoutCta;
    }

    public void setTimeBankMinWithoutCta(int timeBankMinWithoutCta) {
        this.timeBankMinWithoutCta = timeBankMinWithoutCta;
    }

    public int getTimeBankMinWithCta() {
        return timeBankMinWithCta;
    }

    public void setTimeBankMinWithCta(int timeBankMinWithCta) {
        this.timeBankMinWithCta = timeBankMinWithCta;
    }

    public Long getEmploymentId() {
        return employmentId;
    }

    public void setEmploymentId(Long employmentId) {
        this.employmentId = employmentId;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public int getTotalTimeBankMin() {
        return totalTimeBankMin;
    }

    public void setTotalTimeBankMin(int totalTimeBankMin) {
        this.totalTimeBankMin = totalTimeBankMin;
    }

    public int getContractualMin() {
        return contractualMin;
    }

    public void setContractualMin(int contractualMin) {
        this.contractualMin = contractualMin;
    }

    public int getDeltaAccumulatedTimebankMinutes() {
        return deltaAccumulatedTimebankMinutes;
    }

    public void setDeltaAccumulatedTimebankMinutes(int deltaAccumulatedTimebankMinutes) {
        this.deltaAccumulatedTimebankMinutes = deltaAccumulatedTimebankMinutes;
    }
}
