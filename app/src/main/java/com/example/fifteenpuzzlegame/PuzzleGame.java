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
        initializeTiles();  // Initialize the tiles with numbers, leaving the last one blank
        shuffleTiles();     // Optional: Shuffle the tiles to start the game in a random state
    }

    private void initializeTiles() {
        int number = 1;  // Start from 1
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (i == gridSize - 1 && j == gridSize - 1) {
                    tiles[i][j] = 0;  // Set the last tile as blank (0)
                    emptyRow = i;
                    emptyCol = j;
                } else {
                    tiles[i][j] = number++;
                }
            }
        }
    }

    void shuffleTiles() {
        List<Integer> flatTiles = new ArrayList<>();
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                flatTiles.add(tiles[i][j]);
            }
        }

        // Shuffle the tiles
        Collections.shuffle(flatTiles);

        // Reassign shuffled tiles back to the 2D array
        int index = 0;
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                tiles[i][j] = flatTiles.get(index);
                if (tiles[i][j] == 0) {
                    emptyRow = i;
                    emptyCol = j;
                }
                index++;
            }
        }
    }

    public int getTileValue(int row, int col) {
        return tiles[row][col];
    }

    public int getGridSize() {
        return gridSize;
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
                    return tiles[i][j] == 0;  // Last tile should be empty
                } else if (tiles[i][j] != expectedValue) {
                    return false;
                }
                expectedValue++;
            }
        }
        return true;
    }

    private void swapTiles(int row, int col) {
        tiles[emptyRow][emptyCol] = tiles[row][col];
        tiles[row][col] = 0;
        emptyRow = row;
        emptyCol = col;
    }

    private boolean isAdjacentToEmptyTile(int row, int col) {
        return (Math.abs(row - emptyRow) == 1 && col == emptyCol) ||
                (Math.abs(col - emptyCol) == 1 && row == emptyRow);
    }
}
