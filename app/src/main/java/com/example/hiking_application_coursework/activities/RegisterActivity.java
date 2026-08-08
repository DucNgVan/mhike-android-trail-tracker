package com.example.hiking_application_coursework.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hiking_application_coursework.R;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText editFullName, editUsername, editPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        editFullName = findViewById(R.id.registerFullName);
        editUsername = findViewById(R.id.registerUsername);
        editPassword = findViewById(R.id.registerPassword);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView txtGoToLogin = findViewById(R.id.txtGoToLogin);

        btnRegister.setOnClickListener(v -> handleRegister());
        txtGoToLogin.setOnClickListener(v -> finish());
    }

    private void handleRegister() {
        String fullName = editFullName.getText().toString().trim();
        String email = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        android.util.Log.e("FirebaseRegister", "Error: " + error);
                        Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                        
                        if (error != null && error.contains("CONFIGURATION_NOT_FOUND")) {
                            Toast.makeText(this, "IMPORTANT: Please ENABLE 'Email/Password' in Firebase Console -> Authentication", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
