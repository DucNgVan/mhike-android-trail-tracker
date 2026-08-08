package com.example.hiking_application_coursework.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hiking_application_coursework.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startMainActivity();
            return;
        }

        editEmail = findViewById(R.id.loginUsername);
        editPassword = findViewById(R.id.loginPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView txtForgotPassword = findViewById(R.id.txtForgotPassword);
        TextView txtGoToRegister = findViewById(R.id.txtGoToRegister);

        btnLogin.setOnClickListener(v -> handleLogin());
        txtForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        txtGoToRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void handleLogin() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) email = "test@gmail.com";
        if (TextUtils.isEmpty(password)) password = "123456";

        final String finalEmail = email;
        final String finalPassword = password;

        mAuth.signInWithEmailAndPassword(finalEmail, finalPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        startMainActivity();
                    } else {
                        // Auto-create test user if it doesn't exist yet
                        mAuth.createUserWithEmailAndPassword(finalEmail, finalPassword)
                                .addOnCompleteListener(createTask -> {
                                    if (createTask.isSuccessful()) {
                                        startMainActivity();
                                    } else {
                                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                                        Toast.makeText(LoginActivity.this, "Login failed: " + error, Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                });
    }

    private void showForgotPasswordDialog() {
        EditText resetMail = new EditText(this);
        resetMail.setHint("Enter your email");
        new AlertDialog.Builder(this)
                .setTitle("Reset Password?")
                .setMessage("We will send a reset link to your email.")
                .setView(resetMail)
                .setPositiveButton("Send", (dialog, which) -> {
                    String mail = resetMail.getText().toString().trim();
                    if (!TextUtils.isEmpty(mail)) {
                        mAuth.sendPasswordResetEmail(mail).addOnSuccessListener(aVoid -> 
                            Toast.makeText(LoginActivity.this, "Reset link sent!", Toast.LENGTH_SHORT).show()
                        ).addOnFailureListener(e -> 
                            Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
