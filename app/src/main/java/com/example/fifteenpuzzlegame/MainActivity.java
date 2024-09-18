package com.example.fifteenpuzzlegame;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {

    private PuzzleGame game;
    private Button[][] buttons;
    private GridLayout gridLayout;
    private int gridSize;

    // Game statistics variables
    private int gamesPlayed;
    private int gamesWon;
    private double winPercentage;
    private int moveCount;
    private long bestTime = Long.MAX_VALUE;
    private int bestMoveCount;

    private TextView moveCounterTextView;
    private Chronometer chronometer;
    private boolean isPaused;
    private long pauseOffset;

    private GameStateManager gameStateManager;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameStateManager = new GameStateManager(this);  // Initialize GameStateManager
        gson = new Gson(); // Initialize Gson object for game state handling

        setupUI();
        loadStatistics();  // Load statistics using GameStateManager
        initGame(savedInstanceState);  // Initialize game based on saved state
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
        buttonRestart.setOnClickListener(view -> startNewGame());
    }

    // Load game statistics using GameStateManager
    private void loadStatistics() {
        gamesPlayed = gameStateManager.getGamesPlayed(gridSize);
        gamesWon = gameStateManager.getGamesWon(gridSize);
        winPercentage = gameStateManager.getWinPercentage(gridSize);
        bestTime = gameStateManager.getBestTime(gridSize);
        bestMoveCount = gameStateManager.getBestMoveCount(gridSize);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Retrieve preferences for Auto-Save and Dark Mode from GameStateManager
        boolean isAutoSaveEnabled = gameStateManager.isAutoSaveEnabled();
        boolean isDarkModeEnabled = gameStateManager.isDarkModeEnabled();

        menu.findItem(R.id.action_autosave).setChecked(isAutoSaveEnabled);
        menu.findItem(R.id.action_darkmode).setChecked(isDarkModeEnabled);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_about) {
            showAboutDialog();  // Show the "About" dialog
            return true;
        } else if (itemId == R.id.action_autosave) {
            toggleAutoSave(item);
            return true;
        } else if (itemId == R.id.action_darkmode) {
            toggleDarkMode(item);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Toggle Auto-Save option
    private void toggleAutoSave(MenuItem item) {
        item.setChecked(!item.isChecked());
        gameStateManager.setAutoSaveEnabled(item.isChecked());
    }

    // Toggle Dark Mode and apply it immediately
    private void toggleDarkMode(MenuItem item) {
        item.setChecked(!item.isChecked());
        gameStateManager.setDarkModeEnabled(item.isChecked());
        applyDarkMode(item.isChecked());
    }

    private void applyDarkMode(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this).setTitle("About the Game").setMessage("This is the Fifteen Puzzle Game.\nVersion 1.0\nEnjoy solving the puzzle!").setPositiveButton("OK", null).show();
    }

    // Initialize game (from saved state or new game)
    private void initGame(Bundle savedInstanceState) {
        gridSize = getIntent().getIntExtra("numButtonRows", 4);

        if (savedInstanceState != null) {
            restoreGameStateFromBundle(savedInstanceState);
        } else {
            checkForPreviousGameOrStartNew();
        }
    }

    private void checkForPreviousGameOrStartNew() {
        PuzzleGame savedGame = gameStateManager.loadTemporaryGameState(gridSize);

        if (savedGame != null && !savedGame.isGameFinished()) {
            new AlertDialog.Builder(this).setTitle("Continue Previous Game?").setMessage("You have an unfinished game. Continue or start a new one?").setPositiveButton("Continue", (dialog, which) -> restoreGameState(savedGame)).setNegativeButton("New Game", (dialog, which) -> {
                gameStateManager.deleteTempGameState(gridSize);
                startNewGame();
            }).show();
        } else {
            startNewGame();
        }
    }

    private void restoreGameState(PuzzleGame savedGame) {
        game = savedGame;
        moveCount = gameStateManager.getMoveCount();
        pauseOffset = gameStateManager.getPauseOffset();
        isPaused = gameStateManager.isPaused();

        initializeGrid();
        updateUI();
        resumeGame();
    }

    private void startNewGame() {
        gameStateManager.deleteTempGameState(gridSize);
        game = new PuzzleGame(gridSize);

        moveCount = 0;
        pauseOffset = 0;
        moveCounterTextView.setText(getString(R.string.move_counter, moveCount));

        initializeGrid();
        updateUI();
        resumeGame();
    }

    private void initializeGrid() {
        gridSize = game.getGridSize();
        buttons = new Button[gridSize][gridSize];
        gridLayout.setRowCount(gridSize);
        gridLayout.setColumnCount(gridSize);

        int buttonSize = calculateButtonSize();
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                createButton(i, j, buttonSize);
            }
        }
    }

    private int calculateButtonSize() {
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

    private void onTileClick(int row, int col) {
        if (!isPaused && game.moveTiles(row, col)) {
            moveCount++;
            updateUI();
            gameStateManager.saveTemporaryGameState(game, moveCount, pauseOffset, isPaused);
            if (game.isSolved()) {
                handleGameWin();
            }
        }
    }

    private void updateUI() {
        updateTiles();
        updateMoveCounter();
        chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
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

    private void handleGameWin() {
        pauseGame();
        calculateGameStatistics();
        gameStateManager.deleteTemporaryGameState(gridSize);  // Delete the temporary state (board, move count, etc.)

        new AlertDialog.Builder(this).setTitle("Congratulations!").setMessage("You've solved the puzzle. What would you like to do next?").setPositiveButton("Play Again", (dialog, which) -> startNewGame()).setNegativeButton("Go to Menu", (dialog, which) -> goToMenu()).show();
    }

    private void showStatistics() {
        pauseGame();

        String stats = gameStateManager.getFormattedStatistics(gridSize);
        new AlertDialog.Builder(this).setTitle("Current Game Statistics").setMessage(stats).setPositiveButton("View All Statistics", (dialog, which) -> launchStatisticsActivity()).setNegativeButton("Close", (dialog, which) -> resumeGame()).setCancelable(false).show();
    }

    private void launchStatisticsActivity() {
        Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
        intent.putExtra("prefsName", "GameStatsPrefs"); // Passing the shared preferences name
        startActivity(intent);
    }

    private void calculateGameStatistics() {
        gamesWon++;
        winPercentage = (gamesPlayed > 0) ? (double) gamesWon / gamesPlayed * 100 : 0.0;

        long currentTime = SystemClock.elapsedRealtime() - chronometer.getBase();
        if (currentTime < bestTime) bestTime = currentTime;
        if (moveCount < bestMoveCount || bestMoveCount == 0) bestMoveCount = moveCount;

        gameStateManager.saveGameStatistics(gridSize, gamesPlayed, gamesWon, winPercentage, bestTime, bestMoveCount);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("gameStateJson", gson.toJson(game));
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
            game = gson.fromJson(gameStateJson, PuzzleGame.class);
            initializeGrid();
            updateUI();
        } else {
            Log.e("MainActivity", "Game state JSON is null. Starting a new game.");
            startNewGame();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameStateManager.saveTemporaryGameState(game, moveCount, pauseOffset, isPaused);
        gameStateManager.saveGameStatistics(gridSize, gamesPlayed, gamesWon, winPercentage, bestTime, bestMoveCount);
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeGame();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameStateManager.saveTemporaryGameState(game, moveCount, pauseOffset, isPaused);
        gameStateManager.saveGameStatistics(gridSize, gamesPlayed, gamesWon, winPercentage, bestTime, bestMoveCount);
    }

    private void pauseChronometer() {
        pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
        chronometer.stop();
    }

    private void resumeChronometer() {
        chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
        chronometer.start();
    }

    private void goToMenu() {
        pauseGame();
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
        finish();
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
}
