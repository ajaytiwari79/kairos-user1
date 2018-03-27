package com.kairos.response.dto.web.experties;

import com.kairos.persistence.model.user.pay_table.FutureDate;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;
import org.neo4j.ogm.annotation.typeconversion.DateLong;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by prerna on 14/11/17.
 */
public class CountryExpertiseDTO {
    private Long id;

    @NotEmpty(message = "error.Expertise.name.notEmpty")
    @NotNull(message = "error.Expertise.name.notnull")
    private String name;
    private String description;
    @NotNull(message = "Start date can't be null")
    @DateLong
    @FutureDate
    private Date startDateMillis;

    @FutureDate
    @DateLong
    private Date endDateMillis;
    @NotNull(message = "Level can not be null")
    private Long OrganizationLevelId;
    @NotNull(message = "services can not be null")
    private Long serviceId;
    @NotNull(message = "union can not be null")
    private Long unionId;
    private int fullTimeWeeklyMinutes = 2220; // This is equals to 37 hours
    private Integer numberOfWorkingDaysInWeek; // 5 or 7
    @NotNull(message = "PayTable can not be null")
    private Long payTableId;
    private PaidOutFrequencyEnum paidOutFrequency;
    private SeniorityLevelDTO seniorityLevel;
    private List<Long> tags;


    public CountryExpertiseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Long> getTags() {
        return tags;
    }

    public void setTags(List<Long> tags) {
        this.tags = tags;
    }

    public Date getStartDateMillis() {
        return startDateMillis;
    }

    public void setStartDateMillis(Date startDateMillis) {
        this.startDateMillis = startDateMillis;
    }

    public Date getEndDateMillis() {
        return endDateMillis;
    }

    public void setEndDateMillis(Date endDateMillis) {
        this.endDateMillis = endDateMillis;
    }

    public Long getOrganizationLevelId() {
        return OrganizationLevelId;
    }

    public void setOrganizationLevelId(Long organizationLevelId) {
        OrganizationLevelId = organizationLevelId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Long getUnionId() {
        return unionId;
    }

    public void setUnionId(Long unionId) {
        this.unionId = unionId;
    }

    public int getFullTimeWeeklyMinutes() {
        return fullTimeWeeklyMinutes;
    }

    public void setFullTimeWeeklyMinutes(int fullTimeWeeklyMinutes) {
        this.fullTimeWeeklyMinutes = fullTimeWeeklyMinutes;
    }

    public Long getPayTableId() {
        return payTableId;
    }

    public void setPayTableId(Long payTableId) {
        this.payTableId = payTableId;
    }

    public PaidOutFrequencyEnum getPaidOutFrequency() {
        return paidOutFrequency;
    }

    public void setPaidOutFrequency(PaidOutFrequencyEnum paidOutFrequency) {
        this.paidOutFrequency = paidOutFrequency;
    }

    public SeniorityLevelDTO getSeniorityLevel() {
        return seniorityLevel;
    }

    public void setSeniorityLevel(SeniorityLevelDTO seniorityLevel) {
        this.seniorityLevel = seniorityLevel;
    }

    public Integer getNumberOfWorkingDaysInWeek() {
        return numberOfWorkingDaysInWeek;
    }

    public void setNumberOfWorkingDaysInWeek(Integer numberOfWorkingDaysInWeek) {
        this.numberOfWorkingDaysInWeek = numberOfWorkingDaysInWeek;
    }

    @AssertTrue(message = "'start date' must be less than 'end date'.")
    public boolean isValid() {
        if (!Optional.ofNullable(this.startDateMillis).isPresent()) {
            return false;
        }
        if (Optional.ofNullable(this.endDateMillis).isPresent()) {
            DateTime endDateAsUtc = new DateTime(this.endDateMillis).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            DateTime startDateAsUtc = new DateTime(this.startDateMillis).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            boolean dateValue = (endDateAsUtc.isBefore(startDateAsUtc)) ? false : true;
            return dateValue;
        }
        return true;
    }
}
