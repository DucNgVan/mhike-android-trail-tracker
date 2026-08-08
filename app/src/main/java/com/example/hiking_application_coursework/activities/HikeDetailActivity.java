package com.example.hiking_application_coursework.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hiking_application_coursework.R;
import com.example.hiking_application_coursework.adapters.ObservationAdapter;
import com.example.hiking_application_coursework.helpers.FirestoreHelper;
import com.example.hiking_application_coursework.models.Hike;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HikeDetailActivity extends AppCompatActivity {

    private Hike hike;
    private FirestoreHelper firestoreHelper;
    private MapView mapView;
    private CircleAnnotationManager circleAnnotationManager;
    private PointAnnotationManager pointAnnotationManager;
    private List<Point> plannedPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hike_detail);

        hike = (Hike) getIntent().getSerializableExtra("hike");
        firestoreHelper = new FirestoreHelper();
        plannedPoints = convertToPoints(hike.getPlannedRoute());
        if (plannedPoints.isEmpty()) plannedPoints = convertToPoints(hike.getRoutePoints());

        setupUI();
        initDetailMap();
    }

    private void setupUI() {
        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
        
        ((TextView) findViewById(R.id.txtDetailName)).setText(hike.getName());
        ((TextView) findViewById(R.id.txtDetailLocation)).setText(hike.getLocation());
        ((TextView) findViewById(R.id.txtDetailDate)).setText(hike.getDate());
        ((TextView) findViewById(R.id.txtDetailLength)).setText(String.format("%s km", hike.getLength()));
        ((TextView) findViewById(R.id.txtDetailParking)).setText(hike.getParking());
        ((TextView) findViewById(R.id.txtDetailWeather)).setText(hike.getWeather());
        ((TextView) findViewById(R.id.txtDetailDescription)).setText(hike.getDescription());

        Chip chipDiff = findViewById(R.id.chipDifficulty);
        String diff = hike.getDifficulty();
        chipDiff.setText(diff);
        if (diff != null) {
            if (diff.equalsIgnoreCase("Easy")) {
                chipDiff.setChipBackgroundColorResource(R.color.diff_easy_bg);
                chipDiff.setTextColor(ContextCompat.getColor(this, R.color.diff_easy_text));
            } else if (diff.equalsIgnoreCase("Medium") || diff.equalsIgnoreCase("Moderate")) {
                chipDiff.setChipBackgroundColorResource(R.color.diff_medium_bg);
                chipDiff.setTextColor(ContextCompat.getColor(this, R.color.diff_medium_text));
            } else {
                chipDiff.setChipBackgroundColorResource(R.color.diff_hard_bg);
                chipDiff.setTextColor(ContextCompat.getColor(this, R.color.diff_hard_text));
            }
        }

        ImageView headerImg = findViewById(R.id.hike_detail_image);
        int resId = getResources().getIdentifier(hike.getImageResName(), "drawable", getPackageName());
        if (resId != 0) headerImg.setImageResource(resId);

        String currentUserId = FirebaseAuth.getInstance().getUid();
        MaterialButton btnDelete = findViewById(R.id.btnDeleteHike);
        MaterialButton btnEdit = findViewById(R.id.btnEditHike);
        
        if (currentUserId != null && currentUserId.equals(hike.getCreatorId())) {
            btnDelete.setVisibility(View.VISIBLE);
            btnEdit.setVisibility(View.VISIBLE);
        } else {
            btnDelete.setVisibility(View.GONE);
            btnEdit.setVisibility(View.GONE);
        }

        btnDelete.setOnClickListener(v -> confirmDelete());
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddHikeActivity.class);
            intent.putExtra("hike", hike);
            startActivity(intent);
        });

        findViewById(R.id.btnAddObservation).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddObservationActivity.class);
            intent.putExtra("hikeId", hike.getFirebaseId());
            startActivity(intent);
        });

        findViewById(R.id.fabFocusHike).setOnClickListener(v -> focusOnHikeLocation());

        findViewById(R.id.btnZoomIn).setOnClickListener(v -> {
            double currentZoom = mapView.getMapboxMap().getCameraState().getZoom();
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(currentZoom + 1.0).build());
        });

        findViewById(R.id.btnZoomOut).setOnClickListener(v -> {
            double currentZoom = mapView.getMapboxMap().getCameraState().getZoom();
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(currentZoom - 1.0).build());
        });

        RecyclerView rv = findViewById(R.id.recyclerViewObservations);
        rv.setLayoutManager(new LinearLayoutManager(this));
        firestoreHelper.getObservationsForHike(hike.getFirebaseId(), observations -> {
            ObservationAdapter adapter = new ObservationAdapter(observations);
            rv.setAdapter(adapter);
        });
    }

    private void initDetailMap() {
        mapView = findViewById(R.id.detailMapView);
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
            drawHikeMarker();
            focusOnHikeLocation();
        });
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

    private void drawHikeMarker() {
        AnnotationPlugin annotationApi = (AnnotationPlugin) mapView.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID);
        if (annotationApi != null && !plannedPoints.isEmpty()) {
            Point p = plannedPoints.get(0);

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
    }

    private void focusOnHikeLocation() {
        if (!plannedPoints.isEmpty()) {
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                    .center(plannedPoints.get(0))
                    .zoom(15.0)
                    .build());
        }
    }

    private List<Point> convertToPoints(List<Map<String, Double>> raw) {
        List<Point> points = new ArrayList<>();
        if (raw != null) {
            for (Map<String, Double> map : raw) {
                try {
                    Object latObj = map.get("lat");
                    Object lngObj = map.get("lng");
                    if (latObj != null && lngObj != null) {
                        double lat = Double.parseDouble(latObj.toString());
                        double lng = Double.parseDouble(lngObj.toString());
                        points.add(Point.fromLngLat(lng, lat));
                    }
                } catch (Exception ignored) {}
            }
        }
        return points;
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Hike")
                .setMessage("Are you sure you want to remove this journey?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    firestoreHelper.deleteHike(hike.getFirebaseId(), new FirestoreHelper.DataStatus() {
                        @Override public void dataIsLoaded(List<Hike> hikes) {}
                        @Override public void dataIsInserted() {}
                        @Override public void dataIsDeleted() {
                            Toast.makeText(HikeDetailActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
