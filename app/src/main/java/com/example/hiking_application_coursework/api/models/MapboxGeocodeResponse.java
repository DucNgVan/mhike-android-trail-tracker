package com.example.hiking_application_coursework.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MapboxGeocodeResponse {
    @SerializedName("features")
    private List<Feature> features;

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public static class Feature {
        @SerializedName("place_name")
        private String placeName;

        @SerializedName("center")
        private List<Double> center; // [lng, lat]

        public String getPlaceName() {
            return placeName;
        }

        public void setPlaceName(String placeName) {
            this.placeName = placeName;
        }

        public List<Double> getCenter() {
            return center;
        }

        public void setCenter(List<Double> center) {
            this.center = center;
        }
    }
}
