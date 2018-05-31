package com.kairos.activity.util;


import org.joda.time.DateTime;

import java.time.*;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.Date;

/**
 * @author pradeep
 * @date - 14/5/18
 */

public class DateTimeInterval {

    private Long start;
    private Long end;


    public DateTimeInterval(ZonedDateTime start, ZonedDateTime end) {
        this.start = start.toInstant().toEpochMilli();
        this.end = end.toInstant().toEpochMilli();
    }

    public DateTimeInterval(Long start, Long end) {
        this.start = start;
        this.end = end;
    }

    public ZonedDateTime getStart() {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(start), ZoneId.systemDefault());
    }

    public long getStartMillis() {
        return start;
    }

    public void setStart(ZonedDateTime start) {
        this.start = start.toInstant().toEpochMilli();
    }

    public void setStart(long start) {
        this.start = start;
    }

    public ZonedDateTime getEnd() {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(end), ZoneId.systemDefault());
    }

    public long getEndMillis() {
        return end;
    }

    public void setStartFrom(LocalDateTime startFrom) {
        this.start = startFrom.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public void setEndTo(LocalDateTime endTo) {
        this.start = endTo.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public LocalDate getStartLocalDate(){
        return getStart().toLocalDate();
    }

    public LocalDate getEndLocalDate(){
        return getEnd().toLocalDate();
    }

    public LocalTime getStartLocalTime(){
        return getStart().toLocalTime();
    }

    public LocalTime getEndLocalTime(){
        return getEnd().toLocalTime();
    }

    public LocalDateTime getStartLocalDateTime(){
        return getStart().toLocalDateTime();
    }

    public LocalDateTime getEndLocalDateTime(){
        return getEnd().toLocalDateTime();
    }


    public void setEnd(ZonedDateTime end) {
        this.end = end.toInstant().toEpochMilli();
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public DateTimeInterval overlap(DateTimeInterval interval) {
        if (!overlaps(interval)) {
            return null;
        }
        long start = Math.max(getStartMillis(), interval.getStartMillis());
        long end = Math.min(getEndMillis(), interval.getEndMillis());
        return new DateTimeInterval(start, end);
    }

    public DateTimeInterval addInterval(DateTimeInterval interval){
        long start = Math.max(this.start, interval.getStartMillis());
        long end = Math.min(this.end, interval.getEndMillis());
        return new DateTimeInterval(start, end);
    }

    public boolean overlaps(DateTimeInterval interval) {
        long thisStart = getStartMillis();
        long thisEnd = getEndMillis();
        if (interval == null) {
            long now = ZonedDateTime.now().toInstant().toEpochMilli();
            return (thisStart < now && now < thisEnd);
        }  else {
            long otherStart = interval.getStartMillis();
            long otherEnd = interval.getEndMillis();
            return (thisStart < otherEnd && otherStart < thisEnd);
        }
    }

    public DateTimeInterval gap(DateTimeInterval interval) {
        long otherStart = interval.getStartMillis();
        long otherEnd = interval.getEndMillis();
        long thisStart = getStartMillis();
        long thisEnd = getEndMillis();
        if (thisStart > otherEnd) {
            return new DateTimeInterval(otherEnd, thisStart);
        } else if (otherStart > thisEnd) {
            return new DateTimeInterval(thisEnd, otherStart);
        } else {
            return null;
        }
    }

    public boolean contains(long millisInstant) {
        long thisStart = getStartMillis();
        long thisEnd = getEndMillis();
        return (millisInstant >= thisStart && millisInstant < thisEnd);
    }

    public boolean contains(Date date) {
        long thisStart = getStartMillis();
        long thisEnd = getEndMillis();
        return (date.getTime() >= thisStart && date.getTime() < thisEnd);
    }

    public boolean containsNow() {
        Date date = new Date();
        long thisStart = getStartMillis();
        long thisEnd = getEndMillis();
        return (date.getTime() >= thisStart && date.getTime() < thisEnd);
    }

    public boolean abuts(DateTimeInterval interval) {
        if (interval == null) {
            long now = ZonedDateTime.now().toInstant().toEpochMilli();
            return (getStartMillis() == now || getEndMillis() == now);
        } else {
            return (interval.getEndMillis() == getStartMillis() ||
                    getEndMillis() == interval.getStartMillis());
        }
    }

    public boolean containsInterval(DateTimeInterval interval){
        return this.start>=interval.getStartMillis() && this.end>=interval.getEndMillis();
    }

    public int getMinutes(){
        return (int) (this.start - this.end)/60000;
    }

    public int getHours(){
        return (int) (this.start - this.end)/3600000;
    }

    public int getSeconds(){
        return (int) (this.start - this.end)/1000;
    }

    public Long getMilliSeconds(){
        return (this.start - this.end);
    }

    @Override
    public String toString() {
        return "DateTimeInterval{" +
                "start=" + getStart() +
                ", end=" + getEnd() +
                '}';
    }
}
