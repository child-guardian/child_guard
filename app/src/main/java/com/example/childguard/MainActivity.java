package com.example.childguard;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);

        bottomNavigationView.setOnNavigationItemSelectedListener(v -> {
            if (v.getItemId() == findViewById(R.id.navigation_home).getId()) {
                getSupportFragmentManager().beginTransaction()
                        .replace(findViewById(R.id.fragmentContainerView).getId(), HomeFragment.newInstance("test", "tset"))
                        .commit();
            } else if (v.getItemId() == findViewById(R.id.navigation_QR).getId()) {
                getSupportFragmentManager().beginTransaction()
                        .replace(findViewById(R.id.fragmentContainerView).getId(), QRFragment.newInstance("test", "tset"))
                        .commit();
            } else if (v.getItemId() == findViewById(R.id.navigation_notification).getId()) {
                getSupportFragmentManager().beginTransaction()
                        .replace(findViewById(R.id.fragmentContainerView).getId(), NotificationFragment.newInstance("test", "test"))
                        .commit();
            }

            return true;

        });
    }
}