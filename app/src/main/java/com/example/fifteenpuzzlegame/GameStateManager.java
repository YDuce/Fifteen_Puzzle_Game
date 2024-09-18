package com.example.fifteenpuzzlegame;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

public class GameStateManager {
    private static final String PREFS_NAME = "GameStatsPrefs";
    private static final String PREFS_BOARD_KEY = "SavedBoardState";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Gson gson;

    // Constructor
    public GameStateManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
        gson = new Gson();  // Initialize GSON for serializing/deserializing objects
    }

    // ========== TEMPORARY STATE MANAGEMENT ==========

    // Save the temporary game state (current puzzle, move count, and other game details)
    public void saveTemporaryGameState(PuzzleGame game, int moveCount, long pauseOffset, boolean isPaused) {
        editor.putString(PREFS_BOARD_KEY + game.getGridSize(), gson.toJson(game));
        editor.putInt("moveCount", moveCount);
        editor.putLong("pauseOffset", pauseOffset);
        editor.putBoolean("isPaused", isPaused);
        editor.apply();
    }

    // Load the temporary game state (puzzle board, move count, etc.)
    public PuzzleGame loadTemporaryGameState(int gridSize) {
        String gameBoardJson = prefs.getString(PREFS_BOARD_KEY + gridSize, null);
        if (gameBoardJson != null) {
            return gson.fromJson(gameBoardJson, PuzzleGame.class);
        }
        return null; // No saved game state found
    }

    // Delete the temporary game state (e.g., when restarting or game completion)
    public void deleteTemporaryGameState(int gridSize) {
        editor.remove(PREFS_BOARD_KEY + gridSize);
        editor.remove("moveCount");
        editor.remove("pauseOffset");
        editor.remove("isPaused");
        editor.apply();
    }

    // Getters for temporary game state data
    public int getMoveCount() {
        return prefs.getInt("moveCount", 0);
    }

    public long getPauseOffset() {
        return prefs.getLong("pauseOffset", 0);
    }

    public boolean isPaused() {
        return prefs.getBoolean("isPaused", false);
    }

    // ========== LONG-TERM GAME STATISTICS MANAGEMENT ==========

    // Save game statistics (games played, won, best times, etc.)
    public void saveGameStatistics(int gridSize, int gamesPlayed, int gamesWon, double winPercentage, long bestTime, int bestMoveCount) {
        editor.putInt("gamesPlayed_" + gridSize, gamesPlayed);
        editor.putInt("gamesWon_" + gridSize, gamesWon);
        editor.putFloat("winPercentage_" + gridSize, (float) winPercentage);
        editor.putLong("bestTime_" + gridSize, bestTime);
        editor.putInt("bestMoveCount_" + gridSize, bestMoveCount);
        editor.apply();  // Apply changes asynchronously
    }

    // Load statistics for the current grid size
    public int getGamesPlayed(int gridSize) {
        return prefs.getInt("gamesPlayed_" + gridSize, 0);
    }

    public int getGamesWon(int gridSize) {
        return prefs.getInt("gamesWon_" + gridSize, 0);
    }

    public float getWinPercentage(int gridSize) {
        return prefs.getFloat("winPercentage_" + gridSize, 0.0f);
    }

    public long getBestTime(int gridSize) {
        return prefs.getLong("bestTime_" + gridSize, Long.MAX_VALUE);
    }

    public int getBestMoveCount(int gridSize) {
        return prefs.getInt("bestMoveCount_" + gridSize, 0);
    }

    // ========== RESETTING GAME STATES ==========

    // Delete temporary game state data
    public void deleteTempGameState(int gridSize) {
        editor.remove(PREFS_BOARD_KEY + gridSize);
        editor.apply();  // Apply changes asynchronously
    }

    // Clear long-term game statistics for the current grid size
    public void clearGameStatistics(int gridSize) {
        editor.remove("gamesPlayed_" + gridSize);
        editor.remove("gamesWon_" + gridSize);
        editor.remove("winPercentage_" + gridSize);
        editor.remove("bestTime_" + gridSize);
        editor.remove("bestMoveCount_" + gridSize);
        editor.apply();  // Apply changes asynchronously
    }

    // ========== ADDITIONAL PREFERENCES ==========

    public boolean isAutoSaveEnabled() {
        return prefs.getBoolean("autosave", false);
    }

    public void setAutoSaveEnabled(boolean isEnabled) {
        editor.putBoolean("autosave", isEnabled);
        editor.apply();
    }

    public boolean isDarkModeEnabled() {
        return prefs.getBoolean("darkmode", false);
    }

    public void setDarkModeEnabled(boolean isEnabled) {
        editor.putBoolean("darkmode", isEnabled);
        editor.apply();
    }

    // ========== FORMATTING STATISTICS (FOR UI) ==========

    public String getFormattedStatistics(int gridSize) {
        int gamesPlayed = getGamesPlayed(gridSize);
        int gamesWon = getGamesWon(gridSize);
        float winPercentage = getWinPercentage(gridSize);
        long bestTime = getBestTime(gridSize);
        int bestMoveCount = getBestMoveCount(gridSize);

        String bestTimeFormatted = bestTime == Long.MAX_VALUE ? "N/A" : formatTime(bestTime);

        return String.format("Grid Size: %dx%d\nGames Played: %d\nGames Won: %d\nWin Percentage: %.2f%%\nBest Time: %s\nBest Move Count: %d", gridSize, gridSize, gamesPlayed, gamesWon, winPercentage, bestTimeFormatted, bestMoveCount);
    }

    // Helper method to format time for statistics display
    private String formatTime(long timeInMillis) {
        int minutes = (int) (timeInMillis / 1000) / 60;
        int seconds = (int) (timeInMillis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}