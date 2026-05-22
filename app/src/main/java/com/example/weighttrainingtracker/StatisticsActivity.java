package com.example.weighttrainingtracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView;
import androidx.annotation.Nullable;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.util.Log;

import android.view.View;
import android.view.LayoutInflater;

import android.widget.CalendarView;

public class StatisticsActivity extends BaseActivity {
    private static final String TAG = "StatisticsActivity";
    private Spinner workoutSpinner;
    private BarChart barChart;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeSetter.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        setupNavigationDrawer();

        workoutSpinner = findViewById(R.id.workout_spinner);
        barChart = findViewById(R.id.bar_chart);

        setupNavigationDrawer();

        setupSpinner();

        testCalculateDailyVolume();
    }

    private void setupSpinner() {
        List<String> workoutTypes = new ArrayList<>();
        workoutTypes.add("Select Workout");
        workoutTypes.add("Bench Press");
        workoutTypes.add("Squats");
        workoutTypes.add("Deadlifts");
        workoutTypes.add("Total Volume");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, workoutTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        workoutSpinner.setAdapter(adapter);

        workoutSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedWorkout = workoutTypes.get(position);
                if (!selectedWorkout.equals("Select Workout")) {
                    printAllSharedPreferencesData();
                    updateChart(selectedWorkout);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                
            }
        });
    }

    // Builds bar chart upon workoutType input and calculateWeeklyVolume output 
    private void updateChart(String workoutType) {
        List<BarEntry> entries = new ArrayList<>();

        for (int i = 1; i <= 12; i++) {
            float weeklyVolume = calculateWeeklyVolume(workoutType, i);
            Log.d(TAG, "Week " + i + " - " + workoutType + " Volume: " + weeklyVolume);
            entries.add(new BarEntry(i, weeklyVolume));
        }

        BarDataSet dataSet = new BarDataSet(entries, workoutType + " Volume");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.invalidate(); 
    }

    // Method for conducting getDateKeyForWeekDay & calculateDailyVolume
    private float calculateWeeklyVolume(String workoutType, int weekNumber) {
        SharedPreferences prefs = getSharedPreferences(CalendarActivity.PREFS_NAME, Context.MODE_PRIVATE);
        float totalVolume = 0f;

        for (int i = 0; i < 7; i++) {
            String dateKey = getDateKeyForWeekDay(weekNumber, i);
            Log.d("StatisticsActivity", "Retrieving data for key: " + CalendarActivity.DATA_KEY + dateKey);
            String dayData = prefs.getString(CalendarActivity.DATA_KEY + dateKey, "KeyDoesNotExist");
            Log.d("StatisticsActivity", "Date Key: " + dateKey + " - Data: " + dayData);

            float dailyVolume = calculateDailyVolume(dayData, workoutType);
            Log.d("StatisticsActivity", "Daily Volume for " + dateKey + ": " + dailyVolume);

            totalVolume += dailyVolume;
        }

        Log.d("StatisticsActivity", "Total Volume for Week " + weekNumber + ": " + totalVolume);
        return totalVolume;
    }

    // Parsing and extraction method
    private float calculateDailyVolume(String dayData, String workoutType) {
        float dailyVolume = 0f;
        Log.d(TAG, "Day Data: " + dayData);

        if (dayData != null && !dayData.isEmpty() && !dayData.equals("KeyDoesNotExist")) {
            String[] entries = dayData.split("\n");
            String currentWorkout = null;

            for (String entry : entries) {
                entry = entry.trim(); 
                if (entry.isEmpty()) continue; 
                Log.d(TAG, "Entry: " + entry);

                if (entry.startsWith("Workout: ")) {
                    currentWorkout = entry.substring("Workout: ".length()).trim();
                    Log.d(TAG, "Current Workout: " + currentWorkout);
                } else if (currentWorkout != null && currentWorkout.equals(workoutType) && entry.startsWith("Set ")) {
                    Log.d(TAG, "Processing set entry: " + entry);
                    try {
                        String[] setDetails = entry.split(": ", 2)[1].split(", ");
                        Log.d(TAG, "Split Entry: " + Arrays.toString(setDetails));
                        float weight = 0;
                        int reps = 0;

                        for (String detail : setDetails) {
                            detail = detail.trim();
                            Log.d(TAG, "Set Detail: " + detail);
                            if (detail.startsWith("Weight: ")) {
                                String weightStr = detail.substring("Weight: ".length()).replace(" kg", "").trim();
                                Log.d(TAG, "Weight String: " + weightStr);
                                weight = Float.parseFloat(weightStr);
                                Log.d(TAG, "Parsed Weight: " + weight);
                            } else if (detail.startsWith("Reps: ")) {
                                String repsStr = detail.substring("Reps: ".length()).trim();
                                Log.d(TAG, "Reps String: " + repsStr);
                                reps = Integer.parseInt(repsStr);
                                Log.d(TAG, "Parsed Reps: " + reps);
                            }
                        }

                        float setVolume = weight * reps;
                        Log.d(TAG, "Calculated Volume for Set: " + setVolume);
                        dailyVolume += setVolume;
                        Log.d(TAG, "Daily Volume Updated: " + dailyVolume);
                    } catch (Exception e) {
                        Log.e(TAG, "Malformed weight and reps data: " + entry, e);
                    }
                }
            }
        } else {
            Log.d(TAG, "No valid data found for day.");
        }

        Log.d(TAG, "Final Daily Volume for " + workoutType + ": " + dailyVolume);
        return dailyVolume;
    }

    // Helper method to get the date key for a specific week and day 
    private String getDateKeyForWeekDay(int relativeWeek, int dayOfWeek) {
        Calendar calendar = Calendar.getInstance();

        // Get current week number
        int currentWeekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);

        // Calculate the target week number for the last 12 weeks
        int targetWeekOfYear = currentWeekOfYear - (12 - relativeWeek);

        // Move calendar to that week
        calendar.set(Calendar.WEEK_OF_YEAR, targetWeekOfYear);

        // Force monday as first day of the week
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        // Move to a specific weekday
        calendar.add(Calendar.DAY_OF_WEEK, dayOfWeek);

        // Converting the date to storage key format
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateKey = dateFormat.format(calendar.getTime());

        Log.d("StatisticsActivity", "Generated date key: " + dateKey); 
        return dateKey;
    }

    // Debugging helper method 
    private void printAllSharedPreferencesData() {
        SharedPreferences prefs = getSharedPreferences(CalendarActivity.PREFS_NAME, Context.MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("SharedPreferencesData", entry.getKey() + ": " + entry.getValue().toString());
        }
    }

    // Unit Tests for the most critical method (calculateDailyVolume)

    // ^Checks for the specific workout type string within all entries of a day / Takes the following
    // two numbers and multiplies them / Adds all results and checks whether the math adds up
    @Test
    public void testCalculateDailyVolume() {
    String data =
        "Workout: Bench Press\n" +
        "Set 1: Weight: 100 kg, Reps: 5\n" +
        "Set 2: Weight: 80 kg, Reps: 10\n";
    
    String testWorkoutType = "Bench Press";

    float result = calculateDailyVolume(data, testWorkoutType);

    assertEquals(1300f, result, 0.01f);
    }

    // Testing for ignoring wrong workout 
    @Test
public void testWorkoutFiltering() {
    String data =
            "Workout: Squats\n" +
            "Set 1: Weight: 120 kg, Reps: 5\n";

    float result = calculateDailyVolume(data, "Bench Press");

    assertEquals(0f, result, 0.01f);
    }

    // Tests whether the method correctly seperates workout types
    @Test
public void testMultipleWorkouts() {
    String data =
            "Workout: Bench Press\n" +
            "Set 1: Weight: 100 kg, Reps: 5\n" +
            "Workout: Squats\n" +
            "Set 1: Weight: 150 kg, Reps: 5\n";

    float result = calculateDailyVolume(data, "Bench Press");

    assertEquals(500f, result, 0.01f);
    }

    // Testing try and catch logic
    @Test
public void testMalformedData() {
    String data =
            "Workout: Bench Press\n" +
            "Set 1: INVALID DATA\n";

    float result = calculateDailyVolume(data, "Bench Press");

    assertEquals(0f, result, 0.01f);
    }

}

