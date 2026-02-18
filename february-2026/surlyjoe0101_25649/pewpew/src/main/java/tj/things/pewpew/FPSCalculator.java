package tj.things.pewpew;

/**
 * FPS Calculator for use in render loops.
 * Computes frames per second using a moving average approach.
 */
public class FPSCalculator {
  private long lastFrameTime;
  private double currentFPS;
  private int frameCount;
  private long fpsUpdateTime;
  private static final long FPS_UPDATE_INTERVAL = 1_000_000_000; // 1 second in nanoseconds

  public FPSCalculator() {
    this.lastFrameTime = System.nanoTime();
    this.fpsUpdateTime = this.lastFrameTime;
    this.currentFPS = 0.0;
    this.frameCount = 0;
  }

  /**
   * Call this method once per frame in your render loop.
   * @return the current FPS value
   */
  public double update() {
    long currentTime = System.nanoTime();
    frameCount++;

    // Calculate FPS every second
    long elapsedSinceUpdate = currentTime - fpsUpdateTime;
    if (elapsedSinceUpdate >= FPS_UPDATE_INTERVAL) {
      currentFPS = (frameCount * 1_000_000_000.0) / elapsedSinceUpdate;
      frameCount = 0;
      fpsUpdateTime = currentTime;
    }

    lastFrameTime = currentTime;
    return currentFPS;
  }

  /**
   * Get the current FPS without updating.
   * @return the most recent FPS calculation
   */
  public double getFPS() {
    return currentFPS;
  }

  /**
   * Get the time delta since the last frame in seconds.
   * Useful for frame-rate independent movement.
   * @return delta time in seconds
   */
  public double getDeltaTime() {
    long currentTime = System.nanoTime();
    return (currentTime - lastFrameTime) / 1_000_000_000.0;
  }

  /**
   * Reset the FPS calculator.
   */
  public void reset() {
    this.lastFrameTime = System.nanoTime();
    this.fpsUpdateTime = this.lastFrameTime;
    this.currentFPS = 0.0;
    this.frameCount = 0;
  }
}