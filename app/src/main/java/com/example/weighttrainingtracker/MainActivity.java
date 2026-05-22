package com.example.weighttrainingtracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent; // Import the Intent class


public class MainActivity extends AppCompatActivity {

    private LinearLayout navDrawer;
    private boolean isNavOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Applies the theme before setting the content view
        ThemeSetter.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navDrawer = findViewById(R.id.nav_drawer);

        // Handles window insets to adjust for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED; // Ensures the correct return type
        });

        // Sets up the theme toggle button
        Button themeToggleButton = findViewById(R.id.theme_toggle_button);
        themeToggleButton.setOnClickListener(v -> {
            Log.d("ThemeToggle", "Theme toggle button clicked");

            // Toggle theme between light and dark
            int currentTheme = ThemeSetter.getThemePreference(MainActivity.this);
            int newTheme = (currentTheme == ThemeSetter.THEME_LIGHT) ? ThemeSetter.THEME_DARK : ThemeSetter.THEME_LIGHT;

            // Saves the new theme preference
            ThemeSetter.setThemePreference(MainActivity.this, newTheme);

            // Debug log to confirm theme change
            Log.d("ThemeToggle", "Theme set to: " + (newTheme == ThemeSetter.THEME_DARK ? "Dark" : "Light"));

            // Restarts the application to apply the new theme
            Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Finish current activity to ensure it is not kept in the stack
        });

        Button openNavButton = findViewById(R.id.open_nav_button);
        openNavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNavOpen) {
                    closeNavDrawer();
                } else {
                    openNavDrawer();
                }
            }
        });

        View imageButton = findViewById(R.id.imageView2);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNavOpen) {
                    closeNavDrawer();
                } else {
                    openNavDrawer();
                }
            }
        });

        // Find the Workouts, Statistics & Goals button in the nav drawer
        Button workoutsButton = findViewById(R.id.workouts_button);
        workoutsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
                startActivity(intent);
            }
        });
        Button statisticsButton = findViewById(R.id.stats_button);
        statisticsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
                startActivity(intent);
            }
        });
        Button goalsButton = findViewById(R.id.goals_button);
        goalsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GoalsActivity.class);
                startActivity(intent);
            }
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


