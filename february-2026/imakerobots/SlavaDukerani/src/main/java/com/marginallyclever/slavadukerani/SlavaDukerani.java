package com.marginallyclever.slavadukerani;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/// Controls one "round" of the game, including the game state, rendering, and user input.
///
/// Can be embedded in a larger UX or used standalone.
public class SlavaDukerani extends JPanel {
    public static void main( String[] args ) {
        // open a centered 800x600 window with the title "Slava Dukerani"
        var app = new SlavaDukerani(20,10, 30,(int)(Math.random()*1000000));

        JFrame frame = new JFrame("SlavaDukerani");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(app);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private final Grid grid;
    private GridTile hoverOver;  // the cursor is over this tile.
    private final EventListenerList listenerList = new EventListenerList();

    private BufferedImage playerImage,
            sensorImage;

    private int px = 0, py = 0;  // player position
    private int sx = 1, sy = 1;  // sensor position
    private boolean gameOver = false;
    private boolean youWon = false;
    private boolean initialized = false;
    private final int sensorRange = 2; // range of the sensor


    /// Construct a new SlavaDukerani game with the specified grid size, seed, and number of mines.
    /// @param gridWidth  Width of the grid in tiles.
    /// @param gridHeight Height of the grid in tiles.
    /// @param seed       Seed for the random number generator.
    /// @param numMines   Number of mines to place on the grid.
    public SlavaDukerani(int gridWidth, int gridHeight,int seed,int numMines) {
        super(new BorderLayout(5, 5));
        grid = new Grid(gridWidth, gridHeight, seed, numMines);
        getReady();
    }

    public SlavaDukerani(String gridString) {
        super(new BorderLayout(5, 5));
        grid = new Grid(gridString);
        getReady();
    }

    private void getReady() {
        setSize(grid.getGridWidth() * GridTile.SIZE_X, grid.getGridHeight() * GridTile.SIZE_Y);
        setMinimumSize  (new Dimension(grid.getGridWidth() * GridTile.SIZE_X, grid.getGridHeight() * GridTile.SIZE_Y));
        setPreferredSize(new Dimension(grid.getGridWidth() * GridTile.SIZE_X, grid.getGridHeight() * GridTile.SIZE_Y));
        setMaximumSize  (new Dimension(grid.getGridWidth() * GridTile.SIZE_X, grid.getGridHeight() * GridTile.SIZE_Y));

        attachMouseListeners();
        attachKeyboardListeners();
    }

    void onFirstFrame() throws Exception {
        System.out.println("Initializing game...");
        //initToolBar();
        initGame();
        initArt();
        System.out.println("Ready...");
    }

    private void attachKeyboardListeners() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(gameOver || !initialized) return;
                int dx=0,dy=0;
                switch(e.getKeyCode()) {
                    case KeyEvent.VK_W, KeyEvent.VK_UP   :  dy=-1;  break;
                    case KeyEvent.VK_S, KeyEvent.VK_DOWN :  dy= 1;  break;
                    case KeyEvent.VK_A, KeyEvent.VK_LEFT :  dx=-1;  break;
                    case KeyEvent.VK_D, KeyEvent.VK_RIGHT:  dx= 1;  break;
                    // key events for users with one-button mice or who prefer keyboard controls.
                    case KeyEvent.VK_Q                   :  clickEvent(true,false);  return; // left click
                    case KeyEvent.VK_E                   :  clickEvent(false,true);  return; // right click
                    default: return; // ignore other keys
                }
                movePlayer(dx, dy);
            }
        });
    }

    private void movePlayer(int dx, int dy) {
        int x = px+dx;
        int y = py+dy;
        // check bounds
        if (x<0 || x>= grid.getGridWidth() || y<0 || y>= grid.getGridHeight()) return;
        // check for mine
        var tile = grid.getTile(x,y);

        // walking into an unknown tile reveals that tile.
        if(tile.hidden) {
            if(grid.revealTile(tile.x,tile.y)) {
                fireGameOver(false);
            }
        }

        if(tile.type == GridTile.TYPE_MINE) {
            // you died, game over
            System.out.println("You died.  Game over!");
            fireGameOver(false);
        } else if(tile.type == GridTile.TYPE_EXIT) {
            // you win!
            System.out.println("You win!");
            fireGameOver(true);
        }
        // if pushing into the sensor, try to push the sensor
        if(sx==x && sy==y) {
            int bx2 = sx +dx;
            int by2 = sy +dy;
            if (bx2<0 || bx2>= grid.getGridWidth() || by2<0 || by2>= grid.getGridHeight()) return; // box out of bounds
            var tile2 = grid.getTile(bx2,by2);
            if(tile2.type == GridTile.TYPE_MINE) {
                // equipment destroyed, game over.
                System.out.println("Equipment destroyed.  Game over!");
                fireGameOver(false);
            }
            if(tile2.type == GridTile.TYPE_EXIT) {
                // box pushed onto exit, you win!
                System.out.println("You win!");
                fireGameOver(true);
            }
            // move sensor
            sx = bx2;
            sy = by2;
        }
        px = x;
        py = y;
        repaint();
    }

    private void attachMouseListeners() {
        addMouseListener(new MouseAdapter() {
            boolean leftDown = false;
            boolean rightDown = false;

            @Override
            public void mousePressed(MouseEvent e) {
                boolean isLeft = SwingUtilities.isLeftMouseButton(e);
                boolean isRight = SwingUtilities.isRightMouseButton(e);
                if(isLeft) leftDown=true;
                if(isRight) rightDown=true;
                //System.out.println("Mouse pressed at: " + e.getX() + "," + e.getY()+"  Left: "+leftDown+"  Right: "+rightDown);
                clickEvent(leftDown,rightDown);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                boolean isLeft = SwingUtilities.isLeftMouseButton(e);
                boolean isRight = SwingUtilities.isRightMouseButton(e);
                if(isLeft) leftDown=false;
                if(isRight) rightDown=false;
                super.mouseReleased(e);
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoverOver = null;
                if(gameOver || !initialized) return;
                // get tile at cursor position.
                int x = e.getX()/(getWidth()/ grid.getGridWidth());
                int y = e.getY()/(getHeight()/ grid.getGridHeight());
                if (x<0 || x>= grid.getGridWidth() ||
                    y<0 || y>= grid.getGridHeight()) return;
                // show sensor value in title bar
                hoverOver = grid.getTile(x,y);
                repaint();
            }
        });
    }

    private void clickEvent(boolean isLeft, boolean isRight) {
        if(gameOver || !initialized) return;
        // get tile at cursor position.
        if (hoverOver==null) return;
        int mouseX = hoverOver.x;
        int mouseY = hoverOver.y;
        GridTile tile = grid.getTile(mouseX,mouseY);

        // right click
        if(isRight) {
            // on hidden tile to flag/unflag it.
            if (tile.hidden) {
                tile.flagged = !tile.flagged;
                fireFlagChanged();
                repaint();
            }
        }

        // left click
        if(isLeft) {
            // on a hidden tile with no flag to reveal it.
            if (tile.hidden) {
                if (!tile.flagged) {
                    if(grid.revealTile(mouseX, mouseY)) {
                        fireGameOver(false);
                    }
                }
            } else {
                // on a revealed tile to move player there if adjacent.
                if ((tile.x == px && Math.abs(tile.y - py) == 1) ||
                    (tile.y == py && Math.abs(tile.x - px) == 1)) {
                    movePlayer(tile.x - px, tile.y - py);
                }
            }
            repaint();
        }

        if(isLeft && isRight) {
            // "chording", aka the double-click technique where you click both buttons on a revealed tile
            // to reveal all adjacent hidden tiles if the number of adjacent flags equals the sensor value.
            doChord(tile);
        }
    }

    private void doChord(GridTile tile) {
        if(!tile.hidden && tile.sensorValue > 0) {
            int adjacentFlags = 0;
            List<GridTile> adjacentHidden = new ArrayList<>();
            for(int dx=-1; dx<=1; dx++) {
                for(int dy=-1; dy<=1; dy++) {
                    if(dx==0 && dy==0) continue;
                    int ax = tile.x + dx;
                    int ay = tile.y + dy;
                    if (ax<0 || ax>= grid.getGridWidth() || ay<0 || ay>= grid.getGridHeight()) continue;
                    var t = grid.getTile(ax,ay);
                    if(t.flagged) adjacentFlags++;
                    if(t.hidden) adjacentHidden.add(t);
                }
            }
            if(adjacentFlags == tile.sensorValue) {
                boolean failed = false;
                for(var t : adjacentHidden) {
                    if(!t.flagged) {
                        failed |= grid.revealTile(t.x, t.y);
                    }
                }
                if(failed) {
                    fireGameOver(false);
                }
            }
        }
    }

    public void addFlagChangeListener(FlagChangeListener listener) {
        listenerList.add(FlagChangeListener.class, listener);
    }
    public void removeFlagChangeListener(FlagChangeListener listener) {
        listenerList.remove(FlagChangeListener.class, listener);
    }

    private void fireFlagChanged() {
        int numFlags = 0;
        for (int x = 0; x< grid.getGridWidth(); x++) {
            for (int y = 0; y< grid.getGridHeight(); y++) {
                if(grid.getTile(x,y).flagged) numFlags++;
            }
        }

        for (FlagChangeListener listener : listenerList.getListeners(FlagChangeListener.class)) {

            listener.flagCountChanged(numFlags);
        }
    }

    public void addGameOverListener(GameOverListener listener) {
        listenerList.add(GameOverListener.class, listener);
    }
    public void removeGameOverListener(GameOverListener listener) {
        listenerList.remove(GameOverListener.class, listener);
    }

    private void fireGameOver(boolean won) {
        gameOver = true;
        youWon = won;
        for (GameOverListener listener : listenerList.getListeners(GameOverListener.class)) {
            listener.gameOver(won);
        }
    }

    private void initGame() {
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!initialized) {
            try {
                onFirstFrame();
            } catch(Exception e) {
                e.printStackTrace();
                java.lang.System.exit(1);
            }
            initialized=true;
        }

        grid.paintComponent(g,gameOver);

        PanelHelper.drawImage(g, playerImage, px, py, Color.BLUE);
        PanelHelper.drawImage(g, sensorImage, sx, sy,Color.ORANGE);
        highlightHoverOver(g);
        drawSensorRange(g);

        if(gameOver) {
            // draw game over text
            String text = youWon ? "You Win!" : "Game Over";
            Font oldFont = g.getFont();
            Font bold32 = oldFont.deriveFont(Font.BOLD, 64f);
            g.setFont(bold32);
            FontMetrics fm = g.getFontMetrics(bold32);
            int sx = (getWidth() - fm.stringWidth(text)) / 2;
            int sy = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g.setColor(youWon ? Color.BLUE : Color.RED);
            g.drawString(text, sx, sy);
            g.setFont(oldFont);
        }
    }

    // illustrate sensor range
    private void drawSensorRange(Graphics g) {
        g.setColor(new Color(255, 165, 0, 32)); // semi-transparent orange
        int sensorDrawX = (sx -sensorRange)* GridTile.SIZE_X;
        int sensorDrawY = (sy -sensorRange)* GridTile.SIZE_Y;
        int sensorDrawSizeX = (sensorRange*2+1)* GridTile.SIZE_X;
        int sensorDrawSizeY = (sensorRange*2+1)* GridTile.SIZE_Y;
        g.fillRect(sensorDrawX, sensorDrawY, sensorDrawSizeX, sensorDrawSizeY);

        for(int x=0; x< grid.getGridWidth(); x++) {
            for(int y=0; y< grid.getGridHeight(); y++) {
                var tile = grid.getTile(x,y);
                if (tile.sensorValue > 0 && !tile.hidden) {
                    // if bx/by is within sensorRange of this tile, show sensor value
                    if(Math.abs(tile.x- sx) <= sensorRange && Math.abs(tile.y- sy) <= sensorRange) {
                        var drawX = tile.x * GridTile.SIZE_X;
                        var drawY = tile.y * GridTile.SIZE_Y;
                        drawSensorValue(g,drawX,drawY,tile.sensorValue);
                    }
                }
            }
        }
    }

    // highlight if hover over
    private void highlightHoverOver(Graphics g) {
        if(hoverOver==null) return;

        int drawX = hoverOver.x* GridTile.SIZE_X;
        int drawY = hoverOver.y* GridTile.SIZE_Y;
        // if hoverOver is cardinal with and adjacent to the player, highlight in green, otherwise yellow.
        if((hoverOver.x==px && Math.abs(hoverOver.y-py)==1) ||
           (hoverOver.y==py && Math.abs(hoverOver.x-px)==1)) {
            g.setColor(Color.GREEN);
        } else {
            g.setColor(Color.YELLOW);
        }
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(drawX, drawY, GridTile.SIZE_X, GridTile.SIZE_Y);
    }

    private void drawSensorValue(Graphics g, int drawX, int drawY, int sensorValue) {
        // set bold 16px font and center the text using FontMetrics
        Font oldFont = g.getFont();
        Font bold16 = oldFont.deriveFont(Font.BOLD, 16f);
        g.setFont(bold16);
        FontMetrics fm = g.getFontMetrics(bold16);
        String text = Integer.toString(sensorValue);
        int sx = drawX + (GridTile.SIZE_X - fm.stringWidth(text)) / 2;
        int sy = drawY + (GridTile.SIZE_Y - fm.getHeight()) / 2 + fm.getAscent();
        g.setColor(Color.BLACK);
        g.drawString(text, sx, sy);
        g.setFont(oldFont);
    }

    // load artwork from resources.  Called once on the first frame.
    private void initArt() throws IOException {
        // load all Duke images from the "dukes" folder in resources and pick one at random for the player image.
        try {
            String packagePath = getClass().getPackage().getName().replace('.', '/');
            List<String> pngs = listPngResources(packagePath+"/dukes"); // folder inside src/main/resources
            if (pngs.isEmpty()) {
                System.err.println("No PNGs found.");
                return;
            }
            String chosen = pngs.get(new Random().nextInt(pngs.size()));
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(chosen)) {
                if (is != null) {
                    playerImage = ImageIO.read(is);
                    System.out.println("Loaded art: " + chosen);
                } else {
                    System.err.println("Failed to open resource: " + chosen);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        sensorImage = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("sensor.png" )));
    }

    // helper to list pngs from a resource folder (handles both file and jar)
    private List<String> listPngResources(String resourceFolder) throws IOException, URISyntaxException {
        List<String> result = new ArrayList<>();
        ClassLoader cl = getClass().getClassLoader();
        URL dirURL = cl.getResource(resourceFolder);
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
            File folder = new File(dirURL.toURI());
            File[] files = folder.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
            if (files != null) {
                for (File f : files) result.add(resourceFolder + "/" + f.getName());
            }
        } else if (dirURL != null && dirURL.getProtocol().equals("jar")) {
            String dirPath = dirURL.getPath();
            String jarPath = dirPath.substring(5, dirPath.indexOf("!")); // strip "file:" and everything after "!"
            try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8))) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    String name = entries.nextElement().getName();
                    if (name.startsWith(resourceFolder + "/") && name.toLowerCase().endsWith(".png")) {
                        result.add(name);
                    }
                }
            }
        } else {
            // fallback: attempt scanning classpath URLs (may not list inside JARs)
            Enumeration<URL> resources = cl.getResources(resourceFolder);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if ("file".equals(url.getProtocol())) {
                    File folder = new File(url.toURI());
                    File[] files = folder.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
                    if (files != null) for (File f : files) result.add(resourceFolder + "/" + f.getName());
                }
            }
        }
        return result;
    }
}
