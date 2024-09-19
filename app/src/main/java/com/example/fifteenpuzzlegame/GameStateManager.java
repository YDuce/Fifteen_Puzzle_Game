package com.example.fifteenpuzzlegame;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

/**
 * Manages the game state, including temporary game data and long-term statistics, using SharedPreferences.
 */
public class GameStateManager {
    private static final String PREFS_NAME = "GameStatsPrefs";
    private static final String PREFS_BOARD_KEY = "SavedBoardState";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Gson gson;

    /**
     * Constructor to initialize the GameStateManager with a context.
     *
     * @param context The application context used to access SharedPreferences.
     */
    public GameStateManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
        gson = new Gson();
    }

    // ========== TEMPORARY STATE MANAGEMENT ==========

    /**
     * Saves the temporary game state, including the puzzle, move count, and pause details.
     *
     * @param game        The current PuzzleGame object to save.
     * @param moveCount   The number of moves made.
     * @param pauseOffset The pause offset for the chronometer.
     * @param isPaused    Whether the game is currently paused.
     */
    public void saveTemporaryGameState(PuzzleGame game, int moveCount, long pauseOffset, boolean isPaused) {
        editor.putString(PREFS_BOARD_KEY + game.getGridSize(), gson.toJson(game));
        editor.putInt("moveCount", moveCount);
        editor.putLong("pauseOffset", pauseOffset);
        editor.putBoolean("isPaused", isPaused);
        editor.apply();
    }

    /**
     * Loads the temporary game state from SharedPreferences.
     *
     * @param gridSize The grid size to load the state for.
     * @return The saved PuzzleGame object or null if no saved state exists.
     */
    public PuzzleGame loadTemporaryGameState(int gridSize) {
        String gameBoardJson = prefs.getString(PREFS_BOARD_KEY + gridSize, null);
        if (gameBoardJson != null) {
            return gson.fromJson(gameBoardJson, PuzzleGame.class);
        }
        return null;
    }

    /**
     * Deletes the temporary game state for a specific grid size.
     *
     * @param gridSize The grid size whose temporary state will be deleted.
     */
    public void deleteTemporaryGameState(int gridSize) {
        editor.remove(PREFS_BOARD_KEY + gridSize);
        editor.remove("moveCount");
        editor.remove("pauseOffset");
        editor.remove("isPaused");
        editor.apply();
    }

    // Getters for temporary game state data

    /**
     * Retrieves the saved move count.
     *
     * @return The saved move count.
     */
    public int getMoveCount() {
        return prefs.getInt("moveCount", 0);
    }

    /**
     * Retrieves the saved pause offset.
     *
     * @return The saved pause offset.
     */
    public long getPauseOffset() {
        return prefs.getLong("pauseOffset", 0);
    }

    /**
     * Retrieves the pause state of the game.
     *
     * @return True if the game is paused, false otherwise.
     */
    public boolean isPaused() {
        return prefs.getBoolean("isPaused", false);
    }

    // ========== LONG-TERM GAME STATISTICS MANAGEMENT ==========

    /**
     * Saves long-term game statistics, including games played, won, win percentage, best time, and best move count.
     *
     * @param gridSize      The grid size the statistics apply to.
     * @param gamesPlayed   The total games played.
     * @param gamesWon      The total games won.
     * @param winPercentage The win percentage.
     * @param bestTime      The best completion time in milliseconds.
     * @param bestMoveCount The best move count.
     */
    public void saveGameStatistics(int gridSize, int gamesPlayed, int gamesWon, double winPercentage, long bestTime, int bestMoveCount) {
        editor.putInt("gamesPlayed_" + gridSize, gamesPlayed);
        editor.putInt("gamesWon_" + gridSize, gamesWon);
        editor.putFloat("winPercentage_" + gridSize, (float) winPercentage);
        editor.putLong("bestTime_" + gridSize, bestTime);
        editor.putInt("bestMoveCount_" + gridSize, bestMoveCount);
        editor.apply();
    }

    /**
     * Retrieves the total number of games played for a specific grid size.
     *
     * @param gridSize The grid size to retrieve statistics for.
     * @return The total number of games played.
     */
    public int getGamesPlayed(int gridSize) {
        return prefs.getInt("gamesPlayed_" + gridSize, 0);
    }

    /**
     * Retrieves the total number of games won for a specific grid size.
     *
     * @param gridSize The grid size to retrieve statistics for.
     * @return The total number of games won.
     */
    public int getGamesWon(int gridSize) {
        return prefs.getInt("gamesWon_" + gridSize, 0);
    }

    /**
     * Retrieves the win percentage for a specific grid size.
     *
     * @param gridSize The grid size to retrieve statistics for.
     * @return The win percentage as a float.
     */
    public float getWinPercentage(int gridSize) {
        return prefs.getFloat("winPercentage_" + gridSize, 0.0f);
    }

    /**
     * Retrieves the best completion time for a specific grid size.
     *
     * @param gridSize The grid size to retrieve statistics for.
     * @return The best time in milliseconds.
     */
    public long getBestTime(int gridSize) {
        return prefs.getLong("bestTime_" + gridSize, Long.MAX_VALUE);
    }

    /**
     * Retrieves the best move count for a specific grid size.
     *
     * @param gridSize The grid size to retrieve statistics for.
     * @return The best move count.
     */
    public int getBestMoveCount(int gridSize) {
        return prefs.getInt("bestMoveCount_" + gridSize, 0);
    }

    // ========== RESETTING GAME STATES ==========

    /**
     * Deletes the temporary game state for a specific grid size.
     *
     * @param gridSize The grid size whose temporary state will be deleted.
     */
    public void deleteTempGameState(int gridSize) {
        editor.remove(PREFS_BOARD_KEY + gridSize);
        editor.apply();  // Apply changes asynchronously
    }

