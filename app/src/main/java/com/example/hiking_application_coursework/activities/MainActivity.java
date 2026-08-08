package com.example.hiking_application_coursework.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.hiking_application_coursework.R;
import com.example.hiking_application_coursework.fragments.HomeFragment;
import com.example.hiking_application_coursework.fragments.MapFragment;
import com.example.hiking_application_coursework.fragments.SearchFragment;
import com.example.hiking_application_coursework.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // KIỂM TRA BẢO MẬT: Nếu chưa đăng nhập thì bắt quay lại Login
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();
            
            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_map) {
                selectedFragment = new MapFragment();
            } else if (id == R.id.nav_search) {
                selectedFragment = new SearchFragment();
            } else if (id == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }
            
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });
    }

    public void navigateToHomeTab() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    @Override
    public void onBackPressed() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null && bottomNav.getSelectedItemId() != R.id.nav_home) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        } else {
            super.onBackPressed();
        }
    }
}
