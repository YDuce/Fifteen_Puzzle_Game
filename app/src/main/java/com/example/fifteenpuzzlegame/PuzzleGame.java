package com.example.fifteenpuzzlegame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PuzzleGame {
    private final int[][] tiles;
    private final int gridSize;
    private int emptyRow;
    private int emptyCol;

    public PuzzleGame(int gridSize) {
        this.gridSize = gridSize;
        tiles = new int[gridSize][gridSize];
        shuffleTiles();
    }

    public int getGridSize() {
        return gridSize;
    }

    public int getTileValue(int row, int col) {
        return tiles[row][col];
    }

    public boolean moveTile(int row, int col) {
        if (isAdjacentToEmptyTile(row, col)) {
            swapTiles(row, col);
            return true;
        }
        return false;
    }

    public boolean isSolved() {
        int expectedValue = 1;
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (i == gridSize - 1 && j == gridSize - 1) {
                    return tiles[i][j] == 0; // Last tile should be empty
                } else if (tiles[i][j] != expectedValue) {
                    return false;
                }
                expectedValue++;
            }
        }
        return true;
    }

    public void shuffleTiles() {
        List<Integer> numbers = generateShuffledNumbers();
        populateTiles(numbers);
    }

    private List<Integer> generateShuffledNumbers() {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i < gridSize * gridSize; i++) {
            numbers.add(i);
        }
        numbers.add(0); // Empty tile

        Collections.shuffle(numbers);
        return numbers;
    }

    private void populateTiles(List<Integer> numbers) {
        int index = 0;
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                tiles[i][j] = numbers.get(index);
                if (tiles[i][j] == 0) {
                    emptyRow = i;
                    emptyCol = j;
                }
                index++;
            }
        }
    }

    private boolean isAdjacentToEmptyTile(int row, int col) {
        return (Math.abs(row - emptyRow) == 1 && col == emptyCol) ||
                (Math.abs(col - emptyCol) == 1 && row == emptyRow);
    }

    private void swapTiles(int row, int col) {
        tiles[emptyRow][emptyCol] = tiles[row][col];
        tiles[row][col] = 0;
        emptyRow = row;
        emptyCol = col;
    }
}
