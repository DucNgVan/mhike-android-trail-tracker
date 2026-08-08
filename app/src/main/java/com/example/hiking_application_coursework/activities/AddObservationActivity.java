package com.example.hiking_application_coursework.activities;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hiking_application_coursework.R;
import com.example.hiking_application_coursework.helpers.FirestoreHelper;
import com.example.hiking_application_coursework.models.Observation;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Calendar;
import java.util.Locale;

public class AddObservationActivity extends AppCompatActivity {

    private EditText editObsName, editObsTime, editObsComments;
    private String hikeId;
    private FirestoreHelper firestoreHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_observation);

        hikeId = getIntent().getStringExtra("hikeId");
        firestoreHelper = new FirestoreHelper();

        MaterialToolbar toolbar = findViewById(R.id.obsToolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        editObsName = findViewById(R.id.editObsName);
        editObsTime = findViewById(R.id.editObsTime);
        editObsComments = findViewById(R.id.editObsComments);

        editObsTime.setOnClickListener(v -> showTimePicker());
        findViewById(R.id.btnSaveObservation).setOnClickListener(v -> saveObservation());
    }

    private void showTimePicker() {
        final Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> 
                editObsTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void saveObservation() {
        String name = editObsName.getText().toString().trim();
        String time = editObsTime.getText().toString().trim();
        String comments = editObsComments.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(time)) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Observation obs = new Observation();
        obs.setObservation(name);
        obs.setTime(time);
        obs.setComments(comments);
        obs.setHikeFirebaseId(hikeId);

        firestoreHelper.insertObservation(obs, documentReference -> {
            Toast.makeText(AddObservationActivity.this, "Observation Saved!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
