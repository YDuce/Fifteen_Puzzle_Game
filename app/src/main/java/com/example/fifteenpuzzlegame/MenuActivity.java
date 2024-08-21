package com.example.fifteenpuzzlegame;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;

import com.example.fifteenpuzzlegame.databinding.ActivityMenuBinding;

import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AppCompatActivity {

    private ActivityMenuBinding binding;
    private int numButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        setFAB();
        setMenuButtons();

    }

    private void setMenuButtons() {
        ArrayList<Button> buttons = new ArrayList<Button>();
        buttons.add(findViewById(R.id.grid_size_button_3x3));
        buttons.add(findViewById(R.id.grid_size_button_4x4));
        buttons.add(findViewById(R.id.grid_size_button_5x5));

        View.OnClickListener onClickListener = this::onButtonClick;
        buttons.forEach(button -> {
            button.setOnClickListener(onClickListener);
        });
    }

    private void setFAB() {
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show();
            }
        });
    }

    private void onButtonClick(View view) {

        int bClicked = view.getId();

        if (bClicked == R.id.grid_size_button_3x3) {
            numButtons = 3;
        } else if (bClicked == R.id.grid_size_button_4x4) {
            numButtons = 4;
        } else if (bClicked == R.id.grid_size_button_5x5) {
            numButtons = 5;
        }

        // Create an Intent to launch MainActivity
        Intent intent = new Intent(this, MainActivity.class);

        // Pass the numButtons value as an extra
        intent.putExtra("numButtons", numButtons);

        // Start MainActivity
        startActivity(intent);
    }

}

