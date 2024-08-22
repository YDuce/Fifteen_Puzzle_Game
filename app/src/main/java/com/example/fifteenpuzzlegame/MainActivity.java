package com.example.fifteenpuzzlegame;

import android.os.Bundle;
import android.os.SystemClock;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

import com.example.fifteenpuzzlegame.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
//import android.widget.Chronometer;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ButtonAdapter adapter;
    private List<Integer> numbers;
    private GridView gridView;
//    private Chronometer chronometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupFAB();

        // Set up the GridView and buttons
        int numButtonRows = getIntent().getIntExtra("numButtonRows", 4); // Default to 4x4 grid
        gridView = findViewById(R.id.grid_view);

        numbers = new ArrayList<>();
        for (int i = 1; i < numButtonRows * numButtonRows; i++) {
            numbers.add(i);
        }
        numbers.add(0); // Adding the empty space for the 15th tile

        // Shuffle the numbers list to create a random puzzle state
        Collections.shuffle(numbers);

        adapter = new ButtonAdapter(this, numbers);
        gridView.setAdapter(adapter);
        gridView.setNumColumns(numButtonRows);

//        setupChronometer(); // Start the chronometer
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void setupToolbar() {
        setSupportActionBar((Toolbar) binding.toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupFAB() {
        if (binding.fab != null) {
            binding.fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAnchorView(R.id.fab)
                            .setAction("Action", null).show();
                }
            });
        }
    }

//    private void setupChronometer() {
//        chronometer = findViewById(R.id.chronometer);
//        chronometer.setBase(SystemClock.elapsedRealtime());
//        chronometer.start();
//    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Go back to the previous activity
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
