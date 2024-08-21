package com.example.fifteenpuzzlegame;

public class Tile {

    private int number, x ,y;

    public Tile() {
        number = 0;
    }

    public Tile(int n) {
        number = n;
    }

    public int getNumber() {
        return number;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setNumber(int n) {
        number = n;
    }

    public void setCoordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }
//	public Point getCoordinates(){
//		return new Point(coordinates);
//	}

}
