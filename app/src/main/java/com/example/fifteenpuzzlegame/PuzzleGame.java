package com.example.fifteenpuzzlegame;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PuzzleGame implements Parcelable {

    // Parcelable.Creator for PuzzleGame
    public static final Creator<PuzzleGame> CREATOR = new Creator<PuzzleGame>() {
        @Override
        public PuzzleGame createFromParcel(Parcel in) {
            return new PuzzleGame(in);
        }

        @Override
        public PuzzleGame[] newArray(int size) {
            return new PuzzleGame[size];
        }
    };
    // Gson instance (reuse for serialization/deserialization)
    private static final Gson gson = new GsonBuilder().create();
    private final int gridSize;
    private final int[][] tiles;
    private int emptyRow;
    private int emptyCol;
    private boolean isGameFinished;

    // Constructor to initialize a new game
    public PuzzleGame(int gridSize) {
        this.gridSize = gridSize;
        this.tiles = new int[gridSize][gridSize];
        initializeTiles();
        shuffleTiles();
    }

    // Constructor used for Parcelable
    protected PuzzleGame(Parcel in) {
        this.gridSize = in.readInt();
        this.tiles = new int[gridSize][gridSize];
        for (int i = 0; i < gridSize; i++) {
            in.readIntArray(tiles[i]);
        }
        this.emptyRow = in.readInt();
        this.emptyCol = in.readInt();
        this.isGameFinished = in.readByte() != 0; // read boolean as byte
    }

    // Convert JSON string back to PuzzleGame object using Gson
    public static PuzzleGame fromJson(String jsonString) {
        return gson.fromJson(jsonString, PuzzleGame.class);
    }

    // Initialize tiles in ascending order with the last tile empty
    private void initializeTiles() {
        int number = 1;
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (i == gridSize - 1 && j == gridSize - 1) {
                    tiles[i][j] = 0;  // Last tile is empty
                } else {
                    tiles[i][j] = number++;
                }
            }
        }
        emptyRow = gridSize - 1;
        emptyCol = gridSize - 1;
    }

    // Shuffle the tiles while ensuring the puzzle is solvable and not solved
    public void shuffleTiles() {
        List<Integer> flatTiles = new ArrayList<>();
        for (int[] row : tiles) {
            for (int tile : row) {
                flatTiles.add(tile);
            }
        }

        // Shuffle until a solvable and unsolved configuration is found
        do {
            Collections.shuffle(flatTiles);
            int index = 0;
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    tiles[i][j] = flatTiles.get(index++);
                    if (tiles[i][j] == 0) {
                        emptyRow = i;
                        emptyCol = j;
                    }
                }
            }
        } while (!isSolvable() || isSolved());
    }

    // Attempt to move the tiles, returns true if successful
    public boolean moveTiles(int row, int col) {
        if (row == emptyRow || col == emptyCol) {
            if (row == emptyRow) {
                moveHorizontally(row, col);
            } else {
                moveVertically(row, col);
            }
            return true;
        }
        return false;
    }

    private void moveHorizontally(int row, int col) {
        if (col > emptyCol) {
            for (int c = emptyCol; c < col; c++) {
                swapTiles(row, c, row, c + 1);
            }
        } else {
            for (int c = emptyCol; c > col; c--) {
                swapTiles(row, c, row, c - 1);
            }
        }
    }

    private void moveVertically(int row, int col) {
        if (row > emptyRow) {
            for (int r = emptyRow; r < row; r++) {
                swapTiles(r, col, r + 1, col);
            }
        } else {
            for (int r = emptyRow; r > row; r--) {
                swapTiles(r, col, r - 1, col);
            }
        }
    }

    private void swapTiles(int row1, int col1, int row2, int col2) {
        int temp = tiles[row1][col1];
        tiles[row1][col1] = tiles[row2][col2];
        tiles[row2][col2] = temp;

        emptyRow = row2;
        emptyCol = col2;
    }

    // Check if the puzzle is solvable
    private boolean isSolvable() {
        int[] flatTiles = new int[gridSize * gridSize - 1];
        int index = 0;
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (tiles[i][j] != 0) {
                    flatTiles[index++] = tiles[i][j];
                }
            }
        }

        // Count inversions
        int inversions = 0;
        for (int i = 0; i < flatTiles.length - 1; i++) {
            for (int j = i + 1; j < flatTiles.length; j++) {
                if (flatTiles[i] > flatTiles[j]) {
                    inversions++;
                }
            }
        }

        return (inversions % 2 == 0);  // Solvable if inversions are even
    }

    // Check if the puzzle is solved
    public boolean isSolved() {
        int expectedValue = 1;
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (i == gridSize - 1 && j == gridSize - 1) {
                    return tiles[i][j] == 0;
                } else if (tiles[i][j] != expectedValue++) {
                    return false;
                }
            }
        }
        return true;
    }

    // Parcelable methods
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(gridSize);
        for (int i = 0; i < gridSize; i++) {
            dest.writeIntArray(tiles[i]);
        }
        dest.writeInt(emptyRow);
        dest.writeInt(emptyCol);
        dest.writeByte((byte) (isGameFinished ? 1 : 0));  // boolean as byte
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Convert PuzzleGame to JSON string using Gson
    public String toJson() {
        return gson.toJson(this);
    }

    // Getters
    public int[][] getTiles() {
        return tiles;
    }

    public int getTileValue(int row, int col) {
        return tiles[row][col];
    }

    public int getEmptyRow() {
        return emptyRow;
    }

    public int getEmptyCol() {
        return emptyCol;
    }

    public boolean isGameFinished() {
        return isGameFinished;
    }

    public int getGridSize() {
        return gridSize;
    }
}
