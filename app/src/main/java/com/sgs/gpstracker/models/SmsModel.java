package com.sgs.gpstracker.models;

public class SmsModel {
    private String address;
    private String name;
    private String body;
    private long timestamp;
    private String formattedDate;
    private String type;

    // Required empty constructor for Firebase
    public SmsModel() {}

    public SmsModel(String address, String name, String body, long timestamp, String formattedDate, String type) {
        this.address = address;
        this.name = name;
        this.body = body;
        this.timestamp = timestamp;
        this.formattedDate = formattedDate;
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public void setFormattedDate(String formattedDate) {
        this.formattedDate = formattedDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
