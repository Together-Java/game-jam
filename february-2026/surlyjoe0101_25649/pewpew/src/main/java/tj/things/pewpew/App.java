package tj.things.pewpew;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.function.Predicate.not;

/**
 * Pew Pew - A Defender-style side-scrolling shooter featuring Java Duke!
 * Controls: WASD to move, SPACE to fire laser pulses.
 */
public class App extends Application {

    // --- Window / world constants ---
    private static final int WIDTH = 1024;
    private static final int HEIGHT = 600;
    private static final double WORLD_WIDTH = 6000;

    // --- Duke (player ship) constants ---
    private static final double DUKE_THRUST = 0.4;
    private static final double DUKE_FRICTION = 0.92;
    private static final double DUKE_MAX_SPEED = 5.0;

    // --- Duke screen-position targets (fraction of screen width) ---
    private static final double DUKE_TARGET_X_RIGHT = 0.30;  // left side when facing right
    private static final double DUKE_TARGET_X_LEFT  = 0.55;  // right side when facing left
    private static final double DUKE_SLIDE_SPEED    = 0.02;  // how fast the target interpolates (0→1)

    // --- Duke sprite size ---
    private static final double DUKE_DRAW_WIDTH = 51;
    private static final double DUKE_DRAW_HEIGHT = 42;

    // --- Projectile constants ---
    private static final double LASER_SPEED = 12.0;
    private static final long FIRE_COOLDOWN_NS = 150_000_000L;

    // --- Enemy constants ---
    private static final long SPAWN_INTERVAL_NS = 1_200_000_000L;
    private static final double ENEMY_SPEED = 2.5;

    // --- Terrain ---
    private static final int TERRAIN_SEGMENTS = 300;

    // --- Point Feedback
    private static final long POINTS_LINGER_TIME = 1_000_000_000L;

    // debug
    final boolean debug = false;

    // --- State ---
    private final Set<KeyCode> keysPressed = new HashSet<>();
    private double dukeScreenX;
    private double dukeScreenY;
    private double dukeVX = 0;
    private double dukeVY = 0;
    private boolean facingRight = true;
    private boolean isLanding = false;
    private double dukeAnchorT = 0.0; // interpolation factor: 0 = facing-right anchor, 1 = facing-left anchor
    private double worldScrollOffset = 0;
    private long lastFireTime = 0;
    private long lastSpawnTime = 0;
    private int score = 0;
    private int lives = 3;
    private boolean gameOver = false;
    private long invincibleUntil = 0;
    private int numBabyDukes = 10;

    private int updates = 0;
    private int updateRate = 2;

    private final List<Laser> lasers = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private final double[] terrainHeights = new double[TERRAIN_SEGMENTS + 1];
    private final List<BabyDuke> babyDukes = new ArrayList<>();
    private final List<Points> points = new ArrayList<>(); // textual points display

    private final Random random = new Random();

    private boolean showStats = false;
    private final FPSCalculator fpsCalculator = new FPSCalculator();

    // --- Duke sprite ---
    private Image dukeImage;
    private Image dukeLeft;
    private Image dukeRight;

    // --- Audio ---
    private AudioClip laserSound, boomBomber, boomBaiter, boomLander;
    private MediaPlayer bckgndMusic;

    @Override
    public void start(Stage stage) {
        dukeImage = new Image(
            Objects.requireNonNull(getClass().getResourceAsStream("/images/dukeSmall.png")));
        dukeLeft = new Image(
            Objects.requireNonNull(getClass().getResourceAsStream("/images/dukeSmallLeft.png")));
        dukeRight = new Image(
            Objects.requireNonNull(getClass().getResourceAsStream("/images/dukeSmallRight.png")));

        laserSound = new AudioClip(
            Objects.requireNonNull(getClass().getResource("/audio/laser.wav")).toString());

        boomBomber = new AudioClip(
            Objects.requireNonNull(getClass().getResource("/audio/boomBomber.wav")).toString());
        boomBaiter = new AudioClip(
            Objects.requireNonNull(getClass().getResource("/audio/boomBaiter.wav")).toString());
        boomLander = new AudioClip(
            Objects.requireNonNull(getClass().getResource("/audio/boomLander.wav")).toString());

//        bckgndMusic = new MediaPlayer(new Media(
//            Objects.requireNonNull(getClass().getResource("/audio/epl.wav")).toString()));

        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, WIDTH, HEIGHT);


