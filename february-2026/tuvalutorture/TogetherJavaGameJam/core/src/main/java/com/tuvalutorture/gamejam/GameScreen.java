package com.tuvalutorture.gamejam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen implements Screen {

    private SpriteBatch batch;
    private Player player;
    private Viewport viewport;
    private OrthographicCamera camera;
    private UserInterface ui;
    private float x = 100;
    private float y = 100;
    private float speed = 60f;
    private Room[] rooms;
    private int mapSelected;
    private float mapCooldown;
    private FontRenderer font;


    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(640, 480, camera);

        // load the map
        rooms = new Room[5];
        rooms[0] = new Room(GameMap.loadFromFile("maps/test1.tmap"));
        rooms[0].populateDoors();
        rooms[1] = new Room(GameMap.loadFromFile("maps/test2.tmap"));
        rooms[1].populateDoors();
        rooms[2] = new Room(GameMap.loadFromFile("maps/test.tmap"));
        rooms[2].populateDoors();
        rooms[3] = new Room(GameMap.loadFromFile("maps/test3.tmap"));
        rooms[3].populateDoors();
        rooms[4] = new Room(GameMap.loadFromFile("maps/test4.tmap"));
        rooms[4].populateDoors();

        Room.linkDoorways(Room.Direction.EAST, rooms[1], rooms[2]);
        Room.linkDoorways(Room.Direction.WEST, rooms[3], rooms[2]);

        mapSelected = 2;
        GameState.currentRoom = rooms[2];

        font = new FontRenderer("black", 8, 8, 2);
        font.addMessage(new FontRenderer.Message("The quick brown fox jumps over the lazy dog.", batch,40, 420, 2));
        font.addMessage(new FontRenderer.Message("THE QUICK BROWN FOX JUMPS OVER THE LAZY DOG", batch,40, 420, 2));
        font.addMessage(new FontRenderer.Message("kerning", batch,40, 420, 2));
        font.addMessage(new FontRenderer.Message("KERNING", batch,40, 420, 2));

        player = new Player();
        GameState.player = player;
        player.centreOnScreen();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 0.5f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (mapCooldown > 0) mapCooldown -= delta;

        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            player.playerEntity.move(Creature.EntityDirection.LEFT);
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            player.playerEntity.move(Creature.EntityDirection.RIGHT);
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            player.playerEntity.move(Creature.EntityDirection.UP);
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            player.playerEntity.move(Creature.EntityDirection.DOWN);
        } else {
            player.playerEntity.move(null);
        }

        GameState.currentRoom = player.playerEntity.room;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT_BRACKET) && mapCooldown <= 0) {
            GameState.currentRoom = rooms[--mapSelected];
            player.playerEntity.room = rooms[mapSelected];
            mapCooldown = 0.5f;
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT_BRACKET) && mapCooldown <= 0) {
            GameState.currentRoom = rooms[++mapSelected];
            player.playerEntity.room = rooms[mapSelected];
            mapCooldown = 0.5f;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.E) && mapCooldown <= 0) {
            font.removeMessage(0);
            mapCooldown = 0.5f;
        }

        GameState.currentRoom.roomMap.updateAnimations();

        batch.begin();

        GameState.currentRoom.roomMap.renderLayer(batch,0);

        batch.draw(
            player.playerEntity.returnFrame(),
            player.playerEntity.pixelX,
            player.playerEntity.pixelY,
            player.playerEntity.getSpriteWidth(),
            player.playerEntity.getSpriteHeight()
        );

        GameState.currentRoom.roomMap.renderLayer(batch,1);

        UserInterface.draw(batch);

        font.renderCurrent();

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        player.playerEntity.dispose();
        batch.dispose();
        font.dispose();
        for (Room room : rooms) {
            room.roomMap.dispose();
        }
    }
}
