package com.example.fifteenpuzzlegame;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.List;

public class ButtonAdapter extends ArrayAdapter<Integer> {
    private final Context context;
    private final List<Integer> numbers;

    public ButtonAdapter(Context context, List<Integer> numbers) {
        super(context, R.layout.grid_item, numbers);
        this.context = context;
        this.numbers = numbers;
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
        }

        Button button = convertView.findViewById(R.id.grid_button);
        button.setText(String.valueOf(numbers.get(position)));

        // Optional: Set a click listener for each button
        button.setOnClickListener(v -> {
            // Handle button click
            Toast.makeText(context, "Button " + numbers.get(position) + " clicked", Toast.LENGTH_SHORT).show();
        });

        return convertView;
    }
}
