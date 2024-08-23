package com.example.fifteenpuzzlegame;

import static androidx.core.util.ObjectsCompat.requireNonNull;

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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "GameStatsPrefs";
    private static final String KEY_GAMES_PLAYED = "gamesPlayed";
    private static final String KEY_GAMES_WON = "gamesWon";
    private static final String KEY_WIN_PERCENTAGE = "winPercentage";

    private PuzzleGame game;
    private Button[][] buttons;
    private GridLayout gridLayout;
    private int moveCount;
    private int gamesPlayed;
    private int gamesWon;
    private double winPercentage;
    private TextView moveCounterTextView;
    private Chronometer chronometer;
    private boolean isPaused;
    private long pauseOffset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initGame();
        setupUI();
        loadGameData();
        initializeGrid();
        updateUI();
        startChronometer();

        gamesPlayed++;
        saveGameData();
    }

    private void initGame() {
        Intent intent = getIntent();
        int gridSize = intent.getIntExtra("numButtonRows", 4);
        game = new PuzzleGame(gridSize * gridSize);
    }

    private void setupUI() {
        setupToolbar();
        setupBottomAppBar();

        gridLayout = findViewById(R.id.grid_layout);
        moveCounterTextView = findViewById(R.id.move_counter);
        chronometer = findViewById(R.id.chronometer);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        } else {
            Log.e("MainActivity", "Toolbar is null. Check your layout file.");
        }
    }

    private void setupBottomAppBar() {
        BottomAppBar bottomAppBar = findViewById(R.id.bottom_app_bar);
        if (bottomAppBar != null) {
            setSupportActionBar(bottomAppBar);
        } else {
            Log.e("MainActivity", "BottomAppBar is null. Check your layout file.");
        }

        setupBottomBarButtons();
    }

    private void setupBottomBarButtons() {
        ImageButton buttonHome = findViewById(R.id.button_home);
        ImageButton buttonPause = findViewById(R.id.button_pause);
        ImageButton buttonStats = findViewById(R.id.button_stats);
        ImageButton buttonRestart = findViewById(R.id.button_restart);

        if (buttonHome != null) {
            buttonHome.setOnClickListener(v -> goToMenu());
        } else {
            Log.e("MainActivity", "buttonHome is null. Check your layout file.");
        }

        if (buttonPause != null) {
            buttonPause.setOnClickListener(v -> {
                if (isPaused) {
                    resumeGame();
                } else {
                    pauseGame();
                }
            });
        } else {
            Log.e("MainActivity", "buttonPause is null. Check your layout file.");
        }

        if (buttonStats != null) {
            buttonStats.setOnClickListener(v -> showStatistics());
        } else {
            Log.e("MainActivity", "buttonStats is null. Check your layout file.");
        }

        if (buttonRestart != null) {
            buttonRestart.setOnClickListener(this::restartGame);
        } else {
            Log.e("MainActivity", "buttonRestart is null. Check your layout file.");
        }
    }

    private void initializeGrid() {
        int gridSize = game.getGridSize();
        buttons = new Button[gridSize][gridSize];
        gridLayout.setRowCount(gridSize);
        gridLayout.setColumnCount(gridSize);

        int totalMargin = 5 * (gridSize + 1);
        int availableWidth = getResources().getDisplayMetrics().widthPixels - totalMargin;
        int buttonSize = availableWidth / gridSize;

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                createButton(i, j, buttonSize);
            }
        }
    }

    private void createButton(int row, int col, int buttonSize) {
        // Create a new Button
        Button button = new Button(this);

        // Set up GridLayout layout parameters
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.rowSpec = GridLayout.spec(row, 1f);
        params.columnSpec = GridLayout.spec(col, 1f);
        params.width = buttonSize;
        params.height = buttonSize;
        params.setMargins(5, 5, 5, 5);  // Set margins between buttons

        // Apply the layout parameters to the button
        button.setLayoutParams(params);

        // Store the button reference in the 2D array for future access
        buttons[row][col] = button;

        // Add the button to the grid layout
        gridLayout.addView(button);

        // Set a click listener to handle tile movement when the button is clicked
        button.setOnClickListener(v -> onTileClick(row, col));
    }

    private void onTileClick(int row, int col) {
        // Attempt to move the tile at the specified row and column
        if (game.moveTile(row, col)) {
            // If the move was successful, update the UI to reflect the new state
            updateUI();

            // Increment the move counter and update the display
            moveCount++;
            moveCounterTextView.setText(getString(R.string.move_counter, moveCount));

            // Check if the puzzle is solved
            if (game.isSolved()) {
                // Handle the game win scenario if the puzzle is solved
                handleGameWin();
            }
        }
    }

    private void handleGameWin() {
        gamesWon++;
        calculateWinPercentage();
        saveGameData();
        Snackbar.make(findViewById(R.id.bottom_app_bar), "Congratulations, you won!", Snackbar.LENGTH_LONG).show();
    }

    private void updateUI() {
        for (int i = 0; i < buttons.length; i++) {
            for (int j = 0; j < buttons[i].length; j++) {
                buttons[i][j].setText(String.valueOf(game.getTileValue(i, j)));
            }
        }
    }

    private void calculateWinPercentage() {
        if (gamesPlayed > 0) {
            winPercentage = (double) gamesWon / gamesPlayed * 100;
        } else {
            winPercentage = 0.0;
        }
    }

    private void saveGameData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_GAMES_PLAYED, gamesPlayed);
        editor.putInt(KEY_GAMES_WON, gamesWon);
        editor.putFloat(KEY_WIN_PERCENTAGE, (float) winPercentage);
        editor.apply();
    }

    private void loadGameData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        gamesPlayed = prefs.getInt(KEY_GAMES_PLAYED, 0);
        gamesWon = prefs.getInt(KEY_GAMES_WON, 0);
        winPercentage = prefs.getFloat(KEY_WIN_PERCENTAGE, 0.0f);
    }

    private void startChronometer() {
        if (chronometer != null) {
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
        } else {
            Log.e("MainActivity", "Chronometer is null. Check your layout file.");
        }
    }

    private void pauseGame() {
        if (chronometer != null && !isPaused) {
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            isPaused = true;
        }
    }

    private void resumeGame() {
        if (chronometer != null && isPaused) {
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
            chronometer.start();
            isPaused = false;
        }
    }

    private void restartGame(View view) {
        // Reset the puzzle by shuffling the tiles
        game.shuffleTiles();

        // Reset move counter
        moveCount = 0;
        moveCounterTextView.setText(getString(R.string.move_counter, moveCount));

        // Reset and start the chronometer
        pauseOffset = 0;
        if (chronometer != null) {
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
        } else {
            Log.e("MainActivity", "Chronometer is null. Check your layout file.");
        }

        // Update the UI to reflect the new game state
        updateUI();
    }

    private void goToMenu() {
        resetGameData();
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
        finish();
    }

    private void resetGameData() {
        moveCount = 0;
        if (moveCounterTextView != null) {
            moveCounterTextView.setText(getString(R.string.move_counter, moveCount));
        } else {
            Log.e("MainActivity", "MoveCounterTextView is null. Check your layout file.");
        }
    }

    private void showStatistics() {
        String stats = "Games Played: " + gamesPlayed +
                "\nGames Won: " + gamesWon +
                "\nWin Percentage: " + String.format(Locale.getDefault(), "%.2f", winPercentage) + "%";

        BottomAppBar bottomAppBar = findViewById(R.id.bottom_app_bar);
        if (bottomAppBar != null) {
            Snackbar.make(bottomAppBar, stats, Snackbar.LENGTH_LONG).show();
        } else {
            Log.e("MainActivity", "BottomAppBar is null. Check your layout file.");
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("gamesPlayed", gamesPlayed);
        outState.putInt("gamesWon", gamesWon);
        outState.putDouble("winPercentage", winPercentage);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        gamesPlayed = savedInstanceState.getInt("gamesPlayed");
        gamesWon = savedInstanceState.getInt("gamesWon");
        winPercentage = savedInstanceState.getDouble("winPercentage");
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeGame();
    }
}
