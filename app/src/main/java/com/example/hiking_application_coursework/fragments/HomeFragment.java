package com.example.hiking_application_coursework.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hiking_application_coursework.R;
import com.example.hiking_application_coursework.activities.AddHikeActivity;
import com.example.hiking_application_coursework.activities.HikeDetailActivity;
import com.example.hiking_application_coursework.activities.LoginActivity;
import com.example.hiking_application_coursework.adapters.HikeAdapter;
import com.example.hiking_application_coursework.helpers.FirestoreHelper;
import com.example.hiking_application_coursework.models.Hike;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private HikeAdapter adapter;
    private FirestoreHelper firestoreHelper;
    private ChipGroup chipGroupFilters;
    private List<Hike> allHikes = new ArrayList<>();
    private View btnLogout;
    
    private TextView txtGreeting, txtStatHikes, txtStatDistance, txtStatHours;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        firestoreHelper = new FirestoreHelper();
        recyclerView = view.findViewById(R.id.homeRecyclerViewHikes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chipGroupFilters = view.findViewById(R.id.chipGroupFilters);
        btnLogout = view.findViewById(R.id.imgLogout);
        
        txtGreeting = view.findViewById(R.id.txtUserGreeting);
        txtStatHikes = view.findViewById(R.id.txtStatHikes);
        txtStatDistance = view.findViewById(R.id.txtStatDistance);
        txtStatHours = view.findViewById(R.id.txtStatHours);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            txtGreeting.setText(String.format("Hello, %s", (name != null ? name : "Explorer")));
        }

        view.findViewById(R.id.fabAddHike).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddHikeActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> signOut());
        
        // Tap title to re-seed sample Vietnam hikes
        view.findViewById(R.id.txtAppName).setOnClickListener(v -> {
            FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
            String uid = u != null ? u.getUid() : "guest";
            firestoreHelper.seedSampleVietnamData(uid, () -> {
                Toast.makeText(getContext(), "Vietnam Sample Data Reseeded!", Toast.LENGTH_SHORT).show();
                loadHikes();
            });
        });

        setupFilters();
        loadHikes();

        return view;
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void setupFilters() {
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                if (adapter != null) adapter.setHikes(allHikes);
                return;
            }
            int chipId = checkedIds.get(0);
            Chip chip = group.findViewById(chipId);
            filterHikesByDifficulty(chip.getText().toString());
        });
    }

    private void filterHikesByDifficulty(String category) {
        if (category == null || category.equalsIgnoreCase("All")) {
            adapter.setHikes(allHikes);
            return;
        }

        List<Hike> filtered = new ArrayList<>();
        for (Hike h : allHikes) {
            if (h.getDifficulty() != null && h.getDifficulty().equalsIgnoreCase(category)) {
                filtered.add(h);
            }
        }
        adapter.setHikes(filtered);
    }

    private void loadHikes() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user != null ? user.getUid() : "guest";

        firestoreHelper.getAllHikes(new FirestoreHelper.DataStatus() {
            @Override
            public void dataIsLoaded(List<Hike> hikes) {
                if (hikes == null || hikes.isEmpty()) {
                    firestoreHelper.seedSampleVietnamData(uid, () -> loadHikes());
                    return;
                }
                allHikes = hikes;
                updateStats(hikes);
                adapter = new HikeAdapter(hikes, hike -> {
                    Intent intent = new Intent(getActivity(), HikeDetailActivity.class);
                    intent.putExtra("hike", hike);
                    startActivity(intent);
                });
                recyclerView.setAdapter(adapter);
            }
            @Override public void dataIsInserted() {}
            @Override public void dataIsDeleted() {}
        });
    }

    private void updateStats(List<Hike> hikes) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        int count = 0;
        double distance = 0;
        double hours = 0;

        for (Hike h : hikes) {
            if (user.getUid().equals(h.getCreatorId())) {
                count++;
                try {
                    distance += Double.parseDouble(h.getLength());
                    hours += Double.parseDouble(h.getDuration());
                } catch (Exception ignored) {}
            }
        }

        txtStatHikes.setText(String.valueOf(count));
        txtStatDistance.setText(String.format(Locale.getDefault(), "%.1f km", distance));
        txtStatHours.setText(String.format(Locale.getDefault(), "%.1f h", hours));
    }

    private void confirmAndResetData() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Wipe All Data?");
        builder.setMessage("This will permanently delete all records and seed fresh English sample data.");
        builder.setPositiveButton("Wipe & Reset", (dialog, which) -> resetEverything());
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void resetEverything() {
        Toast.makeText(getContext(), "Wiping cloud database...", Toast.LENGTH_LONG).show();
        firestoreHelper.wipeAllData(this::seedVietnamData);
    }

    private void seedVietnamData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user != null ? user.getUid() : "unknown";
        String uname = user != null ? user.getDisplayName() : "Admin";

        List<Hike> samples = new ArrayList<>();
        
        Hike h1 = new Hike(1, "Fansipan Summit", "Lao Cai, Vietnam", "02/08/2026", "Yes", "15", "Hard", "The Roof of Indochina", "Sunny", "Local guide", "img1");
        h1.setDuration("10"); h1.setCreatorId(uid); h1.setCreatorName(uname);
        h1.setPlannedRoute(createPoint(22.3033, 103.7750));
        samples.add(h1);

        Hike h2 = new Hike(2, "Lang Biang Peak", "Da Lat, Vietnam", "05/08/2026", "Yes", "8", "Medium", "Cloudy Da Lat View", "Cool", "Friends", "img2");
        h2.setDuration("4"); h2.setCreatorId(uid); h2.setCreatorName(uname);
        h2.setPlannedRoute(createPoint(12.0431, 108.4411));
        samples.add(h2);

        Hike h3 = new Hike(3, "Ba Vi Forest", "Hanoi, Vietnam", "10/08/2026", "Yes", "5", "Easy", "Green lungs of the capital", "Foggy", "Family", "img3");
        h3.setDuration("2"); h3.setCreatorId(uid); h3.setCreatorName(uname);
        h3.setPlannedRoute(createPoint(21.0811, 105.3700));
        samples.add(h3);

        Hike h4 = new Hike(4, "Marble Mountain", "Da Nang, Vietnam", "20/08/2026", "No", "3", "Easy", "Caves and ancient temples", "Hot", "Alone", "img5");
        h4.setDuration("1.5"); h4.setCreatorId(uid); h4.setCreatorName(uname);
        h4.setPlannedRoute(createPoint(15.9995, 108.2635));
        samples.add(h4);

        for (int i = 0; i < samples.size(); i++) {
            final boolean isLast = (i == samples.size() - 1);
            firestoreHelper.insertHike(samples.get(i), new FirestoreHelper.DataStatus() {
                @Override public void dataIsLoaded(List<Hike> hikes) {}
                @Override public void dataIsInserted() {
                    if (isLast) {
                        Toast.makeText(getContext(), "Data Reset Complete!", Toast.LENGTH_SHORT).show();
                        loadHikes();
                    }
                }
                @Override public void dataIsDeleted() {}
            });
        }
    }

    private List<Map<String, Double>> createPoint(double lat, double lng) {
        List<Map<String, Double>> list = new ArrayList<>();
        Map<String, Double> p = new HashMap<>();
        p.put("lat", lat);
        p.put("lng", lng);
        list.add(p);
        return list;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHikes();
    }
}
