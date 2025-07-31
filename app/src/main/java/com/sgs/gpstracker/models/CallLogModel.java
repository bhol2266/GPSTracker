package com.sgs.gpstracker.models;

public class CallLogModel {
    private String number;
    private String name;
    private String type;
    private String formattedDate;
    private long duration; // ⏱️ Duration in seconds

    public CallLogModel() {
        // Required for Firebase
    }

    public CallLogModel(String number, String name, String type, String formattedDate, long duration) {
        this.number = number;
        this.name = name;
        this.type = type;
        this.formattedDate = formattedDate;
        this.duration = duration;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public void setFormattedDate(String formattedDate) {
        this.formattedDate = formattedDate;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
