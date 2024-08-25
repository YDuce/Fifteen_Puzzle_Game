package com.example.fifteenpuzzlegame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        setupToolbar();
        displayStatistics();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void displayStatistics() {
        Intent intent = getIntent();

        int gamesPlayed = intent.getIntExtra("gamesPlayed", 0);
        int gamesWon = intent.getIntExtra("gamesWon", 0);
        String winPercentage = intent.getStringExtra("winPercentage");
        long bestTime = intent.getLongExtra("bestTime", 0);
        int bestMoveCount = intent.getIntExtra("bestMoveCount", 0);

        String bestTimeFormatted = formatTime(bestTime);

        setTextViewText(R.id.games_played, "Games Played: " + gamesPlayed);
        setTextViewText(R.id.games_won, "Games Won: " + gamesWon);
        setTextViewText(R.id.win_percentage, "Win Percentage: " + winPercentage + "%");
        setTextViewText(R.id.best_time, "Best Time: " + bestTimeFormatted);
        setTextViewText(R.id.best_move_count, "Best Move Count: " + bestMoveCount);
    }

    private void setTextViewText(int textViewId, String text) {
        TextView textView = findViewById(textViewId);
        textView.setText(text);
    }

    private String formatTime(long timeInMillis) {
        int minutes = (int) (timeInMillis / 1000) / 60;
        int seconds = (int) (timeInMillis / 1000) % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }
}
