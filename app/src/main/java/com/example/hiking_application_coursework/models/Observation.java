package com.example.hiking_application_coursework.models;

import java.io.Serializable;

public class Observation implements Serializable {
    private int id;
    private int hikeId;
    private String observation;
    private String time;
    private String comments;
    private String hikeFirebaseId;

    public Observation() {}

    public Observation(int id, int hikeId, String observation, String time, String comments) {
        this.id = id;
        this.hikeId = hikeId;
        this.observation = observation;
        this.time = time;
        this.comments = comments;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getHikeId() { return hikeId; }
    public void setHikeId(int hikeId) { this.hikeId = hikeId; }

    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public String getHikeFirebaseId() { return hikeFirebaseId; }
    public void setHikeFirebaseId(String hikeFirebaseId) { this.hikeFirebaseId = hikeFirebaseId; }
}
