package com.example.hiking_application_coursework.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Hike implements Serializable {
    private int id;
    private String name;
    private String location;
    private String date;
    private String parking;
    private String length; // Lưu số KM (chuỗi hoặc số)
    private String duration; // MỚI: Lưu số giờ (Hours)
    private String difficulty;
    private String description;
    private String weather;
    private String companions;
    private String imageResName;
    private String firebaseId;
    
    // Thông tin người tạo
    private String creatorId;
    private String creatorName;
    
    private List<Map<String, Double>> plannedRoute = new ArrayList<>();
    private List<Map<String, Double>> actualRoute = new ArrayList<>();

    public Hike() {}

    public Hike(int id, String name, String location, String date, String parking, String length, String difficulty, String description, String weather, String companions, String imageResName) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.date = date;
        this.parking = parking;
        this.length = length;
        this.difficulty = difficulty;
        this.description = description;
        this.weather = weather;
        this.companions = companions;
        this.imageResName = imageResName;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getParking() { return parking; }
    public void setParking(String parking) { this.parking = parking; }

    public String getLength() { return length; }
    public void setLength(String length) { this.length = length; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getWeather() { return weather; }
    public void setWeather(String weather) { this.weather = weather; }

    public String getCompanions() { return companions; }
    public void setCompanions(String companions) { this.companions = companions; }

    public String getImageResName() { return imageResName; }
    public void setImageResName(String imageResName) { this.imageResName = imageResName; }

    public String getFirebaseId() { return firebaseId; }
    public void setFirebaseId(String firebaseId) { this.firebaseId = firebaseId; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public List<Map<String, Double>> getPlannedRoute() { return plannedRoute; }
    public void setPlannedRoute(List<Map<String, Double>> plannedRoute) { this.plannedRoute = plannedRoute; }

    public List<Map<String, Double>> getActualRoute() { return actualRoute; }
    public void setActualRoute(List<Map<String, Double>> actualRoute) { this.actualRoute = actualRoute; }
    
    public List<Map<String, Double>> getRoutePoints() { return plannedRoute; }
    public void setRoutePoints(List<Map<String, Double>> routePoints) { this.plannedRoute = routePoints; }
}
