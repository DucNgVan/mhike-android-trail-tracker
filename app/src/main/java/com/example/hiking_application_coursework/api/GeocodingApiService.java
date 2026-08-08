package com.example.hiking_application_coursework.api;

import com.example.hiking_application_coursework.api.models.MapboxGeocodeResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GeocodingApiService {

    @GET("geocoding/v5/mapbox.places/{query}.json")
    Call<MapboxGeocodeResponse> searchMapboxPlaces(
            @Path("query") String query,
            @Query("access_token") String accessToken,
            @Query("limit") int limit
    );
}
