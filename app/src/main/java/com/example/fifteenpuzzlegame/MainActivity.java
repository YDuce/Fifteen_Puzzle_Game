package com.example.fifteenpuzzlegame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "GameStatsPrefs";
    private static final String KEY_GAMES_PLAYED = "gamesPlayed";
    private static final String KEY_GAMES_WON = "gamesWon";
    private static final String KEY_WIN_PERCENTAGE = "winPercentage";

    private static final int GRID_SIZE = 4; // Default to a 4x4 grid
    private final Button[][] buttons = new Button[GRID_SIZE][GRID_SIZE];
    private GridLayout gridLayout;
    private int emptyRow = GRID_SIZE - 1;
    private int emptyCol = GRID_SIZE - 1;
    private int moveCount = 0; // Track the number of moves
    private int gamesPlayed = 0; // Track the number of games played in this session
    private int gamesWon = 0; // Track the number of games won in this session
    private double winPercentage = 0.0; // Track the win percentage
    private TextView moveCounterTextView;
    private Chronometer chronometer;
    private boolean isPaused = false;
    private long pauseOffset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbar();
        setupBottomAppBar();

        gridLayout = findViewById(R.id.grid_layout);
        moveCounterTextView = findViewById(R.id.move_counter);
        chronometer = findViewById(R.id.chronometer);

        // Load persisted data
        loadGameData();

        gridLayout.setRowCount(GRID_SIZE);
        gridLayout.setColumnCount(GRID_SIZE);

        initializeGrid();  // This will now handle the dynamic creation of grid buttons
        shuffleTiles();
        startChronometer();

        // Increment the number of games played
        gamesPlayed++;
        saveGameData();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide default title to show custom elements like timer and move counter
    }

    private void setupBottomAppBar() {
        BottomAppBar bottomAppBar = findViewById(R.id.bottom_app_bar);
        setSupportActionBar(bottomAppBar);
        bottomAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.action_pause) {
                    if (isPaused) {
                        resumeGame();
                    } else {
                        pauseGame();
                    }
                    return true;
                } else if (itemId == R.id.action_menu) {
                    goToMenu();
                    return true;
                } else if (itemId == R.id.action_stats) {
                    showStatistics();
                    return true;
                }
                return false;
            }
        });

        // Set up button click listeners if not using a menu
        findViewById(R.id.button_menu).setOnClickListener(v -> goToMenu());
        findViewById(R.id.button_pause).setOnClickListener(v -> {
            if (isPaused) {
                resumeGame();
            } else {
                pauseGame();
            }
        });
        findViewById(R.id.button_stats).setOnClickListener(v -> showStatistics());
    }


    private void initializeGrid() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                Button button = new Button(this);
                button.setText(String.valueOf(i * GRID_SIZE + j + 1));

                if (i == GRID_SIZE - 1 && j == GRID_SIZE - 1) {
                    button.setText(""); // Empty tile for the last position
                }

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec = GridLayout.spec(i, 1f);
                params.columnSpec = GridLayout.spec(j, 1f);
                params.width = 0;
                params.height = 0;
                params.setMargins(5, 5, 5, 5);

                button.setLayoutParams(params);
                button.setOnClickListener(v -> onTileClick(button, i, j));

                buttons[i][j] = button;
                gridLayout.addView(button);
            }
        }
    }

    private void onTileClick(Button button, int row, int col) {
        // Check if the tile clicked is adjacent to the empty tile
        if ((Math.abs(row - emptyRow) == 1 && col == emptyCol) ||
                (Math.abs(col - emptyCol) == 1 && row == emptyRow)) {
            // Swap the tiles
            buttons[emptyRow][emptyCol].setText(button.getText());
            button.setText("");

            // Update the empty tile's position
            emptyRow = row;
            emptyCol = col;

            // Increment move count
            moveCount++;
            moveCounterTextView.setText("Moves: " + moveCount);

            // Check if the player has won
            if (checkIfWon()) {
                gamesWon++;
                calculateWinPercentage();
                saveGameData();
                // Optionally display a message or handle win scenario
            }
        }
    }

    private boolean checkIfWon() {
        int expectedValue = 1;
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (i == GRID_SIZE - 1 && j == GRID_SIZE - 1) {
                    return buttons[i][j].getText().equals(""); // Last tile should be empty
                } else if (!buttons[i][j].getText().toString().equals(String.valueOf(expectedValue))) {
                    return false;
                }
                expectedValue++;
            }
        }
        return true;
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

    private void shuffleTiles() {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i < GRID_SIZE * GRID_SIZE; i++) {
            numbers.add(i);
        }
        numbers.add(0); // 0 represents the empty space

        Collections.shuffle(numbers);

        int index = 0;
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (index < numbers.size() - 1) {
                    buttons[i][j].setText(String.valueOf(numbers.get(index)));
                } else {
                    buttons[i][j].setText(""); // Empty tile
                    emptyRow = i;
                    emptyCol = j;
                }
                index++;
            }
        }

        // Reset move count
        moveCount = 0;
        moveCounterTextView.setText("Moves: " + moveCount);
    }

    private void startChronometer() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }

    private void pauseGame() {
        if (!isPaused) {
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            isPaused = true;
        }
    }

    private void resumeGame() {
        if (isPaused) {
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
            chronometer.start();
            isPaused = false;
        }
    }

    private void goToMenu() {
        // Navigate to menu activity to change grid size or other settings
        Intent intent = new Intent(this, MenuActivity.class);
        resetGameData(); // Reset game data when going back to the menu
        startActivity(intent);
        finish(); // Close MainActivity to ensure it's reset on return
    }

    // Method to restart the game
    public void restartGame(View view) {
        shuffleTiles();
        pauseOffset = 0;  // Reset the pause offset to ensure accurate time tracking
        startChronometer();
        resetGameData(); // Reset game data when restarting
    }

    private void resetGameData() {
        // Reset the tracking variables when the game is restarted or MainActivity is relaunched
        gamesPlayed = 0;
        gamesWon = 0;
        winPercentage = 0.0;
        saveGameData();
    }

    private void showStatistics() {
        String stats = "Games Played: " + gamesPlayed +
                "\nGames Won: " + gamesWon +
                "\nWin Percentage: " + String.format("%.2f", winPercentage) + "%";
        Snackbar.make(findViewById(R.id.bottom_app_bar), stats, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPaused) {
            resumeGame();
        }
    }
}
