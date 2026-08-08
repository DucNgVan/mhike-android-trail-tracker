package com.example.hiking_application_coursework.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String MAPBOX_BASE_URL = "https://api.mapbox.com/";
    private static Retrofit mapboxRetrofit = null;

    public static Retrofit getMapboxClient() {
        if (mapboxRetrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            mapboxRetrofit = new Retrofit.Builder()
                    .baseUrl(MAPBOX_BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return mapboxRetrofit;
    }

    public static GeocodingApiService getGeocodingService() {
        return getMapboxClient().create(GeocodingApiService.class);
    }
}
