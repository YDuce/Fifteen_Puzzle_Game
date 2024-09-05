package com.example.fifteenpuzzlegame;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    }

    // Convert JSON to PuzzleGame object
    public static PuzzleGame fromJson(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);

            // Validate required fields in the JSON
            if (!jsonObject.has("gridSize") || !jsonObject.has("board")) {
                Log.e("PuzzleGame", "Invalid JSON data: Missing gridSize or board");
                return null;  // Return null if required fields are missing
            }

            int gridSize = jsonObject.getInt("gridSize");
            PuzzleGame game = new PuzzleGame(gridSize);

            // Set game parameters from JSON
            game.emptyRow = jsonObject.getInt("emptyRow");
            game.emptyCol = jsonObject.getInt("emptyCol");
            game.isGameFinished = jsonObject.getBoolean("isGameFinished");

            // Restore board state from JSON array
            JSONArray boardArray = jsonObject.getJSONArray("board");
            for (int i = 0; i < gridSize; i++) {
                JSONArray rowArray = boardArray.getJSONArray(i);
                for (int j = 0; j < gridSize; j++) {
                    game.tiles[i][j] = rowArray.getInt(j);
                }
            }

            return game;

        } catch (JSONException e) {
            Log.e("PuzzleGame", "Failed to parse JSON: " + e.getMessage());
            return null;
        }
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

    // Move tiles horizontally
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

    // Move tiles vertically
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

    // Swap two tiles and update empty tile position
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
                    if (tiles[i][j] == 0) {
                        isGameFinished = true;
                        return true;
                    }
                } else if (tiles[i][j] != expectedValue++) {
                    isGameFinished = false;
                    return false;
                }
            }
        }
        return true;
    }

    // Return whether the game is finished
    public boolean isGameFinished() {
        return isGameFinished;
    }

    // Get the value of a tile at a specific position
    public int getTileValue(int row, int col) {
        return tiles[row][col];
    }

    // Get the grid size of the puzzle
    public int getGridSize() {
        return gridSize;
    }

    // Convert the game state to a JSON string
    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("gridSize", gridSize);
            jsonObject.put("emptyRow", emptyRow);
            jsonObject.put("emptyCol", emptyCol);
            jsonObject.put("isGameFinished", isGameFinished);

            // Store board state as a JSON array
            JSONArray boardArray = new JSONArray();
            for (int i = 0; i < gridSize; i++) {
                JSONArray rowArray = new JSONArray();
                for (int j = 0; j < gridSize; j++) {
                    rowArray.put(tiles[i][j]);
                }
                boardArray.put(rowArray);
            }
            jsonObject.put("board", boardArray);

            // Add metadata for future compatibility
            jsonObject.put("version", 1);
            jsonObject.put("timestamp", System.currentTimeMillis());

        } catch (JSONException e) {
            Log.e("PuzzleGame", "Failed to convert game state to JSON: " + e.getMessage());
            return "{}";  // Return an empty JSON object instead of null to avoid app crashes
        }
        return jsonObject.toString();
    }

    // Parcelable implementation to write to Parcel
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(gridSize);
        for (int i = 0; i < gridSize; i++) {
            dest.writeIntArray(tiles[i]);
        }
        dest.writeInt(emptyRow);
        dest.writeInt(emptyCol);
    }

    // Parcelable implementation
    @Override
    public int describeContents() {
        return 0;
    }
}
