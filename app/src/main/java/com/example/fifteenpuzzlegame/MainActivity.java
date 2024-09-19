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
    private GameStateManager gameStateManager;
    private Gson gson;

    private int gridSize;

    // Game statistics variables
    private int gamesPlayed;
    private int gamesWon;
    private double winPercentage;
    private int moveCount;
    private long bestTime = Long.MAX_VALUE;
    private int bestMoveCount;

    private GridLayout gridLayout;
    private Button[][] buttons;

    private TextView moveCounterTextView;
    private Chronometer chronometer;

    private boolean isPaused;
    private long pauseOffset;

    /**
     * Called when the activity is created. Sets up the UI and initializes the game state.
     *
     * @param savedInstanceState Bundle containing the saved state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameStateManager = new GameStateManager(this);
        gson = new Gson();

        setupUI();
        loadStatistics();
        initGame(savedInstanceState);
    }

    /**
     * Sets up the main UI elements such as the toolbar and bottom app bar.
     */
    private void setupUI() {
        setupToolbar();
        setupBottomAppBar();
        gridLayout = findViewById(R.id.grid_layout);
        moveCounterTextView = findViewById(R.id.move_counter);
        chronometer = findViewById(R.id.chronometer);
    }

    /**
     * Configures the toolbar.
     */
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

    /**
     * Sets up the bottom app bar and initializes its buttons.
     */
    private void setupBottomAppBar() {
        BottomAppBar bottomAppBar = findViewById(R.id.bottom_app_bar);
        if (bottomAppBar != null) {
            setSupportActionBar(bottomAppBar);
        } else {
            Log.e("MainActivity", "BottomAppBar is null.");
        }
        setupBottomBarButtons();
    }

    /**
     * Initializes the buttons in the bottom app bar.
     */
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

    /**
     * Loads game statistics from GameStateManager.
     */
    private void loadStatistics() {
        gamesPlayed = gameStateManager.getGamesPlayed(gridSize);
        gamesWon = gameStateManager.getGamesWon(gridSize);
        winPercentage = gameStateManager.getWinPercentage(gridSize);
        bestTime = gameStateManager.getBestTime(gridSize);
        bestMoveCount = gameStateManager.getBestMoveCount(gridSize);
    }

    /**
     * Inflates the options menu and sets the state of auto-save and dark mode toggles.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        boolean isAutoSaveEnabled = gameStateManager.isAutoSaveEnabled();
        boolean isDarkModeEnabled = gameStateManager.isDarkModeEnabled();

        menu.findItem(R.id.action_autosave).setChecked(isAutoSaveEnabled);
        menu.findItem(R.id.action_darkmode).setChecked(isDarkModeEnabled);

        return true;
    }

    /**
     * Handles menu item selections.
     *
     * @param item The selected menu item.
     * @return true if the selection was handled.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_about) {
            showAboutDialog();
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

    /**
     * Toggles the auto-save setting and updates GameStateManager.
     *
     * @param item The menu item.
     */
    private void toggleAutoSave(MenuItem item) {
        item.setChecked(!item.isChecked());
        gameStateManager.setAutoSaveEnabled(item.isChecked());
    }

    /**
     * Toggles dark mode and applies the change.
     *
     * @param item The menu item.
     */
    private void toggleDarkMode(MenuItem item) {
        item.setChecked(!item.isChecked());
        gameStateManager.setDarkModeEnabled(item.isChecked());
        applyDarkMode(item.isChecked());
    }

    /**
     * Applies dark mode or light mode based on user selection.
     *
     * @param isDarkMode True if dark mode should be enabled.
     */
    private void applyDarkMode(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * Displays the "About" dialog with game information.
     */
    private void showAboutDialog() {
        String message = "Welcome to the Fifteen Puzzle Game!\n\n" + "How to Play:\n" + "1. You are presented with a 3x3, 4x4, or 5x5 grid of numbered tiles, with one tile missing.\n" + "2. The goal is to arrange the tiles in numerical order by sliding them into the empty space.\n" + "3. To move a tile, simply tap on it, and it will slide into the adjacent empty space.\n" + "4. Continue sliding tiles until the puzzle is solved.\n\n" + "Good luck and enjoy the game!";

        new AlertDialog.Builder(this).setTitle("About the Game").setMessage(message).setPositiveButton("OK", null).show();
    }

    /**
     * Initializes the game from a saved state or starts a new game.
     *
     * @param savedInstanceState The saved instance state.
     */
    private void initGame(Bundle savedInstanceState) {
        gridSize = getIntent().getIntExtra("numButtonRows", 4);

        if (savedInstanceState != null) {
            restoreGameStateFromBundle(savedInstanceState);
        } else {
            checkForPreviousGameOrStartNew();
        }
    }

    /**
     * Checks if a previous game exists and allows resuming it, or starts a new game.
     */
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

    /**
     * Restores the saved game state.
     *
     * @param savedGame The saved PuzzleGame instance.
     */
    private void restoreGameState(PuzzleGame savedGame) {
        game = savedGame;
        moveCount = gameStateManager.getMoveCount();
        pauseOffset = gameStateManager.getPauseOffset();
        isPaused = gameStateManager.isPaused();

        initializeGrid();
        updateUI();
        resumeGame();
    }

    /**
     * Starts a new game and initializes the game board.
     */
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

    /**
     * Initializes the grid layout for the puzzle.
     */
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

    /**
     * Calculates the size of each puzzle button based on screen dimensions.
     *
     * @return The size of each button.
     */
    private int calculateButtonSize() {
        int totalMargin = 10 * (gridSize + 1);
        int availableWidth = getResources().getDisplayMetrics().widthPixels - totalMargin;
        int availableHeight = getResources().getDisplayMetrics().heightPixels - totalMargin;
        return (int) ((double) Math.min(availableWidth, availableHeight) / gridSize * 0.9);
    }

    /**
     * Creates a button representing a tile in the puzzle.
     *
     * @param row        The row position of the tile.
     * @param col        The column position of the tile.
     * @param buttonSize The size of the button.
     */
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

    /**
     * Handles tile click events and moves tiles if possible.
     *
     * @param row The row of the clicked tile.
     * @param col The column of the clicked tile.
     */
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

    /**
     * Updates the UI elements after tile moves.
     */
    private void updateUI() {
        updateTiles();
        updateMoveCounter();
        chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
    }

    /**
     * Updates the tiles in the grid.
     */
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

    /**
     * Updates the move counter in the UI.
     */
    private void updateMoveCounter() {
        moveCounterTextView.setText(getString(R.string.move_counter, moveCount));
    }

    /**
     * Handles the game win scenario and shows a dialog with options to play again or return to the menu.
     */
    private void handleGameWin() {
        pauseGame();
        calculateGameStatistics();
        gameStateManager.deleteTemporaryGameState(gridSize);

        new AlertDialog.Builder(this).setTitle("Congratulations!").setMessage("You've solved the puzzle. What would you like to do next?").setPositiveButton("Play Again", (dialog, which) -> startNewGame()).setNegativeButton("Go to Menu", (dialog, which) -> goToMenu()).show();
    }

    /**
     * Displays the current game statistics in a dialog.
     */
    private void showStatistics() {
        pauseGame();

        String stats = gameStateManager.getFormattedStatistics(gridSize);
        new AlertDialog.Builder(this).setTitle("Current Game Statistics").setMessage(stats).setPositiveButton("View All Statistics", (dialog, which) -> launchStatisticsActivity()).setNegativeButton("Close", (dialog, which) -> resumeGame()).setCancelable(false).show();
    }

    /**
     * Launches the statistics activity to display overall game statistics.
     */
    private void launchStatisticsActivity() {
        Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
        intent.putExtra("prefsName", "GameStatsPrefs");
        startActivity(intent);
    }

    /**
     * Calculates and saves the game statistics after a win.
     */
    private void calculateGameStatistics() {
        gamesWon++;
        winPercentage = (gamesPlayed > 0) ? (double) gamesWon / gamesPlayed * 100 : 0.0;

        long currentTime = SystemClock.elapsedRealtime() - chronometer.getBase();
        if (currentTime < bestTime) bestTime = currentTime;
        if (moveCount < bestMoveCount || bestMoveCount == 0) bestMoveCount = moveCount;

        gameStateManager.saveGameStatistics(gridSize, gamesPlayed, gamesWon, winPercentage, bestTime, bestMoveCount);
    }

    /**
     * Saves the game state when the activity is paused or destroyed.
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("gameStateJson", gson.toJson(game));
        outState.putInt("moveCount", moveCount);
        outState.putLong("chronometerBase", chronometer.getBase());
        outState.putLong("pauseOffset", pauseOffset);
        outState.putBoolean("isPaused", isPaused);
    }

    /**
     * Restores the game state from the saved instance state.
     *
     * @param savedInstanceState The saved instance state.
     */
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

    /**
     * Restores the game state from the saved instance bundle.
     *
     * @param savedInstanceState The saved instance state.
     */
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

    /**
     * Saves the game state and statistics when the activity is paused.
     */
    @Override
    protected void onPause() {
        super.onPause();
        gameStateManager.saveTemporaryGameState(game, moveCount, pauseOffset, isPaused);
        gameStateManager.saveGameStatistics(gridSize, gamesPlayed, gamesWon, winPercentage, bestTime, bestMoveCount);
    }

    /**
     * Resumes the game state when the activity is resumed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        resumeGame();
    }

    /**
     * Saves the game state and statistics when the activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameStateManager.saveTemporaryGameState(game, moveCount, pauseOffset, isPaused);
        gameStateManager.saveGameStatistics(gridSize, gamesPlayed, gamesWon, winPercentage, bestTime, bestMoveCount);
    }

    /**
     * Pauses the chronometer.
     */
    private void pauseChronometer() {
        pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
        chronometer.stop();
    }

    /**
     * Resumes the chronometer.
     */
    private void resumeChronometer() {
        chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
        chronometer.start();
    }

    /**
     * Navigates back to the menu.
     */
    private void goToMenu() {
        pauseGame();
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Toggles the pause state of the game.
     */
    private void togglePause() {
        if (isPaused) {
            resumeGame();
        } else {
            pauseGame();
        }
    }

    /**
     * Pauses the game.
     */
    private void pauseGame() {
        isPaused = true;
        pauseChronometer();
        updatePauseButtonIcon(R.drawable.ic_play);
    }

    /**
     * Resumes the game.
     */
    private void resumeGame() {
        isPaused = false;
        resumeChronometer();
        updatePauseButtonIcon(R.drawable.ic_pause);
    }

    /**
     * Updates the icon of the pause button.
     *
     * @param iconResId The resource ID of the new icon.
     */
    private void updatePauseButtonIcon(int iconResId) {
        ImageButton buttonPause = findViewById(R.id.button_pause);
        if (buttonPause != null) {
            buttonPause.setImageResource(iconResId);
        } else {
            Log.e("MainActivity", "Pause button is null.");
        }
    }
}
