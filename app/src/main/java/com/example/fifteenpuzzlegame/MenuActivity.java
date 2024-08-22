package com.example.fifteenpuzzlegame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fifteenpuzzlegame.databinding.ActivityMenuBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class MenuActivity extends AppCompatActivity {

    private ActivityMenuBinding binding;
    private int numButtonRows;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        if (savedInstanceState != null) {
            numButtonRows = savedInstanceState.getInt("numButtonRows", 4); // Default to 4x4
        } else {
            numButtonRows = 4; // Default grid size if no state is saved
        }

        setFAB();
        setMenuButtons();
    }

    private void setMenuButtons() {
        ArrayList<Button> buttons = new ArrayList<>();
        buttons.add(findViewById(R.id.grid_size_button_3x3));
        buttons.add(findViewById(R.id.grid_size_button_4x4));
        buttons.add(findViewById(R.id.grid_size_button_5x5));

        View.OnClickListener onClickListener = this::onButtonClick;
        for (Button button : buttons) {
            button.setOnClickListener(onClickListener);
        }
    }

    private void setFAB() {
        binding.fab.setOnClickListener(view -> Snackbar.make(view,
                        "Fifteen Game Puzzle semester project for Android App Development by Joseph Guindi & Yehoshua Dusowitz",
                        Snackbar.LENGTH_LONG)
                .setAnchorView(view)
                .setAction("Action", null).show());
    }

    private void onButtonClick(View view) {
        int bClicked = view.getId();

        if (bClicked == R.id.grid_size_button_3x3) {
            numButtonRows = 3;
        } else if (bClicked == R.id.grid_size_button_4x4) {
            numButtonRows = 4;
        } else if (bClicked == R.id.grid_size_button_5x5) {
            numButtonRows = 5;
        }

        // Create an Intent to launch MainActivity
        Intent intent = new Intent(this, MainActivity.class);

        // Pass the numButtonRows value as an extra
        intent.putExtra("numButtonRows", numButtonRows);

        // Start MainActivity
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("numButtonRows", numButtonRows);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        numButtonRows = savedInstanceState.getInt("numButtonRows", 4);
    }
}
