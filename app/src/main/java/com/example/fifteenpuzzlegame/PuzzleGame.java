package com.example.fifteenpuzzlegame;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the logic and state of a Fifteen Puzzle game, including tile movements, shuffling, and solving.
 */
public class PuzzleGame implements Parcelable {

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

    private final int gridSize;
    private final int[][] tiles;
    private int emptyRow;
    private int emptyCol;
    private boolean isGameFinished;

    /**
     * Constructor to initialize a new PuzzleGame.
     *
     * @param gridSize The size of the grid (e.g., 3x3, 4x4).
     */
    public PuzzleGame(int gridSize) {
        this.gridSize = gridSize;
        this.tiles = new int[gridSize][gridSize];
        initializeTiles();
        shuffleTiles();
    }

    /**
     * Constructor used for Parcelable.
     *
     * @param in The Parcel containing the PuzzleGame data.
     */
    protected PuzzleGame(Parcel in) {
        this.gridSize = in.readInt();
        this.tiles = new int[gridSize][gridSize];
        for (int i = 0; i < gridSize; i++) {
            in.readIntArray(tiles[i]);
        }
        this.emptyRow = in.readInt();
        this.emptyCol = in.readInt();
        this.isGameFinished = in.readByte() != 0; // Read boolean as byte
    }

    /**
     * Initializes the tiles in ascending order with the last tile as empty.
     */
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

    /**
     * Shuffles the tiles randomly, ensuring the puzzle remains solvable and not already solved.
     */
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

    /**
     * Attempts to move the tiles, either horizontally or vertically.
     *
     * @param row The row of the tile to move.
     * @param col The column of the tile to move.
     * @return True if the tile was successfully moved, false otherwise.
     */
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

    /**
     * Checks whether the current puzzle configuration is solvable.
     *
     * @return True if the puzzle is solvable, false otherwise.
     */
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

        return (inversions % 2 == 0);
    }

    /**
     * Checks whether the puzzle is currently solved.
     *
     * @return True if the puzzle is solved, false otherwise.
     */
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

    /**
     * Writes the PuzzleGame object to a Parcel.
     *
     * @param dest  The destination Parcel.
     * @param flags Flags for parceling.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(gridSize);
        for (int i = 0; i < gridSize; i++) {
            dest.writeIntArray(tiles[i]);
        }
        dest.writeInt(emptyRow);
        dest.writeInt(emptyCol);
        dest.writeByte((byte) (isGameFinished ? 1 : 0));  // Boolean as byte
    }

    /**
     * Describes the contents for Parcelable.
     *
     * @return An integer describing the contents (usually 0).
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Retrieves the value of a specific tile.
     *
     * @param row The row of the tile.
     * @param col The column of the tile.
     * @return The value of the tile.
     */
    public int getTileValue(int row, int col) {
        return tiles[row][col];
    }

    /**
     * Checks if the game is finished.
     *
     * @return True if the game is finished, false otherwise.
     */
    public boolean isGameFinished() {
        return isGameFinished;
    }

    /**
     * Retrieves the size of the puzzle grid.
     *
     * @return The grid size (e.g., 3 for 3x3 grid).
     */
    public int getGridSize() {
        return gridSize;
    }
}
