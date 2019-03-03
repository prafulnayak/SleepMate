package com.anyemi.sleepmate;

public class IdleModel {

    private String idleStartTime;
    private String idleEndTime;
    private long days;
    private long hours;
    private long min;
    private long sec;

    public IdleModel(String idleStartTime, String idleEndTime, long days, long hours, long min, long sec) {
        this.idleStartTime = idleStartTime;
        this.idleEndTime = idleEndTime;
        this.days = days;
        this.hours = hours;
        this.min = min;
        this.sec = sec;
    }

    public String getIdleStartTime() {
        return idleStartTime;
    }

    public void setIdleStartTime(String idleStartTime) {
        this.idleStartTime = idleStartTime;
    }

    public String getIdleEndTime() {
        return idleEndTime;
    }

    public void setIdleEndTime(String idleEndTime) {
        this.idleEndTime = idleEndTime;
    }

    public long getDays() {
        return days;
    }

    public void setDays(long days) {
        this.days = days;
    }

    public long getHours() {
        return hours;
    }

    public void setHours(long hours) {
        this.hours = hours;
    }

    public long getMin() {
        return min;
    }

    public void setMin(long min) {
        this.min = min;
    }

    public long getSec() {
        return sec;
    }

    public void setSec(long sec) {
        this.sec = sec;
    }
}
