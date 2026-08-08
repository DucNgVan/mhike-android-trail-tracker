package com.example.hiking_application_coursework.models;

import java.io.Serializable;

public class User implements Serializable {
    private String uid;
    private String username;
    private String fullName;
    private String phone;
    
    // Statistics
    private int totalHikes;
    private double totalDistance;
    private double totalHours;

    public User() {}

    public User(String uid, String username, String fullName, String phone) {
        this.uid = uid;
        this.username = username;
        this.fullName = fullName;
        this.phone = phone;
    }

    // Getters and Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public int getTotalHikes() { return totalHikes; }
    public void setTotalHikes(int totalHikes) { this.totalHikes = totalHikes; }

    public double getTotalDistance() { return totalDistance; }
    public void setTotalDistance(double totalDistance) { this.totalDistance = totalDistance; }

    public double getTotalHours() { return totalHours; }
    public void setTotalHours(double totalHours) { this.totalHours = totalHours; }
}
