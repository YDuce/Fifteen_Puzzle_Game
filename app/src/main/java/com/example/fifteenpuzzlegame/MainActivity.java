package com.example.fifteenpuzzlegame;

import static java.util.Objects.*;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "GameStatsPrefs";
    private static final String KEY_GAMES_PLAYED = "gamesPlayed";
    private static final String KEY_GAMES_WON = "gamesWon";
    private static final String KEY_WIN_PERCENTAGE = "winPercentage";

    private int GRID_SIZE;
    private Button[][] buttons;
    private GridLayout gridLayout;
    private int emptyRow;
    private int emptyCol;
    private int moveCount = 0;
    private int gamesPlayed = 0;
    private int gamesWon = 0;
    private double winPercentage = 0.0;
    private TextView moveCounterTextView;
    private Chronometer chronometer;
    private boolean isPaused = false;
    private long pauseOffset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        GRID_SIZE = intent.getIntExtra("numButtonRows", 4);
        buttons = new Button[GRID_SIZE][GRID_SIZE];
        emptyRow = GRID_SIZE - 1;
        emptyCol = GRID_SIZE - 1;

        setupToolbar();
        setupBottomAppBar();

        gridLayout = findViewById(R.id.grid_layout);
        moveCounterTextView = findViewById(R.id.move_counter);
        chronometer = findViewById(R.id.chronometer);

        loadGameData();

        initializeGrid();
        shuffleTiles();
        startChronometer();

        gamesPlayed++;
        saveGameData();
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
        setSupportActionBar(bottomAppBar);

        ImageButton buttonHome = findViewById(R.id.button_home);
        if (buttonHome != null) {
            buttonHome.setOnClickListener(v -> goToMenu());
        } else {
            Log.e("MainActivity", "buttonHome is null. Check your layout file.");
        }

        ImageButton buttonPause = findViewById(R.id.button_pause);
        if (buttonPause != null) {
            buttonPause.setOnClickListener(v -> {
                if (isPaused) {
                    resumeGame();
                    Log.d("MainActivity", "Game resumed.");
                } else {
                    pauseGame();
                    Log.d("MainActivity", "Game paused.");
                }
            });
        } else {
            Log.e("MainActivity", "buttonPause is null. Check your layout file.");
        }

        ImageButton buttonStats = findViewById(R.id.button_stats);
        if (buttonStats != null) {
            buttonStats.setOnClickListener(v -> showStatistics());
        } else {
            Log.e("MainActivity", "buttonStats is null. Check your layout file.");
        }

        ImageButton buttonRestart = findViewById(R.id.button_restart);
        if (buttonRestart != null) {
            buttonRestart.setOnClickListener(this::restartGame);
            Log.i("MainActivity", "Game restarted.");
        } else {
            Log.e("MainActivity", "buttonRestart is null. Check your layout file.");
        }
    }

    private void initializeGrid() {
        gridLayout.setRowCount(GRID_SIZE);
        gridLayout.setColumnCount(GRID_SIZE);

        int totalMargin = 5 * (GRID_SIZE + 1);
        int availableWidth = getResources().getDisplayMetrics().widthPixels - totalMargin;
        int buttonSize = availableWidth / GRID_SIZE;

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                Button button = new Button(this);

                button.setText(String.valueOf(i * GRID_SIZE + j + 1));

                if (i == GRID_SIZE - 1 && j == GRID_SIZE - 1) {
                    button.setText("");
                }

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec = GridLayout.spec(i, 1f);
                params.columnSpec = GridLayout.spec(j, 1f);
                params.width = buttonSize;
                params.height = buttonSize;
                params.setMargins(5, 5, 5, 5);

                button.setLayoutParams(params);

                final int row = i;
                final int col = j;

                button.setOnClickListener(v -> onTileClick(button, row, col));

                buttons[i][j] = button;
                gridLayout.addView(button);
            }
        }
    }

    private void onTileClick(Button button, int row, int col) {
        if ((Math.abs(row - emptyRow) == 1 && col == emptyCol) ||
                (Math.abs(col - emptyCol) == 1 && row == emptyRow)) {

            Button emptyButton = buttons[emptyRow][emptyCol];

            float translationX = emptyButton.getX() - button.getX();
            float translationY = emptyButton.getY() - button.getY();

            button.animate()
                    .translationXBy(translationX)
                    .translationYBy(translationY)
                    .setDuration(300)
                    .withEndAction(() -> {
                        emptyButton.setText(button.getText());
                        button.setText("");

                        button.setTranslationX(0);
                        button.setTranslationY(0);

                        emptyRow = row;
                        emptyCol = col;

                        moveCount++;
                        moveCounterTextView.setText(getString(R.string.move_counter, moveCount));

                        if (checkIfWon()) {
                            button.postDelayed(() -> {
                                gamesWon++;
                                calculateWinPercentage();
                                saveGameData();
                                Snackbar.make(findViewById(R.id.bottom_app_bar), "Congratulations, you won!", Snackbar.LENGTH_LONG).show();
                            }, 500);
                        }
                    })
                    .start();
        }
    }

    private boolean checkIfWon() {
        int expectedValue = 1;
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (i == GRID_SIZE - 1 && j == GRID_SIZE - 1) {
                    return buttons[i][j].getText().equals("");
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
        numbers.add(0);

        Collections.shuffle(numbers);

        int index = 0;
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (index < numbers.size() - 1) {
                    buttons[i][j].setText(String.valueOf(numbers.get(index)));
                } else {
                    buttons[i][j].setText("");
                    emptyRow = i;
                    emptyCol = j;
                }
                index++;
            }
        }

        moveCount = 0;
        moveCounterTextView.setText(getString(R.string.move_counter, moveCount));
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
        // Reset the tracking variables related to the current game
        moveCount = 0;
        moveCounterTextView.setText(getString(R.string.move_counter, moveCount));
    }


    private void showStatistics() {
        String stats = "Games Played: " + gamesPlayed +
                "\nGames Won: " + gamesWon +
                "\nWin Percentage: " + String.format(Locale.getDefault(), "%.2f", winPercentage) + "%";
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
