package com.example.fifteenpuzzlegame;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fifteenpuzzlegame.databinding.ActivityMenuBinding;

/**
 * MenuActivity provides the UI for selecting the puzzle grid size.
 */
public class MenuActivity extends AppCompatActivity {

    private ActivityMenuBinding binding;

    /**
     * Called when the activity is starting. This is where the layout is set and the toolbar is initialized.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this contains the most recent data. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarMenu);
        initializeMenuButtons();
    }

    /**
     * Initializes the menu buttons for selecting grid sizes.
     * Each button click starts the MainActivity with the corresponding grid size.
     */
    private void initializeMenuButtons() {
        binding.gridSizeButton3x3.setOnClickListener(view -> launchGame(3));
        binding.gridSizeButton4x4.setOnClickListener(view -> launchGame(4));
        binding.gridSizeButton5x5.setOnClickListener(view -> launchGame(5));
    }

    /**
     * Launches the MainActivity with the specified grid size.
     *
     * @param gridSize The size of the puzzle grid (e.g., 3x3, 4x4, or 5x5).
     */
    private void launchGame(int gridSize) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("numButtonRows", gridSize); // Pass grid size to MainActivity
        startActivity(intent);
    }
}
