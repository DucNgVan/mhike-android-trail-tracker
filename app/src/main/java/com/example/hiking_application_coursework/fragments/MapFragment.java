package com.example.hiking_application_coursework.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.hiking_application_coursework.R;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;

import java.util.Locale;

/**
 * Tab Map: User location display (My Location).
 */
public class MapFragment extends Fragment {

    private MapView mapView;
    private TextView txtLat, txtLng;
    private Point lastLocation;

    private final OnIndicatorPositionChangedListener locationListener = new OnIndicatorPositionChangedListener() {
        @Override
        public void onIndicatorPositionChanged(@NonNull Point point) {
            lastLocation = point;
            updateUI(point);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = view.findViewById(R.id.mapView);
        txtLat = view.findViewById(R.id.txtLat);
        txtLng = view.findViewById(R.id.txtLng);

        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> initLocationComponent());

        view.findViewById(R.id.fabMyLocation).setOnClickListener(v -> {
            if (lastLocation != null) {
                mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                        .center(lastLocation)
                        .zoom(15.0)
                        .build());
            } else {
                Toast.makeText(getContext(), "GPS signal not found", Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.btnZoomIn).setOnClickListener(v -> {
            double z = mapView.getMapboxMap().getCameraState().getZoom();
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(z + 1).build());
        });

        view.findViewById(R.id.btnZoomOut).setOnClickListener(v -> {
            double z = mapView.getMapboxMap().getCameraState().getZoom();
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(z - 1).build());
        });
    }

    private void updateUI(Point p) {
        if (txtLat != null) txtLat.setText(String.format(Locale.US, "%.6f°", p.latitude()));
        if (txtLng != null) txtLng.setText(String.format(Locale.US, "%.6f°", p.longitude()));
    }

    private void initLocationComponent() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationComponentPlugin locationPlugin = LocationComponentUtils.getLocationComponent(mapView);
            locationPlugin.setEnabled(true);
            locationPlugin.addOnIndicatorPositionChangedListener(locationListener);
            locationPlugin.updateSettings(settings -> {
                settings.setEnabled(true);
                settings.setPuckBearingEnabled(true);
                return null;
            });
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null) {
            LocationComponentUtils.getLocationComponent(mapView).removeOnIndicatorPositionChangedListener(locationListener);
        }
    }
}
