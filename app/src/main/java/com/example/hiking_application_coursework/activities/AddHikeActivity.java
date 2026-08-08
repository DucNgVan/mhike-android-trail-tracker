package com.example.hiking_application_coursework.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;

import com.example.hiking_application_coursework.R;
import com.example.hiking_application_coursework.helpers.FirestoreHelper;
import com.example.hiking_application_coursework.models.Hike;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.gestures.GesturesUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class AddHikeActivity extends AppCompatActivity {

    private EditText editHikeName, editLocation, editDate, editLength, editDuration, editLat, editLng;
    private AutoCompleteTextView spinnerDifficulty, spinnerImage, spinnerWeather, spinnerParking;
    private MapView previewMap;
    private CircleAnnotationManager circleAnnotationManager;
    private PointAnnotationManager pointAnnotationManager;
    private FirestoreHelper firestoreHelper;
    private FusedLocationProviderClient fusedLocationClient;
    
    private Hike editHike;
    private boolean isEditMode = false;
    private boolean isUpdatingMarkerFromText = false;
    private boolean isUpdatingAddressFromMap = false;

    private final android.os.Handler searchHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable searchRunnable;
    private TextView textSearchStatus;
    private final java.util.concurrent.atomic.AtomicInteger currentSearchId = new java.util.concurrent.atomic.AtomicInteger(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hike);

        firestoreHelper = new FirestoreHelper();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        editHike = (Hike) getIntent().getSerializableExtra("hike");
        if (editHike != null) {
            isEditMode = true;
        }

        setupUI();
        initPreviewMap();
        
        if (isEditMode) {
            fillDataForEdit();
        }
    }

    private void setupUI() {
        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
        editHikeName = findViewById(R.id.editHikeName);
        editLocation = findViewById(R.id.editLocation);
        textSearchStatus = findViewById(R.id.textSearchStatus);
        editDate = findViewById(R.id.editDate);
        editLength = findViewById(R.id.editLength);
        editDuration = findViewById(R.id.editDuration);
        editLat = findViewById(R.id.editLat);
        editLng = findViewById(R.id.editLng);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        spinnerWeather = findViewById(R.id.spinnerWeather);
        spinnerParking = findViewById(R.id.spinnerParking);
        spinnerImage = findViewById(R.id.spinnerImage);
        previewMap = findViewById(R.id.addHikeMapView);

        String[] difficulties = getResources().getStringArray(R.array.difficulty_levels);
        spinnerDifficulty.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, difficulties));

        String[] weathers = getResources().getStringArray(R.array.weather_options);
        spinnerWeather.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, weathers));

        String[] parkings = getResources().getStringArray(R.array.parking_options);
        spinnerParking.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, parkings));

        String[] images = getResources().getStringArray(R.array.image_options);
        spinnerImage.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, images));

        editDate.setOnClickListener(v -> showDatePicker());
        
        TextInputLayout layoutLocation = findViewById(R.id.layoutLocation);
        layoutLocation.setEndIconOnClickListener(v -> searchLocation(editLocation.getText().toString().trim()));
        
        editLocation.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchLocation(editLocation.getText().toString().trim());
                return true;
            }
            return false;
        });

        editLocation.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (isUpdatingAddressFromMap) return;
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                String text = s.toString().trim();
                if (text.length() >= 3) {
                    searchRunnable = () -> searchLocation(text);
                    searchHandler.postDelayed(searchRunnable, 500);
                }
            }
        });

        TextWatcher latLngWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (!isUpdatingMarkerFromText) {
                    updatePreviewMarker();
                }
            }
        };
        editLat.addTextChangedListener(latLngWatcher);
        editLng.addTextChangedListener(latLngWatcher);

        previewMap.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return false;
        });

        findViewById(R.id.btnZoomIn).setOnClickListener(v -> {
            double z = previewMap.getMapboxMap().getCameraState().getZoom();
            previewMap.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(z + 1.0).build());
        });

        findViewById(R.id.btnZoomOut).setOnClickListener(v -> {
            double z = previewMap.getMapboxMap().getCameraState().getZoom();
            previewMap.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(z - 1.0).build());
        });

        findViewById(R.id.btnNext).setOnClickListener(v -> validateAndConfirm());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
        findViewById(R.id.btnGetGPS).setOnClickListener(v -> fetchCurrentLocation());
        
        if (isEditMode) {
            ((TextView) findViewById(R.id.btnNext)).setText("Update Adventure");
        }
    }

    private void fillDataForEdit() {
        editHikeName.setText(editHike.getName());
        editLocation.setText(editHike.getLocation());
        editDate.setText(editHike.getDate());
        editLength.setText(editHike.getLength());
        editDuration.setText(editHike.getDuration());
        spinnerDifficulty.setText(editHike.getDifficulty(), false);
        spinnerWeather.setText(editHike.getWeather(), false);
        spinnerParking.setText(editHike.getParking(), false);
        spinnerImage.setText(editHike.getImageResName(), false);
        
        if (!editHike.getPlannedRoute().isEmpty()) {
            Map<String, Double> p = editHike.getPlannedRoute().get(0);
            editLat.setText(String.valueOf(p.get("lat")));
            editLng.setText(String.valueOf(p.get("lng")));
        }
    }

    private void initPreviewMap() {
        previewMap.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
            setupMapClickListener();
            if (TextUtils.isEmpty(editLat.getText().toString()) || TextUtils.isEmpty(editLng.getText().toString())) {
                isUpdatingMarkerFromText = true;
                editLat.setText("16.047079");
                editLng.setText("108.206230");
                isUpdatingMarkerFromText = false;
            }
            updatePreviewMarker();
        });
    }

    private void setupMapClickListener() {
        GesturesUtils.getGestures(previewMap).addOnMapClickListener(point -> {
            isUpdatingMarkerFromText = true;
            editLat.setText(String.format(Locale.US, "%.6f", point.latitude()));
            editLng.setText(String.format(Locale.US, "%.6f", point.longitude()));
            isUpdatingMarkerFromText = false;
            
            updatePreviewMarker();
            reverseGeocode(point.latitude(), point.longitude());
            return true;
        });
    }

    private void reverseGeocode(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(lat, lng, 1, addresses -> {
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String addressStr = address.getAddressLine(0);
                    if (addressStr != null) {
                        runOnUiThread(() -> {
                            isUpdatingAddressFromMap = true;
                            editLocation.setText(addressStr);
                            isUpdatingAddressFromMap = false;
                        });
                    }
                }
            });
        } else {
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String addressStr = address.getAddressLine(0);
                    if (addressStr != null) {
                        isUpdatingAddressFromMap = true;
                        editLocation.setText(addressStr);
                        isUpdatingAddressFromMap = false;
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    private Bitmap getBitmapFromDrawableRes(int resourceId) {
        Drawable drawable = AppCompatResources.getDrawable(this, resourceId);
        if (drawable == null) return null;
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : 96,
                drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : 96,
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            return;
        }

        CurrentLocationRequest request = new CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();

        fusedLocationClient.getCurrentLocation(request, null).addOnSuccessListener(this, location -> {
            if (location != null) {
                editLat.setText(String.valueOf(location.getLatitude()));
                editLng.setText(String.valueOf(location.getLongitude()));
                updatePreviewMarker();
                reverseGeocode(location.getLatitude(), location.getLongitude());
                Toast.makeText(this, "Location pinned!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Searching for GPS signal...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePreviewMarker() {
        try {
            String latStr = editLat.getText().toString();
            String lngStr = editLng.getText().toString();
            if (TextUtils.isEmpty(latStr) || TextUtils.isEmpty(lngStr)) return;

            double lat = Double.parseDouble(latStr);
            double lng = Double.parseDouble(lngStr);
            Point p = Point.fromLngLat(lng, lat);
            
            // Move camera immediately to requested coordinates
            if (previewMap != null && previewMap.getMapboxMap() != null) {
                previewMap.getMapboxMap().setCamera(new CameraOptions.Builder().center(p).zoom(14.0).build());
            }

            // Update Mapbox red marker icon annotation
            AnnotationPlugin annotationApi = (AnnotationPlugin) previewMap.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID);
            if (annotationApi != null) {
                if (pointAnnotationManager == null) {
                    pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationApi, new AnnotationConfig());
                }
                if (pointAnnotationManager != null) {
                    pointAnnotationManager.deleteAll();
                    Bitmap bitmap = getBitmapFromDrawableRes(R.drawable.ic_red_marker);
                    if (bitmap != null) {
                        PointAnnotationOptions options = new PointAnnotationOptions()
                                .withPoint(p)
                                .withIconImage(bitmap)
                                .withIconSize(1.3)
                                .withIconAnchor(IconAnchor.BOTTOM);
                        pointAnnotationManager.create(options);
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> 
                editDate.setText(String.format(Locale.US, "%02d/%02d/%d", day, month + 1, year)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private boolean isNetworkConnected() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            }
        } catch (Exception ignored) {}
        return true;
    }

    private void updateSearchStatus(int searchId, String message, int colorResId) {
        if (searchId != currentSearchId.get()) return;
        runOnUiThread(() -> {
            if (textSearchStatus != null) {
                textSearchStatus.setVisibility(View.VISIBLE);
                textSearchStatus.setText(message);
                if (colorResId != 0) {
                    textSearchStatus.setTextColor(androidx.core.content.ContextCompat.getColor(this, colorResId));
                }
            }
        });
    }

    private void searchLocation(String query) {
        if (TextUtils.isEmpty(query)) return;

        if (!isNetworkConnected()) {
            Toast.makeText(this, "❌ WiFi/Network Disconnected. Check your internet connection!", Toast.LENGTH_LONG).show();
            return;
        }

        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }

        final int searchId = currentSearchId.incrementAndGet();
        final long startTime = System.currentTimeMillis();

        new Thread(() -> {
            // --- Sử dụng trực tiếp OpenStreetMap Nominatim API ---
            updateSearchStatus(searchId, "🔄 Đang tìm kiếm địa điểm qua OpenStreetMap (Nominatim)...", R.color.secondary);
            String nomErr = searchNominatim(query, searchId);
            if (nomErr == null) {
                long elapsed = System.currentTimeMillis() - startTime;
                updateSearchStatus(searchId, "✅ Tìm thấy vị trí thành công (" + elapsed + " ms)", R.color.primary);
                return;
            }
            if (searchId != currentSearchId.get()) return;

            updateSearchStatus(searchId, "❌ Không tìm thấy vị trí (" + nomErr + "). Vui lòng chạm bản đồ để ghim.", R.color.error);
        }).start();
    }


    private String searchMapboxGeocoding(String query, int searchId) {
        try {
            String token = getString(R.string.mapbox_access_token);
            String encoded = URLEncoder.encode(query, "UTF-8").replace("+", "%20");
            URL url = new URL("https://api.mapbox.com/geocoding/v5/mapbox.places/" + encoded + ".json?access_token=" + token + "&limit=1");
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "MHikeApplication/1.0");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            int code = conn.getResponseCode();
            if (code == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(sb.toString());
                JSONArray features = json.optJSONArray("features");
                if (features != null && features.length() > 0) {
                    JSONObject first = features.getJSONObject(0);
                    JSONArray center = first.getJSONArray("center");
                    double lon = center.getDouble(0);
                    double lat = center.getDouble(1);
                    String placeName = first.optString("place_name", query);

                    if (searchId == currentSearchId.get()) {
                        runOnUiThread(() -> applySearchedCoordinates(lat, lon, placeName));
                        return null; // SUCCESS
                    } else {
                        return "Cancelled";
                    }
                } else {
                    return "0 results";
                }
            } else {
                return "HTTP " + code;
            }
        } catch (UnknownHostException e) {
            return "No Internet (UnknownHostException)";
        } catch (Exception e) {
            return e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    private String searchNominatim(String query, int searchId) {
        try {
            String encoded = URLEncoder.encode(query, "UTF-8").replace("+", "%20");
            URL url = new URL("https://nominatim.openstreetmap.org/search?q=" + encoded + "&format=json&limit=1&accept-language=vi,en");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "MHikeApplication/1.0 (contact: student@coursework.com)");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            int code = conn.getResponseCode();
            if (code == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                JSONArray arr = new JSONArray(sb.toString());
                if (arr.length() > 0) {
                    JSONObject obj = arr.getJSONObject(0);
                    double lat = obj.getDouble("lat");
                    double lon = obj.getDouble("lon");
                    String displayName = obj.optString("display_name", query);

                    if (searchId == currentSearchId.get()) {
                        runOnUiThread(() -> applySearchedCoordinates(lat, lon, displayName));
                        return null; // SUCCESS
                    } else {
                        return "Cancelled";
                    }
                } else {
                    return "0 results";
                }
            } else {
                return "HTTP " + code;
            }
        } catch (UnknownHostException e) {
            return "No Internet (UnknownHostException)";
        } catch (Exception e) {
            return e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    private String searchPhoton(String query, int searchId) {
        try {
            String encoded = URLEncoder.encode(query, "UTF-8").replace("+", "%20");
            URL url = new URL("https://photon.komoot.io/api/?q=" + encoded + "&limit=1");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "MHikeApplication/1.0");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            int code = conn.getResponseCode();
            if (code == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(sb.toString());
                JSONArray features = json.optJSONArray("features");
                if (features != null && features.length() > 0) {
                    JSONObject first = features.getJSONObject(0);
                    JSONObject geometry = first.getJSONObject("geometry");
                    JSONArray coords = geometry.getJSONArray("coordinates");
                    double lon = coords.getDouble(0);
                    double lat = coords.getDouble(1);

                    JSONObject props = first.optJSONObject("properties");
                    String name = props != null ? props.optString("name", query) : query;

                    if (searchId == currentSearchId.get()) {
                        runOnUiThread(() -> applySearchedCoordinates(lat, lon, name));
                        return null; // SUCCESS
                    } else {
                        return "Cancelled";
                    }
                } else {
                    return "0 results";
                }
            } else {
                return "HTTP " + code;
            }
        } catch (UnknownHostException e) {
            return "No Internet (UnknownHostException)";
        } catch (Exception e) {
            return e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    private String searchAndroidGeocoder(String query, int searchId) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocationName(query, 5);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                if (searchId == currentSearchId.get()) {
                    runOnUiThread(() -> applySearchedCoordinates(address.getLatitude(), address.getLongitude(), address.getAddressLine(0)));
                    return null; // SUCCESS
                } else {
                    return "Cancelled";
                }
            } else {
                return "0 results";
            }
        } catch (UnknownHostException e) {
            return "No Internet (UnknownHostException)";
        } catch (Exception e) {
            return e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    private void applySearchedCoordinates(double lat, double lon, String placeName) {
        isUpdatingMarkerFromText = true;
        editLat.setText(String.format(Locale.US, "%.6f", lat));
        editLng.setText(String.format(Locale.US, "%.6f", lon));
        isUpdatingMarkerFromText = false;

        updatePreviewMarker();

        if (placeName != null && !placeName.isEmpty()) {
            isUpdatingAddressFromMap = true;
            editLocation.setText(placeName);
            isUpdatingAddressFromMap = false;
        }
        Toast.makeText(this, "Location found!", Toast.LENGTH_SHORT).show();
    }

    private void applySearchedAddress(Address address) {
        applySearchedCoordinates(address.getLatitude(), address.getLongitude(), address.getAddressLine(0));
    }

    private void validateAndConfirm() {
        if (TextUtils.isEmpty(editHikeName.getText()) || TextUtils.isEmpty(editLocation.getText()) || TextUtils.isEmpty(editDuration.getText())) {
            Toast.makeText(this, "Fields marked with * are required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showConfirmationDialog();
    }

    private void showConfirmationDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_hike, null);
        
        ((TextView) dialogView.findViewById(R.id.confirmName)).setText(editHikeName.getText());
        ((TextView) dialogView.findViewById(R.id.confirmLocation)).setText(editLocation.getText());
        ((TextView) dialogView.findViewById(R.id.confirmDate)).setText(editDate.getText());
        ((TextView) dialogView.findViewById(R.id.confirmDifficulty)).setText(spinnerDifficulty.getText());
        ((TextView) dialogView.findViewById(R.id.confirmDistance)).setText(editLength.getText() + " km");
        ((TextView) dialogView.findViewById(R.id.confirmDuration)).setText(editDuration.getText() + " h");
        ((TextView) dialogView.findViewById(R.id.confirmWeather)).setText(spinnerWeather.getText());
        ((TextView) dialogView.findViewById(R.id.confirmParking)).setText(spinnerParking.getText());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btnDialogCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnDialogConfirm).setOnClickListener(v -> {
            dialog.dismiss();
            saveHike();
        });

        dialog.show();
    }

    private void saveHike() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        
        Hike hike = isEditMode ? editHike : new Hike();
        hike.setName(editHikeName.getText().toString().trim());
        hike.setLocation(editLocation.getText().toString().trim());
        hike.setDate(editDate.getText().toString().trim());
        hike.setLength(editLength.getText().toString().trim()); 
        hike.setDuration(editDuration.getText().toString().trim());
        hike.setDifficulty(spinnerDifficulty.getText().toString());
        hike.setWeather(spinnerWeather.getText().toString());
        hike.setParking(spinnerParking.getText().toString());
        
        if (!isEditMode && user != null) {
            hike.setCreatorId(user.getUid());
            hike.setCreatorName(user.getDisplayName() != null ? user.getDisplayName() : "Explorer");
        }

        try {
            double lat = Double.parseDouble(editLat.getText().toString());
            double lng = Double.parseDouble(editLng.getText().toString());
            List<Map<String, Double>> points = new ArrayList<>();
            Map<String, Double> p = new HashMap<>();
            p.put("lat", lat); p.put("lng", lng);
            points.add(p);
            hike.setPlannedRoute(points);
            hike.setRoutePoints(points);
        } catch (Exception ignored) {}

        String selImg = spinnerImage.getText().toString();
        hike.setImageResName(selImg.contains("img5") ? "img5" : selImg.contains("img4") ? "img4" : selImg.contains("img3") ? "img3" : selImg.contains("img2") ? "img2" : "img1");

        if (isEditMode) {
            firestoreHelper.updateHike(hike, new FirestoreHelper.DataStatus() {
                @Override public void dataIsLoaded(List<Hike> hikes) {}
                @Override public void dataIsInserted() {
                    Toast.makeText(AddHikeActivity.this, "Adventure Updated!", Toast.LENGTH_SHORT).show();
                    finish();
                }
                @Override public void dataIsDeleted() {}
            });
        } else {
            firestoreHelper.insertHike(hike, new FirestoreHelper.DataStatus() {
                @Override public void dataIsLoaded(List<Hike> hikes) {}
                @Override public void dataIsInserted() {
                    Toast.makeText(AddHikeActivity.this, "Adventure Saved!", Toast.LENGTH_SHORT).show();
                    finish();
                }
                @Override public void dataIsDeleted() {}
            });
        }
    }
}
