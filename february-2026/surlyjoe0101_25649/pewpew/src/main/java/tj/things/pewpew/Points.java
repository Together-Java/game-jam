package tj.things.pewpew;

public class Points {
  int x, y;
  long startTime;
  int points;

  Points(int x, int y, long startTime, int points) {
    this.x = x;
    this.y = y;
    this.startTime = startTime;
    this.points = points;
  }

  void update() {
    // drift vertically upward
    this.y--;
  }
}
