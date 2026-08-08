package com.example.hiking_application_coursework.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.hiking_application_coursework.R;
import com.example.hiking_application_coursework.activities.LoginActivity;
import com.example.hiking_application_coursework.helpers.FirestoreHelper;
import com.example.hiking_application_coursework.models.Hike;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private EditText editName, editPhone;
    private TextView txtEmail, txtTotalHikes, txtTotalDistance, txtTotalHours;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirestoreHelper firestoreHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firestoreHelper = new FirestoreHelper();

        editName = view.findViewById(R.id.profileFullName);
        editPhone = view.findViewById(R.id.profilePhone);
        txtEmail = view.findViewById(R.id.profileEmail);
        
        txtTotalHikes = view.findViewById(R.id.profileTotalHikes);
        txtTotalDistance = view.findViewById(R.id.profileTotalDistance);
        txtTotalHours = view.findViewById(R.id.profileTotalHours);

        loadUserData();
        loadUserStats();

        view.findViewById(R.id.btnUpdateProfile).setOnClickListener(v -> updateProfile());
        view.findViewById(R.id.btnSeedSampleData).setOnClickListener(v -> confirmAndSeedSampleData());
        view.findViewById(R.id.btnResetData).setOnClickListener(v -> confirmAndResetData());
        view.findViewById(R.id.btnLogoutProfile).setOnClickListener(v -> signOut());

        return view;
    }

    private void confirmAndSeedSampleData() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Add Sample Vietnam Data")
                .setMessage("This will reset current data and populate 6 realistic Vietnam hiking locations (Fansipan, Son Doong, Langbiang, Marble Mountains, Ta Nang, Ba Den). Proceed?")
                .setPositiveButton("Add Sample Data", (dialog, which) -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    String uid = user != null ? user.getUid() : "guest";
                    firestoreHelper.seedSampleVietnamData(uid, () -> {
                        Toast.makeText(getContext(), "Sample Vietnam Data Loaded!", Toast.LENGTH_SHORT).show();
                        loadUserStats();
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmAndResetData() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Reset All Data")
                .setMessage("Are you sure you want to delete all hike records and observations from Firestore?")
                .setPositiveButton("Reset All", (dialog, which) -> {
                    firestoreHelper.wipeAllData(() -> {
                        Toast.makeText(getContext(), "All Data Reset!", Toast.LENGTH_SHORT).show();
                        loadUserStats();
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            editName.setText(user.getDisplayName());
            txtEmail.setText(user.getEmail());

            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            editPhone.setText(documentSnapshot.getString("phone"));
                        }
                    });
        }
    }

    private void loadUserStats() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        firestoreHelper.getAllHikes(new FirestoreHelper.DataStatus() {
            @Override
            public void dataIsLoaded(List<Hike> hikes) {
                int count = 0;
                double distance = 0;
                double hours = 0;

                for (Hike h : hikes) {
                    if (user.getUid().equals(h.getCreatorId())) {
                        count++;
                        try {
                            if (h.getLength() != null) distance += Double.parseDouble(h.getLength());
                            if (h.getDuration() != null) hours += Double.parseDouble(h.getDuration());
                        } catch (Exception ignored) {}
                    }
                }

                txtTotalHikes.setText(String.valueOf(count));
                txtTotalDistance.setText(String.format(Locale.US, "%.1f km", distance));
                txtTotalHours.setText(String.format(Locale.US, "%.1f h", hours));
            }
            @Override public void dataIsInserted() {}
            @Override public void dataIsDeleted() {}
        });
    }

    private void updateProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        String newName = editName.getText().toString().trim();
        String newPhone = editPhone.getText().toString().trim();

        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();

            user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                Map<String, Object> data = new HashMap<>();
                data.put("phone", newPhone);
                data.put("name", newName);
                db.collection("users").document(user.getUid()).set(data)
                        .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Profile Updated!", Toast.LENGTH_SHORT).show());
            });
        }
    }

    private void signOut() {
        mAuth.signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
