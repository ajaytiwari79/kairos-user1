package com.kairos.response.dto.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by oodles on 26/4/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepetitionNext {
    private RepetitionType next;

    private String weekenddays;

    private String weekdays;

    public String getWeekenddays() {
        return weekenddays;
    }

    public void setWeekenddays(String weekenddays) {
        this.weekenddays = weekenddays;
    }

    public String getWeekdays() {
        return weekdays;
    }

    public void setWeekdays(String weekdays) {
        this.weekdays = weekdays;
    }

    public RepetitionType getNext() {
        return next;
    }

    public void setNext(RepetitionType next) {
        this.next = next;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [next = "+next+"]";
    }
}
