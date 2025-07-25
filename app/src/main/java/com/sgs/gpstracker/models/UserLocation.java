package com.sgs.gpstracker.models;

public class UserLocation {
    private String name;
    private String device;
    private double latitude;
    private double longitude;
    private long timestamp;

    public UserLocation() {}

    public String getName() { return name; }
    public String getDevice() { return device; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public long getTimestamp() { return timestamp; }
}
