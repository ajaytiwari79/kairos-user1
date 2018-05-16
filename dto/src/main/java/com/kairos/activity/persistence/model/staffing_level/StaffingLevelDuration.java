package com.kairos.activity.persistence.model.staffing_level;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
//import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class StaffingLevelDuration {
    //@DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")

    private LocalTime from;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime to;

    public StaffingLevelDuration() {
        //default constructor
    }

    public StaffingLevelDuration(LocalTime from, LocalTime to) {
        this.from = from;
        this.to = to;
    }
    public int getDuration(){
        return  (int)ChronoUnit.MINUTES.between(from, to);
    }

    public LocalTime getFrom() {
        return from;
    }

    public void setFrom(LocalTime from) {
        this.from = from;
    }

    public LocalTime getTo() {
        return to;
    }

    public void setTo(LocalTime to) {
        this.to = to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof StaffingLevelDuration)) return false;

        StaffingLevelDuration that = (StaffingLevelDuration) o;

        return new EqualsBuilder()
                .append(from, that.from)
                .append(to, that.to)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(from)
                .append(to)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("from", from)
                .append("to", to)
                .toString();
    }
}
