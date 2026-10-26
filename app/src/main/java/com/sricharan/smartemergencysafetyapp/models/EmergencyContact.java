package com.sricharan.smartemergencysafetyapp.models;

public class EmergencyContact {
    private String id;
    private String name;
    private String phoneNumber;
    private long createdAt;

    public EmergencyContact() {
        // Empty constructor required for Firestore
    }

    public EmergencyContact(String id, String name, String phoneNumber, long createdAt) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}