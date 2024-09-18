package com.example.fifteenpuzzlegame;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fifteenpuzzlegame.databinding.ActivityMenuBinding;

public class MenuActivity extends AppCompatActivity {

    private ActivityMenuBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarMenu);
        initializeMenuButtons();
    }

    private void initializeMenuButtons() {
        binding.gridSizeButton3x3.setOnClickListener(view -> launchGame(3));
        binding.gridSizeButton4x4.setOnClickListener(view -> launchGame(4));
        binding.gridSizeButton5x5.setOnClickListener(view -> launchGame(5));
    }

    private void launchGame(int gridSize) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("numButtonRows", gridSize); // Pass grid size to MainActivity
        startActivity(intent);
    }
}
