package com.marginallyclever.slavadukerani;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;

public class Grid {
    private int gridWidth = 20;
    private int gridHeight = 10;  // grid width and height
    private GridTile [][] tiles;

    private final Random rand;
    private int numMines = 0;

    private BufferedImage
            flagImage,
            mineImage,
            exitImage,
            emptyImage,
            hiddenImage;

    /// Construct a new Grid game with the specified grid size, seed, and number of mines.
    /// @param gridWidth  Width of the grid in tiles.
    /// @param gridHeight Height of the grid in tiles.
    /// @param seed       Seed for the random number generator.
    /// @param numMines   Number of mines to place on the grid.
    public Grid(int gridWidth, int gridHeight,int seed,int numMines) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.numMines = numMines;

        rand = new Random(seed);

        initGrid();
    }

    /// input is a string of 0s and 1s, where 1 represents a mine and 0 represents an empty tile.
    /// The string is read row by row, starting from the top-left corner of the grid.  every row is terminated with a \n
    public Grid(String input) {
        String[] rows = input.split("\n");
        this.gridHeight = rows.length;
        this.gridWidth = rows[0].length();
        this.tiles = new GridTile[gridWidth][gridHeight];
        this.numMines = 0;

        rand = new Random();
        initGrid();

        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                char c = rows[y].charAt(x);
                var tile = new GridTile(x,y);
                tiles[x][y] = tile;
                tile.type = (c == '1') ? GridTile.TYPE_MINE : GridTile.TYPE_EMPTY;
                if(c=='1') numMines++;
            }
        }
    }

    private void initGrid() {
        loadArt();

        // allocate empty grid
        tiles = new GridTile[gridWidth][gridHeight];
        for (int x = 0; x< gridWidth; x++) {
            for (int y = 0; y< gridHeight; y++) {
                tiles[x][y] = new GridTile(x,y);
            }
        }
        tiles[gridWidth-1][gridHeight-1].type = 2; // exit

        // temp fill so first click isn't a mine, will be cleared later
        tiles[0][0].type = GridTile.TYPE_RESERVED;
        tiles[1][0].type = GridTile.TYPE_RESERVED;
        tiles[0][1].type = GridTile.TYPE_RESERVED;
        tiles[1][1].type = GridTile.TYPE_RESERVED;

        // add some mines
        placeMines();

        tiles[0][0].type = GridTile.TYPE_EMPTY; // clear
        tiles[0][1].type = GridTile.TYPE_EMPTY; // clear
        tiles[1][0].type = GridTile.TYPE_EMPTY; // clear
        tiles[1][1].type = GridTile.TYPE_EMPTY; // clear

        calculateSensorValues();
        revealTile(0,0);
        revealTile(1,1);
    }

    private void loadArt() {
        try {
            flagImage = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("flag-32.png")));
            mineImage = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("mine.png")));
            exitImage = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("exit-32.png")));
            hiddenImage = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("hidden.png")));
        } catch(Exception e) {
            System.out.println("Error loading images: " + e.getMessage());
        }
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public int getNumMines() {
        return numMines;
    }

    public GridTile getTile(int x, int y) {
        return tiles[x][y];
    }

    /// Calculate the sensor values for all tiles based on the current mine placement.  Called after placing mines.
    private void calculateSensorValues() {
        for (int x = 0; x< getGridWidth(); x++) {
            for (int y = 0; y< getGridHeight(); y++) {
                if(getTile(x,y).type == 1) {
                    updateAdjacentSensorValues(x,y);
                }
            }
        }
    }

    /// Raise the sensor value of all tiles adjacent to the given coordinates.  Called when placing mines.
    private void updateAdjacentSensorValues(int x, int y) {
        for (int dx=-1;dx<=1;dx++) {
            for (int dy=-1;dy<=1;dy++) {
                if(dx==0 && dy==0) continue;
                int nx = x+dx;
                int ny = y+dy;
                if (nx>=0 && nx< getGridWidth() && ny>=0 && ny< getGridHeight()) {
                    tiles[nx][ny].sensorValue++;
                }
            }
        }
    }

    private void placeMines() {
        int placed=0;
        while(placed<numMines) {
            int x = (int)(rand.nextDouble() * getGridWidth());
            int y = (int)(rand.nextDouble() * getGridHeight());
            if(getTile(x,y).type==0) {
                getTile(x,y).type = 1;
                placed++;
            }
        }
    }

    ///  returns true if a mine is revealed.
    public boolean revealTile(int x, int y) {
        GridTile tile = getTile(x,y);
        tile.hidden = false;
        if (tile.type == 1) {
            System.out.println("Poked a mine.  Game over!");
            return true;
        }

        if (tile.sensorValue == 0) {
            revealAdjacentTiles(tile);
        }

        return false;
    }

    /// Recursively visit all adjacent hidden tiles with sensorValue=0 and reveal them until reaching tiles with
    /// sensorValue>0.  Called when clearing a tile with sensorValue=0.
    private void revealAdjacentTiles(GridTile startTile) {
        // use a queue to avoid stack overflow from recursion, and a visited set to avoid infinite loops.
        Queue<GridTile> toVisit = new LinkedList<>();
        boolean [][] visited = new boolean [getGridWidth()][getGridHeight()];
        toVisit.add(startTile);

        while(!toVisit.isEmpty()) {
            var tile = toVisit.poll();
            visited[startTile.x][startTile.y] = true;
            tile.hidden = false;
            if (tile.sensorValue > 0) continue;

            // queue new adjacent hidden unflagged tiles.
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int nx = tile.x + dx;
                    int ny = tile.y + dy;
                    if (nx >= 0 && nx < getGridWidth() && ny >= 0 && ny < getGridHeight()) {
                        GridTile adjacentTile = getTile(nx,ny);
                        // seen or in queue already, skip
                        if(visited[adjacentTile.x][adjacentTile.y] || toVisit.contains(adjacentTile)) continue;
                        // not hidden or flagged, skip
                        if(!adjacentTile.hidden || adjacentTile.flagged) continue;
                        // do it!
                        toVisit.add(adjacentTile);
                    }
                }
            }
        }
    }

    public void paintComponent(Graphics g,boolean showAll) {
        // draw all tiles
        for(int x = 0; x< getGridWidth(); ++x) {
            for(int y = 0; y< getGridHeight(); ++y) {
                drawOneTile(g,x,y,showAll);
            }
        }
    }

    private void drawOneTile(Graphics g, int x, int y,boolean showAll) {
        GridTile tile = getTile(x,y);
        int drawX = x * GridTile.SIZE_X;
        int drawY = y * GridTile.SIZE_Y;

        // draw hidden tile
        if(tile.hidden) {
            PanelHelper.drawImage(g,hiddenImage,x,y,Color.GRAY);
            if(showAll) {
                // if game over, show mines
                if(tile.type==1) {
                    PanelHelper.drawImage(g, mineImage,x,y,Color.BLACK);
                }
            } else if(tile.flagged) {
                PanelHelper.drawImage(g, flagImage,x,y,Color.WHITE);
            }
        } else {
            // draw revealed tile
            switch (tile.type) {
                case GridTile.TYPE_EMPTY:
                    PanelHelper.drawImage(g,emptyImage,x,y,Color.WHITE);
                    // draw tile border
                    g.setColor(Color.DARK_GRAY);
                    g.drawRect(drawX, drawY, GridTile.SIZE_X, GridTile.SIZE_Y);
                    break;
                case GridTile.TYPE_MINE:
                    PanelHelper.drawImage(g, mineImage,x,y,Color.BLACK);
                    break;
                case GridTile.TYPE_EXIT:
                    PanelHelper.drawImage(g, exitImage,x,y,Color.GREEN);
                    break;
            }
        }
    }
}
