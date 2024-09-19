package com.example.fifteenpuzzlegame;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fifteenpuzzlegame.databinding.ActivityStatisticsBinding;

import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    private static final int[] GRID_SIZES = {3, 4, 5};
    private ActivityStatisticsBinding binding;

    /**
     * Called when the activity is created. Initializes the layout and loads statistics.
     *
     * @param savedInstanceState Bundle containing the saved state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityStatisticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        loadStatistics();
    }

    /**
     * Sets up the toolbar with back navigation enabled.
     */
    private void setupToolbar() {
        setSupportActionBar(binding.toolbarStatistics);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbarStatistics.setNavigationOnClickListener(v -> finish());
    }

    /**
     * Loads the statistics from SharedPreferences and displays them.
     */
    private void loadStatistics() {
        String prefsName = getIntent().getStringExtra("prefsName");
        if (prefsName != null) {
            SharedPreferences prefs = getSharedPreferences(prefsName, MODE_PRIVATE);
            displayStatisticsForAllGridSizes(prefs);  // Pass SharedPreferences to method
        }
    }

    /**
     * Displays statistics for all grid sizes available.
     *
     * @param prefs SharedPreferences containing the statistics.
     */
    private void displayStatisticsForAllGridSizes(SharedPreferences prefs) {
        for (int gridSize : GRID_SIZES) {
            String[] statistics = getStatisticsForGridSize(prefs, gridSize);
            updateTextViewsForGridSize(gridSize, statistics);
        }
    }

    /**
     * Retrieves statistics for a specific grid size from SharedPreferences.
     *
     * @param prefs    SharedPreferences object containing the statistics.
     * @param gridSize The grid size for which to retrieve the statistics.
     * @return A string array containing games played, games won, win percentage, best time, and best move count.
     */
    private String[] getStatisticsForGridSize(SharedPreferences prefs, int gridSize) {
        int gamesPlayed = prefs.getInt("gamesPlayed_" + gridSize, 0);
        int gamesWon = prefs.getInt("gamesWon_" + gridSize, 0);
        float winPercentage = prefs.getFloat("winPercentage_" + gridSize, 0.0f);
        long bestTime = prefs.getLong("bestTime_" + gridSize, Long.MAX_VALUE);
        int bestMoveCount = prefs.getInt("bestMoveCount_" + gridSize, 0);

        // Format the time and return the statistics as a string array
        return new String[]{String.valueOf(gamesPlayed), String.valueOf(gamesWon), String.format(Locale.getDefault(), "%.2f%%", winPercentage), formatTime(bestTime), String.valueOf(bestMoveCount)};
    }

    /**
     * Updates the TextViews for a specific grid size with the retrieved statistics.
     *
     * @param gridSize   The grid size for which to display the statistics.
     * @param statistics A string array containing the statistics for the grid size.
     */
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

    /**
     * Sets the text of a specific TextView.
     *
     * @param textViewId The resource ID of the TextView.
     * @param text       The text to be displayed in the TextView.
     */
    private void updateStatisticsText(int textViewId, String text) {
        TextView textView = findViewById(textViewId);
        textView.setText(text);
    }

    /**
     * Formats the time from milliseconds into a readable MM:SS format.
     *
     * @param timeInMillis Time in milliseconds.
     * @return The formatted time as a string.
     */
    private String formatTime(long timeInMillis) {
        if (timeInMillis == Long.MAX_VALUE) {
            return "N/A";
        }
        int minutes = (int) (timeInMillis / 1000) / 60;
        int seconds = (int) (timeInMillis / 1000) % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }
}
