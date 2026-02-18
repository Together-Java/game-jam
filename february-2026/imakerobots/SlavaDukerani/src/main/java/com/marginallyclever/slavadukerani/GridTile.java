package com.marginallyclever.slavadukerani;

/// A single tile of the game board. Contains the tile's coordinates, type (empty, mine, or exit), sensor value, and
/// whether it's hidden or flagged.
public class GridTile {
    static final int SIZE_X = 32;
    static final int SIZE_Y = 32;

    public static final int TYPE_EMPTY = 0;
    public static final int TYPE_MINE = 1;
    public static final int TYPE_EXIT = 2;
    public static final int TYPE_RESERVED = 3;

    public int x, y;
    public int type = TYPE_EMPTY;
    public int sensorValue = 0;
    public boolean hidden = true;
    public boolean flagged = false;

    public GridTile(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
