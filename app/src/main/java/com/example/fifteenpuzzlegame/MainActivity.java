package com.example.fifteenpuzzlegame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomappbar.BottomAppBar;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "GameStatsPrefs";
    private static final String PREFS_BOARD_KEY = "SavedBoardState";

    private PuzzleGame game;
    private Button[][] buttons;
    private GridLayout gridLayout;

    // Game statistics variables
    private int gamesPlayed;
    private int gamesWon;
    private double winPercentage;
    private int moveCount;
    private int bestMoveCount;
    private long bestTime = Long.MAX_VALUE;

    private TextView moveCounterTextView;
    private Chronometer chronometer;
    private boolean isPaused;
    private long pauseOffset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupUI();
        loadStatistics();
        initGame(savedInstanceState);
    }

    private void setupUI() {
        setupToolbar();
        setupBottomAppBar();
        gridLayout = findViewById(R.id.grid_layout);
        moveCounterTextView = findViewById(R.id.move_counter);
        chronometer = findViewById(R.id.chronometer);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_main);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        } else {
            Log.e("MainActivity", "Toolbar is null.");
        }
    }

    private void setupBottomAppBar() {
        BottomAppBar bottomAppBar = findViewById(R.id.bottom_app_bar);
        if (bottomAppBar != null) {
            setSupportActionBar(bottomAppBar);
        } else {
            Log.e("MainActivity", "BottomAppBar is null.");
        }
        setupBottomBarButtons();
    }

    private void setupBottomBarButtons() {
        ImageButton buttonHome = findViewById(R.id.button_home);
        ImageButton buttonPause = findViewById(R.id.button_pause);
        ImageButton buttonStats = findViewById(R.id.button_stats);
        ImageButton buttonRestart = findViewById(R.id.button_restart);

        buttonHome.setOnClickListener(v -> goToMenu());
        buttonPause.setOnClickListener(v -> togglePause());
        buttonStats.setOnClickListener(v -> showStatistics());
        buttonRestart.setOnClickListener(this::restartGame);
    }

    // Initialize game (from saved state or new game)
    private void initGame(Bundle savedInstanceState) {
        Intent intent = getIntent();
        int gridSize = intent.getIntExtra("numButtonRows", 4);

        if (savedInstanceState != null) {
            restoreGameStateFromBundle(savedInstanceState);
        } else {
            checkForPreviousGameOrStartNew(gridSize);
        }
    }

    private void checkForPreviousGameOrStartNew(int gridSize) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String gameBoardJson = prefs.getString(PREFS_BOARD_KEY + gridSize, null);

        if (gameBoardJson != null) {
            PuzzleGame game = PuzzleGame.fromJson(gameBoardJson);

            if (game != null && !game.isGameFinished()) {
                new AlertDialog.Builder(this).setTitle("Continue Previous Game?").setMessage("You have an unfinished game. Continue or start a new one?").setPositiveButton("Continue", (dialog, which) -> restoreGameStateFromJson(gameBoardJson)).setNegativeButton("New Game", (dialog, which) -> {
                    deleteSavedGameState(gridSize);
                    startNewGame(gridSize);
                }).show();
            } else {
                startNewGame(gridSize);
            }
        } else {
            startNewGame(gridSize);
        }
    }

    private void deleteSavedGameState(int gridSize) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(PREFS_BOARD_KEY + gridSize);
        editor.apply();
        Log.d("MainActivity", "Saved game state deleted for grid size " + gridSize);
    }

    private void restoreGameStateFromJson(String gameBoardJson) {
        game = PuzzleGame.fromJson(gameBoardJson);
        if (game == null) {
            Log.e("MainActivity", "Failed to restore PuzzleGame from JSON.");
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        moveCount = prefs.getInt("moveCount", 0);
        pauseOffset = prefs.getLong("pauseOffset", 0);
        isPaused = prefs.getBoolean("isPaused", false);

        initializeGrid();
        updateUI();

        if (isPaused) {
            pauseGame();
        } else {
            resumeGame();
        }
    }

    private void startNewGame(int gridSize) {
        clearBoardState();
        game = new PuzzleGame(gridSize);

        moveCount = 0;
        pauseOffset = 0;
        moveCounterTextView.setText(getString(R.string.move_counter, moveCount));

        initializeGrid();
        updateUI();
        resumeGame();
    }

    private void initializeGrid() {
        int gridSize = game.getGridSize();
        buttons = new Button[gridSize][gridSize];
        gridLayout.setRowCount(gridSize);
        gridLayout.setColumnCount(gridSize);

        int buttonSize = calculateButtonSize(gridSize);
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                createButton(i, j, buttonSize);
            }
        }
    }

    private int calculateButtonSize(int gridSize) {
        int totalMargin = 10 * (gridSize + 1);
        int availableWidth = getResources().getDisplayMetrics().widthPixels - totalMargin;
        int availableHeight = getResources().getDisplayMetrics().heightPixels - totalMargin;
        return (int) ((double) Math.min(availableWidth, availableHeight) / gridSize * 0.9);
    }

    private void createButton(int row, int col, int buttonSize) {
        Button button = new Button(this);
        int tileValue = game.getTileValue(row, col);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.rowSpec = GridLayout.spec(row);
        params.columnSpec = GridLayout.spec(col);
        params.width = buttonSize;
        params.height = buttonSize;
        params.setMargins(5, 5, 5, 5);
        button.setLayoutParams(params);

        if (tileValue == 0) {
            button.setText("");
            button.setBackgroundResource(R.drawable.tile_empty);
        } else {
            button.setText(String.valueOf(tileValue));
            button.setBackgroundResource(R.drawable.tile_normal);
        }

        buttons[row][col] = button;
        gridLayout.addView(button);

        button.setOnClickListener(v -> onTileClick(row, col));
    }

    private void loadStatistics() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        gamesPlayed = prefs.getInt("gamesPlayed", 0);
        gamesWon = prefs.getInt("gamesWon", 0);
        winPercentage = prefs.getFloat("winPercentage", 0.0f);
        bestTime = prefs.getLong("bestTime", Long.MAX_VALUE);
        bestMoveCount = prefs.getInt("bestMoveCount", 0);

        Log.d("MainActivity", "Statistics loaded: Games Played=" + gamesPlayed + ", Games Won=" + gamesWon + ", Best Time=" + bestTime + ", Best Moves=" + bestMoveCount);
    }

    private void onTileClick(int row, int col) {
        if (!isPaused && game.moveTiles(row, col)) {
            moveCount++;
            updateUI();
            saveBoardState();
            saveGameStatistics();
            if (game.isSolved()) {
                handleGameWin();
            }
        }
    }

    private void updateUI() {
        updateTiles();
        updateMoveCounter();
        chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset); // Reset the visual timer
    }

    private void updateTiles() {
        for (int i = 0; i < buttons.length; i++) {
            for (int j = 0; j < buttons[i].length; j++) {
                int tileValue = game.getTileValue(i, j);
                if (tileValue == 0) {
                    buttons[i][j].setText("");
                    buttons[i][j].setBackgroundResource(R.drawable.tile_empty);
                } else {
                    buttons[i][j].setText(String.valueOf(tileValue));
                    buttons[i][j].setBackgroundResource(R.drawable.tile_normal);
                }
            }
        }
    }

    private void updateMoveCounter() {
        moveCounterTextView.setText(getString(R.string.move_counter, moveCount));
    }

    private void pauseChronometer() {
        pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
        chronometer.stop();
    }

    private void resumeChronometer() {
        chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
        chronometer.start();
    }

    private void handleGameWin() {
        pauseGame();
        calculateGameStatistics();
        clearBoardState();  // Ensure old game data is cleared
        game = null;

        new AlertDialog.Builder(this, R.style.CustomDialogTheme).setTitle("Congratulations!").setMessage("You've solved the puzzle. What would you like to do next?").setPositiveButton("Play Again", (dialog, which) -> startNewGame(game.getGridSize())).setNegativeButton("Go to Menu", (dialog, which) -> goToMenu()).show();
    }

    private void showStatistics() {
        pauseGame();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int gridSize = game.getGridSize();
        int currentGamesPlayed = prefs.getInt("gamesPlayed_" + gridSize, 0);
        int currentGamesWon = prefs.getInt("gamesWon_" + gridSize, 0);
        float currentWinPercentage = prefs.getFloat("winPercentage_" + gridSize, 0.0f);
        long currentBestTime = prefs.getLong("bestTime_" + gridSize, Long.MAX_VALUE);
        int currentBestMoveCount = prefs.getInt("bestMoveCount_" + gridSize, 0);

        String stats = String.format(Locale.getDefault(), "Current Grid Size: %dx%d\nGames Played: %d\nGames Won: %d\nWin Percentage: %.2f%%\nBest Time: %s\nBest Move Count: %d", gridSize, gridSize, currentGamesPlayed, currentGamesWon, currentWinPercentage, formatTime(currentBestTime), currentBestMoveCount);

        new AlertDialog.Builder(this, R.style.CustomDialogTheme).setTitle("Current Game Statistics").setMessage(stats).setPositiveButton("View All Statistics", (dialog, which) -> launchStatisticsActivity()).setNegativeButton("Close", (dialog, which) -> resumeGame()).setCancelable(false).show();
    }

    private String formatTime(long timeInMillis) {
        if (timeInMillis == Long.MAX_VALUE) {
            return "N/A";
        }
        int minutes = (int) (timeInMillis / 1000) / 60;
        int seconds = (int) (timeInMillis / 1000) % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private void launchStatisticsActivity() {
        Intent intent = new Intent(this, StatisticsActivity.class);
        intent.putExtra("prefsName", PREFS_NAME);
        startActivity(intent);
    }

    private void calculateGameStatistics() {
        gamesWon++;
        winPercentage = (gamesPlayed > 0) ? (double) gamesWon / gamesPlayed * 100 : 0.0;

        long currentTime = SystemClock.elapsedRealtime() - chronometer.getBase();
        if (currentTime < bestTime) bestTime = currentTime;
        if (moveCount < bestMoveCount || bestMoveCount == 0) bestMoveCount = moveCount;

        saveGameStatistics();
    }

    private void clearBoardState() {
        if (game == null) {
            Log.e("MainActivity", "PuzzleGame instance is null. Cannot clear board state.");
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.remove(PREFS_BOARD_KEY + game.getGridSize());
        editor.apply();
        Log.d("MainActivity", "Cleared saved game board data for grid size " + game.getGridSize());
    }

    private void saveBoardState() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String gameStateJson = game.toJson();
        editor.putString(PREFS_BOARD_KEY + game.getGridSize(), gameStateJson);
        editor.putInt("moveCount", moveCount);
        editor.putLong("pauseOffset", pauseOffset);
        editor.putBoolean("isPaused", isPaused);

        editor.putInt("gamesPlayed", gamesPlayed);
        editor.putInt("gamesWon", gamesWon);
        editor.putFloat("winPercentage", (float) winPercentage);
        editor.putLong("bestTime", bestTime);
        editor.putInt("bestMoveCount", bestMoveCount);
        editor.apply();
    }

    private void saveGameStatistics() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt("gamesPlayed", gamesPlayed);
        editor.putInt("gamesWon", gamesWon);
        editor.putFloat("winPercentage", (float) winPercentage);
        editor.putLong("bestTime", bestTime);
        editor.putInt("bestMoveCount", bestMoveCount);
        editor.apply();
    }

    private void goToMenu() {
        pauseGame();
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
        finish();
    }

    private void restartGame(View view) {
        startNewGame(game.getGridSize());
    }

    private void togglePause() {
        if (isPaused) {
            resumeGame();
        } else {
            pauseGame();
        }
    }

    private void pauseGame() {
        isPaused = true;
        pauseChronometer();
        updatePauseButtonIcon(R.drawable.ic_play);
    }

    private void resumeGame() {
        isPaused = false;
        resumeChronometer();
        updatePauseButtonIcon(R.drawable.ic_pause);
    }

    private void updatePauseButtonIcon(int iconResId) {
        ImageButton buttonPause = findViewById(R.id.button_pause);
        if (buttonPause != null) {
            buttonPause.setImageResource(iconResId);
        } else {
            Log.e("MainActivity", "Pause button is null.");
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("gameStateJson", game.toJson());
        outState.putInt("moveCount", moveCount);
        outState.putLong("chronometerBase", chronometer.getBase());
        outState.putLong("pauseOffset", pauseOffset);
        outState.putBoolean("isPaused", isPaused);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreGameStateFromBundle(savedInstanceState);

        chronometer.setBase(savedInstanceState.getLong("chronometerBase"));
        isPaused = savedInstanceState.getBoolean("isPaused", false);
        if (isPaused) {
            pauseChronometer();
        } else {
            resumeChronometer();
        }
    }

    private void restoreGameStateFromBundle(Bundle savedInstanceState) {
        String gameStateJson = savedInstanceState.getString("gameStateJson");

        if (gameStateJson != null) {
            game = PuzzleGame.fromJson(gameStateJson);

            initializeGrid();
            updateUI();
        } else {
            Log.e("MainActivity", "Game state JSON is null. Starting a new game.");
            startNewGame(game.getGridSize());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveBoardState();
        saveGameStatistics();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeGame();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveBoardState();
        saveGameStatistics();
    }
}