//    /**
//     * Clears long-term game statistics for a specific grid size.
//     *
//     * @param gridSize The grid size whose statistics will be cleared.
//     */
//    public void clearGameStatistics(int gridSize) {
//        editor.remove("gamesPlayed_" + gridSize);
//        editor.remove("gamesWon_" + gridSize);
//        editor.remove("winPercentage_" + gridSize);
//        editor.remove("bestTime_" + gridSize);
//        editor.remove("bestMoveCount_" + gridSize);
//        editor.apply();
//    }

    // ========== ADDITIONAL PREFERENCES ==========

    /**
     * Checks if Auto-Save is enabled.
     *
     * @return True if Auto-Save is enabled, false otherwise.
     */
    public boolean isAutoSaveEnabled() {
        return prefs.getBoolean("autosave", false);
    }

    /**
     * Enables or disables the Auto-Save feature.
     *
     * @param isEnabled Whether Auto-Save should be enabled.
     */
    public void setAutoSaveEnabled(boolean isEnabled) {
        editor.putBoolean("autosave", isEnabled);
        editor.apply();
    }

    /**
     * Checks if Dark Mode is enabled.
     *
     * @return True if Dark Mode is enabled, false otherwise.
     */
    public boolean isDarkModeEnabled() {
        return prefs.getBoolean("darkmode", false);
    }

    /**
     * Enables or disables the Dark Mode feature.
     *
     * @param isEnabled Whether Dark Mode should be enabled.
     */
    public void setDarkModeEnabled(boolean isEnabled) {
        editor.putBoolean("darkmode", isEnabled);
        editor.apply();
    }

    // ========== FORMATTING STATISTICS (FOR UI) ==========

    /**
     * Retrieves formatted game statistics as a string for display in the UI.
     *
     * @param gridSize The grid size for which statistics will be formatted.
     * @return A formatted string containing game statistics.
     */
    public String getFormattedStatistics(int gridSize) {
        int gamesPlayed = getGamesPlayed(gridSize);
        int gamesWon = getGamesWon(gridSize);
        float winPercentage = getWinPercentage(gridSize);
        long bestTime = getBestTime(gridSize);
        int bestMoveCount = getBestMoveCount(gridSize);

        String bestTimeFormatted = bestTime == Long.MAX_VALUE ? "N/A" : formatTime(bestTime);

        return String.format("Grid Size: %dx%d\nGames Played: %d\nGames Won: %d\nWin Percentage: %.2f%%\nBest Time: %s\nBest Move Count: %d", gridSize, gridSize, gamesPlayed, gamesWon, winPercentage, bestTimeFormatted, bestMoveCount);
    }

    /**
     * Helper method to format time in MM:SS format.
     *
     * @param timeInMillis The time in milliseconds.
     * @return The formatted time as a string.
     */
    private String formatTime(long timeInMillis) {
        int minutes = (int) (timeInMillis / 1000) / 60;
        int seconds = (int) (timeInMillis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
