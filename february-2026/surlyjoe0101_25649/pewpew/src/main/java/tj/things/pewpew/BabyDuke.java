package tj.things.pewpew;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.Objects;

public class BabyDuke {
  // Animation properties
  private static final int frameCount = 4;
  private static final Image[][] sprites = new Image[4][frameCount]; // [direction][frame]
  // Sprite dimensions (adjust based on your actual images)
  private static int spriteWidth;
  private static int spriteHeight;
  // Position
  private int x;
  private int y;
  private Direction direction;
  private int currentFrame;
  private int animationSpeed; // Update every N game ticks
  private int animationCounter;

  static {
    loadSprites();
  }

  /**
   * Constructor for BabyDuke
   *
   * @param x                  Initial x position
   * @param y                  Initial y position
   * @param direction          Initial direction
   */
  public BabyDuke(int x, int y, Direction direction) {
    this.x = x;
    this.y = y;
    this.direction = direction;
    this.currentFrame = 0;
    this.animationSpeed =  5; // Change frame every x updates
    this.animationCounter = 0;
  }

  /**
   * Load sprite images from resource files
   * <p>
   * images/babydukes/{dir}/{frame#}.png
   */
  private static void loadSprites() {
    String[] directionNames = {"down", "up", "left", "right"};

    try {
      for (int dir = 0; dir < 4; dir++) {
        for (int frame = 0; frame < frameCount; frame++) {
          String babby = "/images/babydukes/" + directionNames[dir] + "/" + (frame + 1) + ".png";
          sprites[dir][frame] =
              new Image(Objects.requireNonNull(BabyDuke.class.getResourceAsStream(babby)));
        }
      }
      spriteWidth = (int) sprites[0][0].getWidth();
      spriteHeight = (int) sprites[0][0].getHeight();
    } catch (Exception e) {
      System.err.println("Error loading sprites: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Update method - call this every game tick
   * Updates animation frame and moves toward target
   */
  public void update(int motherX, int motherY, int limitY) {
    animationCounter++;
    if (animationCounter >= animationSpeed) {
      moveToward(motherX, motherY, limitY);
      currentFrame = (currentFrame + 1) % frameCount;
      animationCounter = 0;
    }
  }

  /**
   * Move one step toward the target (mother ship)
   */
  private void moveToward(int targetX, int targetY, int limitY) {
    int dx = targetX - x;
    int dy = targetY - y;

    // Determine primary direction to move
    if (Math.abs(dx) > Math.abs(dy)) {
      // Move horizontally
      if (dx > 0) {
        x++;
        direction = Direction.RIGHT;
      } else if (dx < 0) {
        x--;
        direction = Direction.LEFT;
      }
    } else {
      // Move vertically
      if (dy > 0) {
        y++;
        direction = Direction.DOWN;
      } else if (dy < 0) {
        // don't move above current terrain
        if (limitY < y) {
          y--;
          direction = Direction.UP;
        } else {
          direction = Direction.DOWN;
        }
      }
    }

    if (limitY >= y) {
      y++; 
    }
  }

  public boolean canBoardMotherShip(double x, double y){
    return Math.abs(x-this.x)<10 && Math.abs(y-this.y)<10;
  }

  /**
   * Draw the sprite on the canvas
   */
  public void draw(GraphicsContext gc) {
    Image currentSprite = getCurrentSprite();
    if (currentSprite != null) {
      gc.drawImage(currentSprite, x, y);
    }
  }

  /**
   * Get the current sprite image based on direction and animation frame
   */
  public Image getCurrentSprite() {
    return sprites[direction.ordinal()][currentFrame];
  }


  // Getters and Setters
  public int getX() {return x;}

  public int getY() {return y;}


  // Direction enum
  public enum Direction {
    UP, DOWN, LEFT, RIGHT
  }
}