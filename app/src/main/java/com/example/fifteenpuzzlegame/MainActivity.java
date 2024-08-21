package com.example.fifteenpuzzlegame;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import com.example.fifteenpuzzlegame.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ButtonAdapter adapter;
    private List<Integer> numbers;
    private GridView gridView;
    private EditText numberInput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupFAB();

        int numButtonRows = getIntent().getIntExtra("numButtonRows", 0);

        gridView = findViewById(R.id.grid_view);

        numbers = new ArrayList<>();
        for (int i = 1; i <= numButtonRows * numButtonRows; i++) {
            numbers.add(i);
        }
        adapter = new ButtonAdapter(this, numbers);
        gridView.setAdapter(adapter);
        gridView.setNumColumns(numButtonRows);
        gridView.setColumnWidth(60);

        adapter.notifyDataSetChanged(); // Refresh the GridView
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupFAB() {
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }


}