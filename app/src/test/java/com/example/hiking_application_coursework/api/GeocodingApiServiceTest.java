package com.example.hiking_application_coursework.api;

import com.example.hiking_application_coursework.api.models.MapboxGeocodeResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GeocodingApiServiceTest {

    private MockWebServer mockWebServer;
    private GeocodingApiService apiService;

    @Before
    public void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mockWebServer.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(GeocodingApiService.class);
    }

    @After
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void searchMapboxPlaces_returnsParsedLocation() throws Exception {
        // Mock JSON Response từ Mapbox API
        String jsonResponse = "{\n" +
                "  \"features\": [\n" +
                "    {\n" +
                "      \"place_name\": \"Fansipan Mountain, Sa Pa, Lao Cai, Vietnam\",\n" +
                "      \"center\": [103.7750, 22.3033]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonResponse));

        // Gọi API
        Call<MapboxGeocodeResponse> call = apiService.searchMapboxPlaces("Fansipan", "fake_token", 1);
        Response<MapboxGeocodeResponse> response = call.execute();

        // Kiểm tra kết quả (Assertions)
        assertTrue(response.isSuccessful());
        assertNotNull(response.body());
        assertNotNull(response.body().getFeatures());
        assertEquals(1, response.body().getFeatures().size());

        MapboxGeocodeResponse.Feature feature = response.body().getFeatures().get(0);
        assertEquals("Fansipan Mountain, Sa Pa, Lao Cai, Vietnam", feature.getPlaceName());
        assertEquals(103.7750, feature.getCenter().get(0), 0.0001);
        assertEquals(22.3033, feature.getCenter().get(1), 0.0001);
    }

    @Test
    public void searchMapboxPlaces_handlesEmptyResults() throws Exception {
        String jsonResponse = "{\"features\": []}";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonResponse));

        Call<MapboxGeocodeResponse> call = apiService.searchMapboxPlaces("UnknownPlace123", "fake_token", 1);
        Response<MapboxGeocodeResponse> response = call.execute();

        assertTrue(response.isSuccessful());
        assertNotNull(response.body());
        assertTrue(response.body().getFeatures().isEmpty());
    }
}
