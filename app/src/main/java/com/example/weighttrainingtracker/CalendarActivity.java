package com.example.weighttrainingtracker;

import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarActivity extends BaseActivity {

    private static final String TAG = "CalendarActivity";

    private CalendarView calendarView;
    private Spinner workoutSpinner;
    private LinearLayout setContainer;
    private Button saveButton;
    private Button viewWorkoutButton;
    private Button deleteLastSetButton;
    private String selectedDate;

    public static final String PREFS_NAME = "CalendarData";
    public static final String DATA_KEY = "data_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeSetter.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // findViewById = Getting references to UI elements from the layout
        calendarView = findViewById(R.id.calendar_view);
        // Makes the week start on Monday instead of Sunday 
        calendarView.setFirstDayOfWeek(Calendar.MONDAY);
        workoutSpinner = findViewById(R.id.workout_spinner);
        setContainer = findViewById(R.id.set_container);
        saveButton = findViewById(R.id.save_button);
        viewWorkoutButton = findViewById(R.id.view_workout_button);
        deleteLastSetButton = findViewById(R.id.delete_last_set_button);

        // Initializes the side navigation drawer menu
        setupNavigationDrawer();

        // Setting up workout types for the dropdown menu
        List<String> workoutTypes = new ArrayList<>();
        workoutTypes.add("Select Workout");
        workoutTypes.add("Bench Press");
        workoutTypes.add("Squats");
        workoutTypes.add("Deadlifts");

        // Converts the list into dropdown items
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, workoutTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        workoutSpinner.setAdapter(adapter);

        // Spinner for immediate reaction of workout selection (currently no use but was reused in StatisticsActivity)
        workoutSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Setting a listener to handle date selection
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            // Converts the date into a specific format
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedDate = dateFormat.format(calendar.getTime());
            Log.d("CalendarActivity", "Selected date: " + selectedDate);  // Debugging help
            // Loads workout data for the date clicked
            loadDataForSelectedDate();
        });

        // Execute specific functions on button clicks
        saveButton.setOnClickListener(v -> saveDataForSelectedDate());
        viewWorkoutButton.setOnClickListener(v -> showWorkoutSummary());
        deleteLastSetButton.setOnClickListener(v -> deleteLastSet());
    }

    private void loadDataForSelectedDate() {
        Log.d(TAG, "Loading data for selected date: " + selectedDate);

        // Pull saved text data from SharedPreferences 
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Each date is saved as a seperate key so each day has independent workouts
        String data = prefs.getString(DATA_KEY + selectedDate, "");

        Log.d(TAG, "Data: " + data);

        // Clear previous sets
        setContainer.removeAllViews();

        if (!data.isEmpty()) {
            // Saved text is split by lines
            String[] lines = data.split("\n");
            if (lines.length > 0) {
                // Set workout type
                String workoutType = lines[0].replace("Workout: ", "").trim();
                workoutSpinner.setSelection(((ArrayAdapter<String>) workoutSpinner.getAdapter()).getPosition(workoutType));

                // Add sets
                for (int i = 1; i < lines.length; i++) {
                    String line = lines[i].trim();
                    if (line.isEmpty()) continue;

                    if (line.startsWith("Set ")) {
                        String[] setDetails = line.split(": ");
                        if (setDetails.length > 1) {
                            String[] weightAndReps = setDetails[1].split(", ");
                            if (weightAndReps.length >= 2) {
                                String weight = weightAndReps[0].replace("Weight: ", "").replace(" kg", "").trim();
                                String reps = weightAndReps[1].replace("Reps: ", "").trim();
                                
                                // Extract weights and reps 
                                View setView = getLayoutInflater().inflate(R.layout.set_input_layout, null);
                                EditText weightInput = setView.findViewById(R.id.weight_input);
                                EditText repsInput = setView.findViewById(R.id.reps_input);
                                weightInput.setText(weight);
                                repsInput.setText(reps);

                                setContainer.addView(setView);
                            }
                        }
                    }
                }
            }
        }

        // Adds a new set input for additional entries
        addNewSetInput();
    }

    // Method to add a new set input
    private void addNewSetInput() {
        View setView = getLayoutInflater().inflate(R.layout.set_input_layout, null);
        setContainer.addView(setView);
    }

    private void saveDataForSelectedDate() {
        Log.d(TAG, "Saving data for selected date: " + selectedDate);

        String selectedWorkout = workoutSpinner.getSelectedItem().toString();

        // Notify the user when user tries to save workout data without selecting workout type
        if (selectedWorkout.equals("Select Workout")) {
            Toast.makeText(this, "Please select a workout", Toast.LENGTH_SHORT).show();
            return;
        }

        // No date is selected when no date is clicked (can happen when user saves data for current day and 
        // assumes default)
        if (selectedDate == null) {
            Calendar calendar = Calendar.getInstance();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedDate = dateFormat.format(calendar.getTime());
        }

        // Get existing data
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String existingData = prefs.getString(DATA_KEY + selectedDate, "");

        StringBuilder dataBuilder = new StringBuilder();
        StringBuilder newSetsBuilder = new StringBuilder();
        boolean workoutExists = false;
        int workoutInsertIndex = -1;

        // Split the existing data by line and build a map of workouts
        String[] lines = existingData.split("\n");
        List<String> orderedWorkouts = new ArrayList<>();  // To keep the order of workouts
        Map<String, List<String>> workoutSetsMap = new LinkedHashMap<>();

        String currentWorkout = "";
        for (String line : lines) {
            line = line.trim(); // Trim to avoid extra spaces

            if (line.startsWith("Workout: ")) {
                currentWorkout = line;
                workoutSetsMap.putIfAbsent(currentWorkout, new ArrayList<>());
                orderedWorkouts.add(currentWorkout);
            } else if (line.startsWith("Set ") && !currentWorkout.isEmpty()) {
                // Adds sets under the current workout
                workoutSetsMap.get(currentWorkout).add(line);
            }
        }

        // Checks if the selected workout already exists in the map
        if (workoutSetsMap.containsKey("Workout: " + selectedWorkout)) {
            workoutExists = true;
            currentWorkout = "Workout: " + selectedWorkout;
        } else {
            // Adds new workout type if it doesn't already exist
            currentWorkout = "Workout: " + selectedWorkout;
            orderedWorkouts.add(currentWorkout);
            workoutSetsMap.put(currentWorkout, new ArrayList<>());
        }

        // Determines the next set number based on existing sets for this workout
        int setNumber = workoutSetsMap.get(currentWorkout).size() + 1;

        // Iterates over the setContainer to get weights and reps from each set input layout
        for (int i = 0; i < setContainer.getChildCount(); i++) {
            View setView = setContainer.getChildAt(i);

            // Access weight and reps EditTexts
            EditText weightEditText = setView.findViewById(R.id.weight_input);
            EditText repsEditText = setView.findViewById(R.id.reps_input);

            // Gets the text from the EditTexts
            String weight = weightEditText.getText().toString().trim();
            String reps = repsEditText.getText().toString().trim();

            // Ensures both weight and reps have values
            if (weight.isEmpty() || reps.isEmpty()) {
                Toast.makeText(this, "Please fill in all weight and reps fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Appends the set details to the new sets builder
            newSetsBuilder.append("Set ").append(setNumber).append(": Weight: ").append(weight)
                    .append(" kg, Reps: ").append(reps).append("\n");
            setNumber++;
        }

        // Adds the new sets to the workout in the map
        workoutSetsMap.get(currentWorkout).addAll(Arrays.asList(newSetsBuilder.toString().trim().split("\n")));

        // Rebuild the data from the map
        for (String workout : orderedWorkouts) {
            dataBuilder.append(workout).append("\n");
            List<String> sets = workoutSetsMap.get(workout);
            for (String set : sets) {
                dataBuilder.append(set).append("\n");
            }
        }

        // Saves the updated data
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(DATA_KEY + selectedDate, dataBuilder.toString().trim());
        editor.apply();

        Toast.makeText(this, "Data saved for " + selectedDate, Toast.LENGTH_SHORT).show();
    }

    // Helper method to determine the next set number for a workout type (Ended up not being used but kept for possible future updates/improvements)
    private int getNextSetNumber(String existingData, String selectedWorkout) {
        int setNumber = 1; 

        if (!existingData.isEmpty()) {
            String[] lines = existingData.split("\n");
            boolean isCorrectWorkout = false;
            for (String line : lines) {
                if (line.equals("Workout: " + selectedWorkout)) {
                    isCorrectWorkout = true;
                    setNumber = 1; 
                } else if (isCorrectWorkout && line.startsWith("Set ") && line.contains(": Weight:") && line.contains("Reps:")) {
                    setNumber++;
                } else if (line.startsWith("Workout: ")) {
                    isCorrectWorkout = false; 
                }
            }
        }
        return setNumber;
    }

    private void showWorkoutSummary() {
        Log.d(TAG, "Show Workout Summary method called for date: " + selectedDate);

        // Ensures selectedDate is not null. If it is, use the current date
        if (selectedDate == null) {
            Calendar calendar = Calendar.getInstance();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedDate = dateFormat.format(calendar.getTime());
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String data = prefs.getString(DATA_KEY + selectedDate, "");

        if (data.isEmpty()) {
            Toast.makeText(this, "No data for " + selectedDate, Toast.LENGTH_SHORT).show();
            return;
        }

        // Popup window showing saved data text
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_workout_summary, null);
        TextView summaryTextView = dialogView.findViewById(R.id.summary_text_view);

        // Sets the saved data text
        summaryTextView.setText(data);

        builder.setView(dialogView)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .create()
                .show();

        Log.d(TAG, "Data: " + data);
    }

    private void deleteLastSet() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String data = prefs.getString(DATA_KEY + selectedDate, "");

        if (data.isEmpty()) {
            Toast.makeText(this, "No data to delete for " + selectedDate, Toast.LENGTH_SHORT).show();
            return;
        }

        // Splits data into lines
        String[] lines = data.split("\n");

        // Removes the last set line only
        StringBuilder updatedData = new StringBuilder();
        int lastSetIndex = -1;

        // Identies the last set index
        for (int i = lines.length - 1; i >= 0; i--) {
            if (lines[i].startsWith("Set ")) {
                lastSetIndex = i;
                break;
            }
        }

        // Rebuilds the data without the last set
        for (int i = 0; i < lines.length; i++) {
            if (i != lastSetIndex) {
                updatedData.append(lines[i]).append("\n");
            }
        }

        // Handles cases where all sets have been deleted
        String finalData = updatedData.toString().trim();

        if (lastSetIndex == -1) {
            // If no sets were found, remove all data for the selected date
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(DATA_KEY + selectedDate); 
            editor.apply();
            Toast.makeText(this, "No more sets for " + selectedDate, Toast.LENGTH_SHORT).show();
        } else if (finalData.startsWith("Workout: ") && finalData.length() > "Workout: ".length()) {
            // Saves updated data with the workout title and remaining sets
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(DATA_KEY + selectedDate, finalData);
            editor.apply();
            Toast.makeText(this, "Last set deleted for " + selectedDate, Toast.LENGTH_SHORT).show();
        } else {
            // Saves updated data
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(DATA_KEY + selectedDate, finalData);
            editor.apply();
            Toast.makeText(this, "Last set deleted for " + selectedDate, Toast.LENGTH_SHORT).show();
        }
    }

    // Method for debugging 
    private void displayDataForDate(String date) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedData = prefs.getString(DATA_KEY + date, "");

        if (savedData != null && !savedData.isEmpty()) {
            Log.d(TAG, "Displaying data for date: " + date);
            String[] entries = savedData.split("\n");

            for (String entry : entries) {
                if (entry.startsWith("Workout: ")) {
                    Log.d(TAG, "Workout: " + entry);
                } else if (entry.startsWith("Set ")) {
                    Log.d(TAG, "Set: " + entry);
                } else {
                    Log.e(TAG, "Malformed data entry: " + entry);
                }
            }
        } else {
            Log.d(TAG, "No data found for date: " + date);
        }
    }
}


