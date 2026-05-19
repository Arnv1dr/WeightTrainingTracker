package com.example.weighttrainingtracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class BaseActivity extends AppCompatActivity {

    protected LinearLayout navDrawer;
    private boolean isNavOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupNavigationDrawer() {
        navDrawer = findViewById(R.id.nav_drawer);

        // Handle window insets to adjust for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED; // Ensure the correct return type
        });

        Button openNavButton = findViewById(R.id.open_nav_button);
        openNavButton.setOnClickListener(v -> {
            if (isNavOpen) {
                closeNavDrawer();
            } else {
                openNavDrawer();
            }
        });

        // Find the Workouts button in the nav drawer
        Button workoutsButton = findViewById(R.id.workouts_button);
        workoutsButton.setOnClickListener(v -> {
            Intent intent = new Intent(BaseActivity.this, CalendarActivity.class);
            startActivity(intent);
        });

        // Find the Statistics button in the nav drawer
        Button statisticsButton = findViewById(R.id.statistics_button);
        // Handle Statistics button click
        statisticsButton.setOnClickListener(v -> {
            Intent intent = new Intent(BaseActivity.this, StatisticsActivity.class);
            startActivity(intent);
        });
        // Find the Statistics button in the nav drawer
        Button homeButton = findViewById(R.id.home_button);
        // Handle Statistics button click
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(BaseActivity.this, MainActivity.class);
            startActivity(intent);
        });
        // Find the Statistics button in the nav drawer
        Button goalsButton = findViewById(R.id.goals_button);
        // Handle Statistics button click
        goalsButton.setOnClickListener(v -> {
            Intent intent = new Intent(BaseActivity.this, GoalsActivity.class);
            startActivity(intent);
        });
    }

    private void openNavDrawer() {
        navDrawer.setVisibility(View.VISIBLE);
        Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
        navDrawer.startAnimation(slideIn);
        isNavOpen = true;
    }

    private void closeNavDrawer() {
        Animation slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
        navDrawer.startAnimation(slideOut);
        navDrawer.setVisibility(View.GONE);
        isNavOpen = false;
    }
}