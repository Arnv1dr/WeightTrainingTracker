package com.example.weighttrainingtracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class GoalsActivity extends BaseActivity {
    private Spinner workoutSpinner;
    private EditText goalInput;
    private Button saveGoalButton;
    private TextView savedGoalText;
    private Button compareButton;

    private Spinner weekSpinner1, weekSpinner2;
    private TextView volumeChangeText, comparisonResultText;
    private String selectedWorkout;
    private int selectedWeek1, selectedWeek2;

    private SharedPreferences preferences;
    public static final String PREFS_NAME = "GoalsData";
    public static final String GOAL_KEY = "goal_";

    public static final String TAG = "GoalsActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeSetter.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);
        setupNavigationDrawer();

        workoutSpinner = findViewById(R.id.workout_spinner);
        goalInput = findViewById(R.id.goal_input);
        saveGoalButton = findViewById(R.id.save_goal_button);
        savedGoalText = findViewById(R.id.saved_goal_text);
        compareButton = findViewById(R.id.compare_button);

        weekSpinner1 = findViewById(R.id.week1_spinner);
        weekSpinner2 = findViewById(R.id.week2_spinner);
        volumeChangeText = findViewById(R.id.volume_change_text);
        comparisonResultText = findViewById(R.id.comparison_result_text);

        preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Creates a list of workouts in the dropdown menu
        List<String> workoutTypes = new ArrayList<>();
        workoutTypes.add("Select Workout");
        workoutTypes.add("Bench Press");
        workoutTypes.add("Squats");
        workoutTypes.add("Deadlifts");


        // Converts java data into visual UI elements
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, workoutTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        workoutSpinner.setAdapter(adapter);

        // Listens to user selection of the drop down and loads the saved goal for the workout selected 
        // + displays the calculated volume change of the last 2 weeks
        workoutSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedWorkout = workoutSpinner.getSelectedItem().toString();
                loadSavedGoal();
                displayDefaultVolumeChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                
            }
        });

        saveGoalButton.setOnClickListener(v -> saveGoal());

        compareButton.setOnClickListener(v -> compareVolumesForSelectedWeeks());

        populateWeekSpinners();
        displayDefaultVolumeChange();

        // Listeners that allow the user to select two weeks from the last 12 to be compared regarding volume change
        // +2 because the spinner position starts at 0 and the current week is 1 (relative week = 1) therefore
        // 0 + 2 = Last week, 0 + 3 = Week before last... 
        weekSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedWeek1 = position + 2;
                compareVolumesForSelectedWeeks();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        weekSpinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedWeek2 = position + 2;
                compareVolumesForSelectedWeeks();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // Displays saved percentage goal for the selected workout by fetching from SharedPreferences (local storage)
    private void loadSavedGoal() {
        if (selectedWorkout != null && !selectedWorkout.equals("Select Workout")) {
            float savedGoal = preferences.getFloat(GOAL_KEY + selectedWorkout, 0);
            savedGoalText.setText(String.format("Current Goal: %.2f%%", savedGoal));
        }
    }

    // Reads user input, validates input, converts text to float and stores input. 
    private void saveGoal() {
        String goalStr = goalInput.getText().toString();
        if (goalStr.isEmpty()) {
            Toast.makeText(this, "Please enter a goal", Toast.LENGTH_SHORT).show();
            return;
        }

        float goal = Float.parseFloat(goalStr);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(GOAL_KEY + selectedWorkout, goal);
        editor.apply();

        savedGoalText.setText(String.format("Current Goal: %.2f%%", goal));
        Toast.makeText(this, "Goal saved", Toast.LENGTH_SHORT).show();
    }


    // Show recent progress (last two weeks) automatically 
    @SuppressLint("DefaultLocale")
    private void displayDefaultVolumeChange() {
        float volumeChange = calculateVolumeChangeForLastTwoWeeks();
        volumeChangeText.setText(String.format("Weekly Volume Change: %.2f%%", volumeChange));
    }

    private float calculateVolumeChangeForLastTwoWeeks() {
        int mostRecentWeek = 2;  
        int weekBeforeLast = 3;  

        float volumeLastWeek = calculateWeeklyVolume(selectedWorkout, mostRecentWeek);
        float volumeWeekBefore = calculateWeeklyVolume(selectedWorkout, weekBeforeLast);
        Log.d("GoalsActivity", "Volume Last Week: " + volumeLastWeek + " - Volume Week Before: " + volumeWeekBefore);

        // Divide by 0 protection
        if (volumeWeekBefore == 0) {
            return 0.0f;
        }

        return ((volumeLastWeek - volumeWeekBefore) / volumeWeekBefore) * 100;
    }

    private void populateWeekSpinners() {
        List<String> weekLabels = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            weekLabels.add(i + "Week/s ago ");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, weekLabels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weekSpinner1.setAdapter(adapter);
        weekSpinner2.setAdapter(adapter);
    }

    @SuppressLint("DefaultLocale")
    private void compareVolumesForSelectedWeeks() {
        float comparisonResult = compareVolumesForWeeks(selectedWeek1, selectedWeek2);
        comparisonResultText.setText(String.format("Comparison Result: %.2f%%", comparisonResult));
    }

    private float compareVolumesForWeeks(int week1, int week2) {
        float volumeWeek1 = calculateWeeklyVolume(selectedWorkout, week1);
        float volumeWeek2 = calculateWeeklyVolume(selectedWorkout, week2);

        if (volumeWeek2 == 0) {
            return 0.0f;
        }

        return ((volumeWeek1 - volumeWeek2) / volumeWeek2) * 100;
    }

    // Method to calculate weekly volume (similar to the one in StatisticsActivity)
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

        
        calendar.add(Calendar.WEEK_OF_YEAR, -(relativeWeek - 1));  

        
        calendar.setFirstDayOfWeek(Calendar.MONDAY);

        
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        
        calendar.add(Calendar.DAY_OF_WEEK, dayOfWeek);

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateKey = dateFormat.format(calendar.getTime());

        Log.d("GoalsActivity", "Generated date key: " + dateKey); 
        return dateKey;
    }
}