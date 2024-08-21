package com.example.fifteenpuzzlegame;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import com.example.fifteenpuzzlegame.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
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


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        setupToolbar();
        int numButtons = getIntent().getIntExtra("numButtons", 0);

        gridView = findViewById(R.id.grid_view);

        numbers = new ArrayList<>();
        for (int i = 1; i <= numButtons * numButtons; i++) {
            numbers.add(i);
        }
        adapter = new ButtonAdapter(this, numbers);
        gridView.setAdapter(adapter);
        gridView.setNumColumns(numButtons);
        gridView.setColumnWidth(60);

        adapter.notifyDataSetChanged(); // Refresh the GridView
    }

//    private void setupToolbar() {
//        setSupportActionBar(binding.toolbar);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//
//        if (item.getItemId() == android.R.id.home) {
//            getOnBackPressedDispatcher();
//            return true;
//        } else
//            return super.onOptionsItemSelected(item);
//    }
}