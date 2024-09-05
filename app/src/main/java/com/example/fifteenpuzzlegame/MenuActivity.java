package com.example.fifteenpuzzlegame;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fifteenpuzzlegame.databinding.ActivityMenuBinding;
import com.google.android.material.snackbar.Snackbar;

public class MenuActivity extends AppCompatActivity {

    private ActivityMenuBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar and menu buttons
        setSupportActionBar(binding.toolbarMenu);
        initializeMenuButtons();
        initializeFAB();
    }

    private void initializeMenuButtons() {
        binding.gridSizeButton3x3.setOnClickListener(view -> launchGame(3));
        binding.gridSizeButton4x4.setOnClickListener(view -> launchGame(4));
        binding.gridSizeButton5x5.setOnClickListener(view -> launchGame(5));
    }

    private void initializeFAB() {
        binding.fab.setOnClickListener(view -> Snackbar.make(view, getString(R.string.fab_message), Snackbar.LENGTH_LONG).setAnchorView(view).setAction("Action", null).show());
    }

    // Launch the game with the specified grid size
    private void launchGame(int gridSize) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("numButtonRows", gridSize);    // Pass the selected grid size to MainActivity
        startActivity(intent);
    }
}
