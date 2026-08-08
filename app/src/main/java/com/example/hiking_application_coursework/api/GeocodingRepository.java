package com.example.hiking_application_coursework.api;

import com.example.hiking_application_coursework.api.models.MapboxGeocodeResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GeocodingRepository {

    private final GeocodingApiService apiService;

    public interface GeocodeCallback {
        void onSuccess(double lat, double lng, String placeName);
        void onError(String errorMessage);
    }

    public GeocodingRepository() {
        this.apiService = ApiClient.getGeocodingService();
    }

    public GeocodingRepository(GeocodingApiService apiService) {
        this.apiService = apiService;
    }

    public void searchPlace(String query, String token, GeocodeCallback callback) {
        apiService.searchMapboxPlaces(query, token, 1).enqueue(new Callback<MapboxGeocodeResponse>() {
            @Override
            public void onResponse(Call<MapboxGeocodeResponse> call, Response<MapboxGeocodeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MapboxGeocodeResponse body = response.body();
                    if (body.getFeatures() != null && !body.getFeatures().isEmpty()) {
                        MapboxGeocodeResponse.Feature feature = body.getFeatures().get(0);
                        if (feature.getCenter() != null && feature.getCenter().size() >= 2) {
                            double lng = feature.getCenter().get(0);
                            double lat = feature.getCenter().get(1);
                            String placeName = feature.getPlaceName() != null ? feature.getPlaceName() : query;
                            callback.onSuccess(lat, lng, placeName);
                            return;
                        }
                    }
                    callback.onError("0 results found");
                } else {
                    callback.onError("HTTP Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MapboxGeocodeResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
