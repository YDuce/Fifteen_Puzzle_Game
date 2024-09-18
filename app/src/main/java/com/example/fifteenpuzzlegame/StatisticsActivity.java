package com.example.fifteenpuzzlegame;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fifteenpuzzlegame.databinding.ActivityStatisticsBinding;

import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    // Define the grid sizes available
    private static final int[] GRID_SIZES = {3, 4, 5};
    private ActivityStatisticsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout using ViewBinding
        binding = ActivityStatisticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup the toolbar and preferences
        setupToolbar();
        loadStatistics();  // Fetching statistics from SharedPreferences
    }

    // Set up the toolbar with back navigation
    private void setupToolbar() {
        setSupportActionBar(binding.toolbarStatistics);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbarStatistics.setNavigationOnClickListener(v -> onBackPressed());
    }

    // Load statistics from SharedPreferences
    private void loadStatistics() {
        // Retrieve the name of the shared preferences
        String prefsName = getIntent().getStringExtra("prefsName");
        if (prefsName != null) {
            SharedPreferences prefs = getSharedPreferences(prefsName, MODE_PRIVATE);
            displayStatisticsForAllGridSizes(prefs);  // Pass SharedPreferences to method
        }
    }

    // Display statistics for all grid sizes defined in GRID_SIZES
    private void displayStatisticsForAllGridSizes(SharedPreferences prefs) {
        for (int gridSize : GRID_SIZES) {
            String[] statistics = getStatisticsForGridSize(prefs, gridSize);
            updateTextViewsForGridSize(gridSize, statistics);
        }
    }

    // Fetch statistics from SharedPreferences for a specific grid size
    private String[] getStatisticsForGridSize(SharedPreferences prefs, int gridSize) {
        // Use the grid size to fetch statistics for each size
        int gamesPlayed = prefs.getInt("gamesPlayed_" + gridSize, 0);
        int gamesWon = prefs.getInt("gamesWon_" + gridSize, 0);
        float winPercentage = prefs.getFloat("winPercentage_" + gridSize, 0.0f);
        long bestTime = prefs.getLong("bestTime_" + gridSize, Long.MAX_VALUE);
        int bestMoveCount = prefs.getInt("bestMoveCount_" + gridSize, 0);

        // Format the time and return the statistics as a string array
        return new String[]{String.valueOf(gamesPlayed), String.valueOf(gamesWon), String.format(Locale.getDefault(), "%.2f%%", winPercentage), formatTime(bestTime), String.valueOf(bestMoveCount)};
    }

    // Update TextViews for each grid size by setting the relevant statistics
    private void updateTextViewsForGridSize(int gridSize, String[] statistics) {
        switch (gridSize) {
            case 3:
                updateStatisticsText(R.id.tv_data_3x3_games_played, statistics[0]);
                updateStatisticsText(R.id.tv_data_3x3_games_won, statistics[1]);
                updateStatisticsText(R.id.tv_data_3x3_win_percentage, statistics[2]);
                updateStatisticsText(R.id.tv_data_3x3_best_time, statistics[3]);
                updateStatisticsText(R.id.tv_data_3x3_best_moves, statistics[4]);
                break;
            case 4:
                updateStatisticsText(R.id.tv_data_4x4_games_played, statistics[0]);
                updateStatisticsText(R.id.tv_data_4x4_games_won, statistics[1]);
                updateStatisticsText(R.id.tv_data_4x4_win_percentage, statistics[2]);
                updateStatisticsText(R.id.tv_data_4x4_best_time, statistics[3]);
                updateStatisticsText(R.id.tv_data_4x4_best_moves, statistics[4]);
                break;
            case 5:
                updateStatisticsText(R.id.tv_data_5x5_games_played, statistics[0]);
                updateStatisticsText(R.id.tv_data_5x5_games_won, statistics[1]);
                updateStatisticsText(R.id.tv_data_5x5_win_percentage, statistics[2]);
                updateStatisticsText(R.id.tv_data_5x5_best_time, statistics[3]);
                updateStatisticsText(R.id.tv_data_5x5_best_moves, statistics[4]);
                break;
        }
    }

    // Helper method to set text in a TextView
    private void updateStatisticsText(int textViewId, String text) {
        TextView textView = findViewById(textViewId);
        textView.setText(text);
    }

    // Format the time from milliseconds into a readable string (MM:SS format)
    private String formatTime(long timeInMillis) {
        if (timeInMillis == Long.MAX_VALUE) {
            return "N/A";
        }
        int minutes = (int) (timeInMillis / 1000) / 60;
        int seconds = (int) (timeInMillis / 1000) % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }
}