        scene.setOnKeyTyped( this::adjustGameSettings );
        scene.setOnKeyPressed(e -> keysPressed.add(e.getCode()));
        scene.setOnKeyReleased(e -> keysPressed.remove(e.getCode()));

        dukeScreenX = WIDTH * DUKE_TARGET_X_RIGHT - DUKE_DRAW_WIDTH/2.0;
        dukeScreenY = HEIGHT * 0.5 - DUKE_DRAW_HEIGHT/2.0;

        generateTerrain();
        generateBabyDukes();

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
              if( updates % updateRate == 0){
                update(now);
              }
              render(gc, now);
              updates++;
            }
        };
        timer.start();

//        bckgndMusic.setAutoPlay(true);
//        bckgndMusic.play();

        stage.setTitle("Pew Pew — Duke Defender");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public void adjustGameSettings(KeyEvent e){
      switch(e.getCharacter()){
        case "+" -> updateRate = Math.clamp(updateRate +1, 1, 5);
        case "-" -> updateRate = Math.clamp(updateRate -1, 1, 5);
        case "~" -> showStats = !showStats;
      }
    }

    // ========================================================================
    //  TERRAIN GENERATION
    // ========================================================================

    private void generateTerrain() {
        for (int i = 0; i <= TERRAIN_SEGMENTS; i++) {
            terrainHeights[i] = HEIGHT - 40 - random.nextDouble() * 80;
        }
        int smoothing = 2;// 0 = jagged mountains, higher goes to flat plains.
        for (int pass = 0; pass < smoothing ; pass++) {
            for (int i = 1; i < TERRAIN_SEGMENTS; i++) {
                terrainHeights[i] = (terrainHeights[i - 1] + terrainHeights[i] + terrainHeights[i + 1]) / 3.0;
            }
        }
    }

    private double getTerrainHeightAt(double x) {
        double segW = WORLD_WIDTH / TERRAIN_SEGMENTS;
        double segmentIndex = x / segW;

        if (segmentIndex < 0) return terrainHeights[0];
        if (segmentIndex >= TERRAIN_SEGMENTS) return terrainHeights[TERRAIN_SEGMENTS];

        int leftIndex = (int) segmentIndex;
        int rightIndex = leftIndex + 1;
        double t = segmentIndex - leftIndex;
        double y = terrainHeights[leftIndex] * (1 - t) + terrainHeights[rightIndex] * t;
        return y ;
    }

    // ========================================================================
    //  BABY DUKE GENERATION
    // ========================================================================

    private void generateBabyDukes(){
      for (int i = 0; i < numBabyDukes; i++) {
          int x = random.nextInt((int) (WORLD_WIDTH-200))+100;
          int y = random.nextInt((int) getTerrainHeightAt(x), HEIGHT);
          babyDukes.add(new BabyDuke(x,y, BabyDuke.Direction.DOWN ));
      }
    }

    // ========================================================================
    //  UPDATE
    // ========================================================================

    private void update(long now) {
        if (gameOver) {
            if (keysPressed.contains(KeyCode.R)) {
                restartGame();
            }
            return;
        }

        // --- Thrust / inertia movement (WASD) ---
        double ax = 0, ay = 0;
        if (keysPressed.contains(KeyCode.D) && !isLanding) ax += DUKE_THRUST;
        if (keysPressed.contains(KeyCode.A) && !isLanding) ax -= DUKE_THRUST;
        if (keysPressed.contains(KeyCode.W)) ay -= DUKE_THRUST;
        if (keysPressed.contains(KeyCode.S)) ay += DUKE_THRUST;

        // Update facing direction based on horizontal input
        if (keysPressed.contains(KeyCode.D) && !keysPressed.contains(KeyCode.A)) {
          facingRight = true;
        } else if (keysPressed.contains(KeyCode.A) && !keysPressed.contains(KeyCode.D)) {
          facingRight = false;
        } 

        // --- Gradually interpolate the anchor position ---
        double targetT = facingRight ? 0.0 : 1.0;
        dukeAnchorT += (targetT - dukeAnchorT) * DUKE_SLIDE_SPEED;
        // Snap when very close to avoid perpetual drift
        if (Math.abs(dukeAnchorT - targetT) < 0.001) dukeAnchorT = targetT;

        // Current anchor X on screen (lerp between the two positions)
        double anchorX = WIDTH * (DUKE_TARGET_X_RIGHT + dukeAnchorT * (DUKE_TARGET_X_LEFT - DUKE_TARGET_X_RIGHT));

        dukeVX += ax;
        dukeVY += ay;

        dukeVX = Math.max(-DUKE_MAX_SPEED, Math.min(DUKE_MAX_SPEED, dukeVX));
        dukeVY = Math.max(-DUKE_MAX_SPEED, Math.min(DUKE_MAX_SPEED, dukeVY));

        dukeVX *= DUKE_FRICTION;
        dukeVY *= DUKE_FRICTION;

        if (Math.abs(dukeVX) < 0.05) dukeVX = 0;
        if (Math.abs(dukeVY) < 0.05) dukeVY = 0;

        // Vertical clamping
        dukeScreenY += dukeVY;
        dukeScreenY = Math.clamp(dukeScreenY, 30, HEIGHT - 20);
        if(dukeScreenY > HEIGHT - 60) {
          // start landing procedure in the last 40 pixels
          isLanding = true;
          // start to kill thrust
           dukeVX = ( HEIGHT-20 - dukeScreenY)/40 * dukeVX;
        } else{
          isLanding = false;
        }

        // --- Horizontal: keep Duke near the anchor, scroll the world for overflow ---
        // The anchor slides across the screen when direction changes, carrying Duke along.
        // Duke can deviate a little from the anchor via thrust, but excess is fed into world scroll.
        double prevDukeScreenX = dukeScreenX;
        dukeScreenX += dukeVX;

        // Also slide Duke toward the moving anchor
        double anchorPull = (anchorX - dukeScreenX) * 0.08;
        dukeScreenX += anchorPull;
        // Compensate the anchor pull in world scroll so the world-position stays consistent
        worldScrollOffset -= anchorPull;

        // Clamp Duke to a band around the anchor (±80px wiggle room)
        double margin = 80;
        double minX = Math.max(30, anchorX - margin);
        double maxX = Math.min(WIDTH - 60, anchorX + margin);

        if (dukeScreenX > maxX) {
            double overflow = dukeScreenX - maxX;
            dukeScreenX = maxX;
            worldScrollOffset += overflow;
        }
        if (dukeScreenX < minX) {
            double underflow = minX - dukeScreenX;
            dukeScreenX = minX;
            worldScrollOffset -= underflow;
        }

        worldScrollOffset = Math.max(0, Math.min(worldScrollOffset, WORLD_WIDTH - WIDTH));

        // --- Firing (SPACE) ---
        if (keysPressed.contains(KeyCode.SPACE) && (now - lastFireTime) > FIRE_COOLDOWN_NS && !isLanding) {
            double laserX = facingRight
                    ? dukeScreenX + 40 + worldScrollOffset
                    : dukeScreenX - 10 + worldScrollOffset;
            lasers.add(new Laser(laserX, dukeScreenY , facingRight));
            laserSound.play();
            lastFireTime = now;
        }
                                                                      
        // --- Update lasers ---
        Iterator<Laser> litIter = lasers.iterator();
        while (litIter.hasNext()) {
            Laser l = litIter.next();
            l.x += l.movingRight ? LASER_SPEED : -LASER_SPEED;
            double screenX = l.x - worldScrollOffset;
            if (screenX > WIDTH + 50 || screenX < -50) {
                litIter.remove();
            }
        }

        if(!debug) {
          // --- Spawn enemies ---
          if (now - lastSpawnTime > SPAWN_INTERVAL_NS) {
            spawnEnemy();
            lastSpawnTime = now;
          }
        }

        // --- Update enemies ---
        Iterator<Enemy> eit = enemies.iterator();
        while (eit.hasNext()) {
            Enemy e = eit.next();
            e.update(dukeScreenX + worldScrollOffset, dukeScreenY);
//          if (e.x - worldScrollOffset < -100 || e.x - worldScrollOffset > WIDTH + 300) {
            if (e.x  < 0 || e.x > WORLD_WIDTH ) {
                eit.remove();
            }
        }

        // --- Update baby dukes ---
        babyDukes.forEach(baby ->
                              baby.update((int) (dukeScreenX+worldScrollOffset),
                                  (int) dukeScreenY,
                                  (int) getTerrainHeightAt(dukeScreenX+worldScrollOffset)));
        Iterator<BabyDuke> bdi = babyDukes.iterator();
        while(bdi.hasNext()){
          BabyDuke baby = bdi.next();
          baby.update((int) (dukeScreenX+worldScrollOffset),
              (int) dukeScreenY,
              (int) getTerrainHeightAt(dukeScreenX+worldScrollOffset));
          // todo: only allow boarding when fully landed.
          if(baby.canBoardMotherShip(dukeScreenX+worldScrollOffset, dukeScreenY)){
            points.add(new Points(baby.getX(),baby.getY(), now, 500));
            bdi.remove();
          }
        }

        // --- All baby dukes saved
        // todo: go to next level, maybe increase enemies, speed, dukes, etc.

        // --- Update Points Feedback
        Iterator<Points> pi = points.iterator();
        while(pi.hasNext()){
          Points point = pi.next();
          point.update();
          if(now - point.startTime > POINTS_LINGER_TIME){
            score += point.points;  // award points now
            pi.remove();
          }
        }

        // --- Collision: lasers vs enemies ---
        Iterator<Laser> li = lasers.iterator();
        while (li.hasNext()) {
            Laser l = li.next();
            Iterator<Enemy> ei = enemies.iterator();
            while (ei.hasNext()) {
                Enemy e = ei.next();
                if (e.hitTest(l.x, l.y)) {
                    e.hp--;
                    if (e.hp <= 0) {
                        score += e.scoreValue;
                        playEnemyDestructionClip(e);
                        spawnExplosion(e.x - worldScrollOffset, e.y, e.color);
                        ei.remove();
                    }
                    li.remove();
                    break;
                }
            }
        }

        // --- Collision: enemies vs Duke ---
        if (now > invincibleUntil) {
            double dukeWorldX = dukeScreenX + worldScrollOffset;
            for (Iterator<Enemy> ei = enemies.iterator(); ei.hasNext(); ) {
                Enemy e = ei.next();
                double ddx = e.x - dukeWorldX - DUKE_DRAW_WIDTH/2.0;
                double ddy = e.y - dukeScreenY - DUKE_DRAW_HEIGHT/2.0;
                if (Math.abs(ddx) < 30 && Math.abs(ddy) < 25) {
                    lives--;
                    playEnemyDestructionClip(e);
                    spawnExplosion(dukeScreenX, dukeScreenY, Color.GOLD);
                    ei.remove();
                    invincibleUntil = now + 2_000_000_000L;
                    if (lives <= 0) {
                        gameOver = true;
                    }
                    break;
                }
            }
        }

        // --- Update particles ---
        particles.removeIf(Particle::update);
    }

    void playEnemyDestructionClip(Enemy e){
      switch(e.type){
        case LANDER -> boomLander.play();
        case BAITER -> boomBaiter.play();
        case BOMBER -> boomBomber.play();
      }
    }

    // ========================================================================
    //  SPAWNING
    // ========================================================================

    private void spawnEnemy() {
        double spawnX = worldScrollOffset + WIDTH + 50 + random.nextDouble() * 200;
        double spawnY = 40 + random.nextDouble() * (HEIGHT - 150);
        EnemyType type = EnemyType.values()[random.nextInt(EnemyType.values().length)];
        enemies.add(new Enemy(spawnX, spawnY, type));
    }

    private void spawnExplosion(double x, double y, Color color) {
        for (int i = 0; i < 24; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 1.5 + random.nextDouble() * 4;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            particles.add(new Particle(x, y, vx, vy, color, 30 + random.nextInt(20)));
        }
    }

    private void restartGame() {
        score = 0;
        lives = 3;
        gameOver = false;
        worldScrollOffset = 0;
        dukeScreenX = WIDTH * DUKE_TARGET_X_RIGHT;
        dukeScreenY = HEIGHT * 0.5;
        dukeVX = 0;
        dukeVY = 0;
        facingRight = true;
        dukeAnchorT = 0.0;
        lasers.clear();
        enemies.clear();
        particles.clear();
        invincibleUntil = 0;
    }

    // ========================================================================
    //  RENDER
    // ========================================================================

    private void render(GraphicsContext gc, long now) {

        // --- Sky gradient ---
        LinearGradient sky = new LinearGradient(0, 0, 0, HEIGHT, false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(5, 5, 30)),
                new Stop(0.7, Color.rgb(15, 10, 60)),
                new Stop(1, Color.rgb(30, 15, 50)));
        gc.setFill(sky);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // --- Stars ---
        gc.setFill(Color.WHITE);
        Random starRand = new Random(42);
        for (int i = 0; i < 150; i++) {
            double sx = (starRand.nextDouble() * WORLD_WIDTH - worldScrollOffset * 0.3) % WIDTH;
            if (sx < 0) sx += WIDTH;
            double sy = starRand.nextDouble() * HEIGHT * 0.8;
            double size = 1 + starRand.nextDouble() * 1.5;
            gc.fillOval(sx, sy, size, size);
        }

        // --- Terrain ---
        drawTerrain(gc);

      // --- Lasers ---
        for (Laser l : lasers) {
            double screenX = l.x - worldScrollOffset;
            gc.setFill(Color.CYAN);
            gc.fillRoundRect(screenX, l.y - 2, 18, 4, 3, 3);
            gc.setFill(Color.WHITE);
            gc.fillRoundRect(screenX + 2, l.y - 1, 12, 2, 2, 2);
        }

      // --- Baby Dukes ---
      babyDukes.stream()  // skip those out of view
          .filter(not(baby -> baby.getX() < worldScrollOffset || baby.getX() > worldScrollOffset + WIDTH))
          .forEach(baby -> gc.drawImage(baby.getCurrentSprite(),
              baby.getX() - worldScrollOffset,
              baby.getY()));

      // --- Points from collecting baby dukes
      points.forEach(point->{
        gc.save();
        gc.setFill(Color.LIGHTGOLDENRODYELLOW);
        gc.fillText(String.valueOf(point.points),point.x-worldScrollOffset,point.y);
        gc.restore();
      });

      // --- Enemies ---
        for (Enemy e : enemies) {
            double sx = e.x - worldScrollOffset;
            drawEnemy(gc, e, sx, e.y);
        }

        // --- Duke (player) ---
        boolean blink = now < invincibleUntil && ((now / 100_000_000L) % 2 == 0);
        if (!blink) {
            drawDuke(gc, dukeScreenX, dukeScreenY);
        }


        // --- Particles ---
        for (Particle p : particles) {
            gc.setGlobalAlpha(p.life / (double) p.maxLife);
            gc.setFill(p.color);
            gc.fillOval(p.x - 2, p.y - 2, 4, 4);
        }
        gc.setGlobalAlpha(1.0);


      if(debug){
        gc.strokeLine(WIDTH/2.0,0, WIDTH/2.0, HEIGHT);
        gc.strokeLine(0,HEIGHT/2.0, WIDTH, HEIGHT/2.0);
      }

        // --- HUD ---
        drawHUD(gc);

        // --- Game Over ---
        if (gameOver) {
            gc.setFill(javafx.scene.paint.Color.color(0, 0, 0, 0.6));
            gc.fillRect(0, 0, WIDTH, HEIGHT);
            gc.setFill(Color.RED);
            gc.setFont(Font.font("Monospace", 48));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("GAME OVER", WIDTH / 2.0, HEIGHT / 2.0 - 20);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Monospace", 24));
            gc.fillText("Score: " + score, WIDTH / 2.0, HEIGHT / 2.0 + 20);
            gc.fillText("Press R to Restart", WIDTH / 2.0, HEIGHT / 2.0 + 55);
            gc.setTextAlign(TextAlignment.LEFT);
        }

        // --- Debug stats overlay ---
        if(showStats){
          double fps = fpsCalculator.update();
          int left  = WIDTH - 250;
          int top = 20;
          int i = 2;
          gc.setFill(javafx.scene.paint.Color.color(0, 0, 0, 0.6));
          gc.fillRect(left, 0, WIDTH, HEIGHT);
          gc.setFill(Color.RED);
          gc.setFont(Font.font("Monospace", 18));
          gc.setTextAlign(TextAlignment.LEFT);
          gc.fillText("stats", left, top);
          gc.setFill(Color.WHITE);
          gc.setFont(Font.font("Monospace", 16));
          gc.fillText("fps: "+ fps, left, top*(i++));
          gc.fillText("now: " + now, left, top*(i++)); 
          gc.fillText("updateRate: " + updateRate, left, top*(i++));
          gc.fillText("player position: "+(int)(dukeScreenX+worldScrollOffset)+","+(int)dukeScreenY, left, top*(i++));
          AtomicInteger bcounter = new AtomicInteger();
          AtomicInteger bi = new AtomicInteger(i);
          babyDukes.forEach( b-> {
                gc.fillText("babyDuke"+ bcounter.getAndIncrement()+": "+b.getX() +","+b.getY(),
                    left, top* (bi.getAndIncrement()));
          });                               
        }
    }

    private double dukeCenterX(double x){
      return x - DUKE_DRAW_WIDTH / 2.0;
    }
    private double dukeCenterY(double y){
      return y -  DUKE_DRAW_HEIGHT / 2.0;
    }
    // ---- Draw Duke using the duke.png sprite, rotated 90° CW and flipped when facing left ----
    private void drawDuke(GraphicsContext gc, double x, double y) {
      x = dukeCenterX(x);
      y = dukeCenterY(y);
      if(debug) {
        gc.strokeLine(dukeScreenX, 0, dukeScreenX, HEIGHT);
        gc.strokeLine(0, dukeScreenY, WIDTH, dukeScreenY);
      }
      gc.save();
      if (isLanding ) {
        gc.translate(dukeScreenX,dukeScreenY   );
        double rot = (40 - ((HEIGHT - 20) - dukeScreenY)) / 40 * (facingRight ? -90 : 90);
        gc.rotate(rot);
        gc.drawImage(facingRight? dukeRight: dukeLeft, -DUKE_DRAW_WIDTH/2.0, -DUKE_DRAW_HEIGHT/2.0);

      } else {
        gc.drawImage(facingRight ? dukeRight : dukeLeft, x, y);
        // Thruster flame — only when actively thrusting horizontally (A or D held)
        boolean thrustingHorizontally =
            keysPressed.stream().anyMatch(keyCode ->
                                              switch (keyCode) {
                                                case A, D, LEFT, RIGHT -> true;
                                                default -> false;
                                              });
        //        keysPressed.contains(KeyCode.A) || keysPressed.contains(KeyCode.D);
        if (thrustingHorizontally && Math.abs(dukeVX) > 0.15) {
          double flicker = 4 + random.nextDouble() * 6 * (Math.abs(dukeVX) / DUKE_MAX_SPEED);
          double flameBaseX = facingRight ? x - 2 : x + DUKE_DRAW_WIDTH + 2;
          double flameTipX = facingRight ? flameBaseX - flicker : flameBaseX + flicker;

          gc.setFill(Color.ORANGE);
          gc.fillPolygon(
              new double[]{flameBaseX, flameTipX, flameBaseX},
              new double[]{y + 14, y + DUKE_DRAW_HEIGHT / 2.0, y + DUKE_DRAW_HEIGHT - 14},
              3
          );
          gc.setFill(Color.YELLOW);
          gc.fillPolygon(
              new double[]{flameBaseX, facingRight ? flameBaseX - flicker * 0.5 :
                                           flameBaseX + flicker * 0.5, flameBaseX},
              new double[]{y + 16, y + DUKE_DRAW_HEIGHT / 2.0, y + DUKE_DRAW_HEIGHT - 16},
              3
          );
        }
      }
      gc.restore();
    }

  // ---- Draw enemies ----
    private void drawEnemy(GraphicsContext gc, Enemy e, double sx, double sy) {
        gc.setFill(e.color);
        switch (e.type) {
            case LANDER -> {
                gc.fillOval(sx - 15, sy - 8, 30, 16);
                gc.setFill(javafx.scene.paint.Color.LIGHTGRAY);
                gc.fillOval(sx - 8, sy - 12, 16, 10);
                gc.setFill(e.color.deriveColor(0, 1, 1, 0.3));
                gc.fillOval(sx - 20, sy - 12, 40, 24);
            }
            case BAITER -> {
                gc.beginPath();
                gc.moveTo(sx - 18, sy);
                gc.lineTo(sx, sy - 10);
                gc.lineTo(sx + 18, sy);
                gc.lineTo(sx, sy + 10);
                gc.closePath();
                gc.fill();
                gc.setFill(Color.YELLOW);
                gc.fillOval(sx - 4, sy - 4, 8, 8);
            }
            case BOMBER -> {
                gc.fillRoundRect(sx - 14, sy - 14, 28, 28, 6, 6);
                gc.setFill(Color.DARKRED);
                gc.fillRect(sx - 8, sy - 8, 16, 16);
                gc.setFill(Color.ORANGE);
                gc.fillOval(sx - 3, sy - 3, 6, 6);
            }
        }
    }

    // ---- Draw terrain ----
    private void drawTerrain(GraphicsContext gc) {
        double segW = WORLD_WIDTH / TERRAIN_SEGMENTS;
        gc.setFill(Color.rgb(20, 80, 20));
        gc.beginPath();
        gc.moveTo(0, HEIGHT);
        for (int i = 0; i <= TERRAIN_SEGMENTS; i++) {
            double sx = i * segW - worldScrollOffset;
            if (sx < -segW) continue;
            if (sx > WIDTH + segW) break;
            gc.lineTo(sx, terrainHeights[i]);
        }
        gc.lineTo(WIDTH, HEIGHT);
        gc.closePath();
        gc.fill();

        gc.setStroke(Color.rgb(40, 140, 40));
        gc.setLineWidth(2);
        gc.beginPath();
        boolean started = false;
        for (int i = 0; i <= TERRAIN_SEGMENTS; i++) {
            double sx = i * segW - worldScrollOffset;
            if (sx < -segW) continue;
            if (sx > WIDTH + segW) break;
            if (!started) {
                gc.moveTo(sx, terrainHeights[i]);
                started = true;
            } else {
                gc.lineTo(sx, terrainHeights[i]);
            }
        }
        gc.stroke();
    }

    // ---- HUD ----
    private void drawHUD(GraphicsContext gc) {
        gc.setFont(Font.font("Monospace", 18));
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("SCORE: " + score, 15, 25);
 
        for (int i = 0; i < lives; i++) {
            double lx = 15 + i * 28;
            double ly = 35;
            gc.drawImage(dukeImage, lx, ly);
        }

        double mmX = WIDTH - 210, mmY = 8, mmW = 200, mmH = 30;
        gc.setFill(javafx.scene.paint.Color.color(0, 0, 0, 0.5));
        gc.fillRect(mmX, mmY, mmW, mmH);
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1);
        gc.strokeRect(mmX, mmY, mmW, mmH);

        double dukeWorldX = dukeScreenX + worldScrollOffset;
        double dmmx = mmX + (dukeWorldX / WORLD_WIDTH) * mmW;
        gc.setFill(Color.LIME);
        gc.fillRect(dmmx - 2, mmY + 10, 4, 10);

        gc.setFill(Color.RED);
        for (Enemy e : enemies) {
            double emmx = mmX + (e.x / WORLD_WIDTH) * mmW;
            gc.fillRect(emmx - 1, mmY + 12, 3, 6);
        }

        gc.setStroke(Color.color(1, 1, 1, 0.3));
        double vpLeft = mmX + (worldScrollOffset / WORLD_WIDTH) * mmW;
        double vpWidth = (WIDTH / WORLD_WIDTH) * mmW;
        gc.strokeRect(vpLeft, mmY, vpWidth, mmH);
    }

    // ========================================================================
    //  INNER CLASSES
    // ========================================================================

    static class Laser {
        double x, y;
        boolean movingRight;

        Laser(double x, double y, boolean movingRight) {
            this.x = x;
            this.y = y;
            this.movingRight = movingRight;
        }
    }

    static class Particle {
        double x, y, vx, vy;
        Color color;
        int life, maxLife;

        Particle(double x, double y, double vx, double vy, Color color, int maxLife) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.maxLife = maxLife;
            this.life = maxLife;
        }

        boolean update() {
            x += vx;
            y += vy;
            vy += 0.05;
            life--;
            return life <= 0;
        }
    }

    enum EnemyType {
        LANDER(1, 100, ENEMY_SPEED, Color.LIMEGREEN),
        BAITER(1, 200, ENEMY_SPEED * 2.2, Color.MAGENTA),
        BOMBER(3, 300, ENEMY_SPEED * 0.7, Color.ORANGERED);

        final int hp;
        final int score;
        final double speed;
        final Color color;

        EnemyType(int hp, int score, double speed, Color color) {
            this.hp = hp;
            this.score = score;
            this.speed = speed;
            this.color = color;
        }
    }

    static class Enemy {
        double x, y;
        double vx, vy;
        int hp;
        int scoreValue;
        Color color;
        EnemyType type;
        private final Random rng = new Random();

        Enemy(double x, double y, EnemyType type) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.hp = type.hp;
            this.scoreValue = type.score;
            this.color = type.color;
            this.vx = -type.speed;
            this.vy = (rng.nextDouble() - 0.5) * 2;
        }

        void update(double playerWorldX, double playerY) {
            switch (type) {
                case LANDER -> {
                    x += vx;
                    y += Math.sin(x * 0.02) * 1.2;
                }
                case BAITER -> {
                    double dx = playerWorldX - x;
                    double dy = playerY - y;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist > 1) {
                        x += (dx / dist) * type.speed;
                        y += (dy / dist) * type.speed;
                    }
                    x += vx * 0.3;
                }
                case BOMBER -> {
                    x += vx;
                    y += vy;
                    if (y < 40 || y > 500) vy = -vy;
                }
            }
        }

        boolean hitTest(double px, double py) {
            return Math.abs(px - x) < 20 && Math.abs(py - y) < 16;
        }
    }
}
