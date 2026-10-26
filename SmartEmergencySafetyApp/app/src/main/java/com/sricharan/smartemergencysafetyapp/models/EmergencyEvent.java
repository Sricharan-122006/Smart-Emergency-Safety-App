package com.sricharan.smartemergencysafetyapp.models;

public class EmergencyEvent {
    private String id;
    private String eventType; // e.g., "SOS Alert", "Location Share", "Safety Timer Expired"
    private long timestamp;
    private double latitude;
    private double longitude;
    private String mapsUrl;
    private String status; // e.g., "SOS Initiated (SMS App Opened)", "Location Shared"
    private String details;

    public EmergencyEvent() {
        // Empty constructor required for Firestore
    }

    public EmergencyEvent(String id, String eventType, long timestamp, double latitude, double longitude, String mapsUrl, String status, String details) {
        this.id = id;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.mapsUrl = mapsUrl;
        this.status = status;
        this.details = details;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getMapsUrl() {
        return mapsUrl;
    }

    public void setMapsUrl(String mapsUrl) {
        this.mapsUrl = mapsUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}