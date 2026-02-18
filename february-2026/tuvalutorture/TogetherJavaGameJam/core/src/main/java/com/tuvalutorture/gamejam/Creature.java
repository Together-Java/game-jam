package com.tuvalutorture.gamejam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.tuvalutorture.gamejam.Room.Door;

import java.util.ArrayList;

public class Creature implements Disposable {
    public enum EntityDirection {
        LEFT, RIGHT, UP, DOWN, CENTRE;

        public Room.Direction getDirection() {
            switch (this) {
                case LEFT: return Room.Direction.WEST;
                case RIGHT: return Room.Direction.EAST;
                case UP: return Room.Direction.NORTH;
                case DOWN: return Room.Direction.SOUTH;
                case CENTRE: return Room.Direction.CENTRE;
                default: return null;
            }
        }

        public EntityDirection getOpposite() {
            switch (this) {
                case LEFT: return EntityDirection.RIGHT;
                case RIGHT: return EntityDirection.LEFT;
                case UP: return EntityDirection.DOWN;
                case DOWN: return EntityDirection.UP;
                case CENTRE: return EntityDirection.CENTRE;
                default: return null;
            }
        }
    }

    public int mapX, mapY, layer;
    public float pixelX, pixelY;

    private int spriteWidth;
    private int spriteHeight;

    private Texture upTexture;
    private Texture downTexture;
    private Texture leftTexture;
    private Texture rightTexture;

    private Texture upHurtTexture;
    private Texture downHurtTexture;
    private Texture leftHurtTexture;
    private Texture rightHurtTexture;

    private Anim upAnim;
    private Anim downAnim;
    private Anim leftAnim;
    private Anim rightAnim;
    private Anim idleAnim;

    public Room room;

    private Texture defaultTexture;

    private EntityDirection direction;

    private Rectangle boundingBox;

    private float speed;

    private String assetFolder;

    public boolean isMoving = false;

    private boolean justEnteredDoor = false;

    public void setSpeed(float speed) {
        if (speed <= 0) return;
        this.speed = speed;
    }

    public float getSpeed() {
        return speed;
    }

    public void setDirection(EntityDirection direction) {
        this.direction = direction;
    }

    public EntityDirection getDirection() {
        return direction;
    }

    public void setSpriteWidth(int spriteWidth) {
        this.spriteWidth = spriteWidth;
    }

    public int getSpriteWidth() {
        return spriteWidth;
    }

    public void setSpriteHeight(int spriteHeight) {
        this.spriteHeight = spriteHeight;
    }

    public int getSpriteHeight() {
        return spriteHeight;
    }

    public void setUpDirectionalAnim(String animFolder, int frameCount, EntityDirection direction, int frameWidth, int frameHeight, float frameTime) {
        switch (direction) {
            case LEFT:  leftAnim    = Anim.loadAnimFromFolder(assetFolder + "/" + animFolder, frameWidth, frameHeight, frameCount, frameTime); leftAnim.setPlaybackMode(Animation.PlayMode.LOOP);   break;
            case RIGHT: rightAnim   = Anim.loadAnimFromFolder(assetFolder + "/" + animFolder, frameWidth, frameHeight, frameCount, frameTime); rightAnim.setPlaybackMode(Animation.PlayMode.LOOP);  break;
            case UP:    upAnim      = Anim.loadAnimFromFolder(assetFolder + "/" + animFolder, frameWidth, frameHeight, frameCount, frameTime); upAnim.setPlaybackMode(Animation.PlayMode.LOOP);     break;
            case DOWN:  downAnim    = Anim.loadAnimFromFolder(assetFolder + "/" + animFolder, frameWidth, frameHeight, frameCount, frameTime); downAnim.setPlaybackMode(Animation.PlayMode.LOOP);   break;
            default:    idleAnim    = Anim.loadAnimFromFolder(assetFolder + "/" + animFolder, frameWidth, frameHeight, frameCount, frameTime); idleAnim.setPlaybackMode(Animation.PlayMode.LOOP);   break;
        }
    }

    public void loadDirectionTextures() {
        upTexture = new Texture(Gdx.files.internal(assetFolder + "/up.png"));
        downTexture = new Texture(Gdx.files.internal(assetFolder + "/down.png"));
        leftTexture = new Texture(Gdx.files.internal(assetFolder + "/left.png"));
        rightTexture = new Texture(Gdx.files.internal(assetFolder + "/right.png"));
    }

    public void loadHurtTextures() {
        upHurtTexture = new Texture(Gdx.files.internal(assetFolder + "/up_hurt.png"));
        downHurtTexture = new Texture(Gdx.files.internal(assetFolder + "/down_hurt.png"));
        leftHurtTexture = new Texture(Gdx.files.internal(assetFolder + "/left.hurt.png"));
        rightHurtTexture = new Texture(Gdx.files.internal(assetFolder + "/right.hurt.png"));
    }

    public Creature(String folder, String defaultTexturePath) {
        this.assetFolder = folder;

        defaultTexture = new Texture(Gdx.files.internal((assetFolder + "/" + defaultTexturePath)));

        spriteWidth = defaultTexture.getWidth();
        spriteHeight = defaultTexture.getHeight();

        direction = EntityDirection.UP;
        boundingBox = new Rectangle(spriteWidth, spriteHeight, spriteWidth, spriteHeight);
    }

    public Texture getCurrentHurtTexture() {
        switch (direction) {
            case LEFT:
                return leftHurtTexture;
            case RIGHT:
                return rightHurtTexture;
            case UP:
                return upTexture;
            case DOWN:
                return downTexture;
            default:
                return null;
        }
    }

    public Texture getCurrentIdleTexture() {
        return getIdleTexture(this.direction);
    }

    public Texture getIdleTexture(EntityDirection desiredDirection) {
        switch (desiredDirection) {
            case LEFT:
                return leftTexture;
            case RIGHT:
                return rightTexture;
            case UP:
                return upTexture;
            case DOWN:
                return downTexture;
            default:
                return null;
        }
    }

    public Anim getCurrentAnimation() {
        return getAnim(this.direction);
    }

    public Anim getAnim(EntityDirection desiredDirection) {
        switch (desiredDirection) {
            case LEFT:
                return leftAnim;
            case RIGHT:
                return rightAnim;
            case UP:
                return upAnim;
            case DOWN:
                return downAnim;
            default:
                return null;
        }
    }

    public GameMap.GameMapTile tileInFront(int layer, float pixelX, float pixelY) {
        return room.roomMap.findTileAt(layer, GameMap.resolvePixelToCoordinate((int)pixelX), GameMap.resolvePixelToCoordinate((int)pixelY));
    }

    public boolean isWallAtPixel(int layer, float pixelX, float pixelY) {
        GameMap.GameMapTile tile = tileInFront(layer, pixelX, pixelY);
        return tile != null && tile.hasAttribute("wall");
    }

    public Door doorAtPixel(EntityDirection direction, float pixelX, float pixelY) {
        Door[] doors = room.getDoors(direction.getDirection());
        if (doors == null || doors.length == 0) return null;
        GameMap.Coordinate position = new GameMap.Coordinate(GameMap.resolvePixelToCoordinate((int)pixelX), GameMap.resolvePixelToCoordinate((int)pixelY));
        for (Door door : doors) {
            if (door.getLocation().getX() == position.getX() && door.getLocation().getY() == position.getY()) {
                System.out.println("found door");
                return door;
            }
        }
        return null;
    }

    public void move(EntityDirection direction) {
        if (direction == null) {
            isMoving = false;
            return;
        }

        float delta = Gdx.graphics.getDeltaTime();
        float speed = getSpeed();

        float nextX = pixelX;
        float nextY = pixelY;

        int width = getSpriteWidth();
        int height = getSpriteHeight();

        isMoving = true;
        setDirection(direction);

        switch (direction) {
            case RIGHT:
                nextX += speed * delta;
                if (!isWallAtPixel(1, nextX + width, pixelY) && !isWallAtPixel(1,nextX + width, pixelY + height)) pixelX = nextX;
                else { isMoving = false; return;}
                break;

            case LEFT:
                nextX -= speed * delta;
                if (!isWallAtPixel(1, nextX, pixelY) && !isWallAtPixel(1, nextX, pixelY + height)) pixelX = nextX;
                else { isMoving = false; return;}
                break;

            case UP:
                nextY += speed * delta;
                if (!isWallAtPixel(1, pixelX, nextY + height) && !isWallAtPixel(1,pixelX + width, nextY + height)) pixelY = nextY;
                else { isMoving = false; return;}
                break;

            case DOWN:
                nextY -= speed * delta;
                if (!isWallAtPixel(1, pixelX, nextY) && !isWallAtPixel(1,pixelX + width, nextY)) pixelY = nextY;
                else { isMoving = false; return;}
                break;
        }

        if (pixelX < 0) pixelX = Gdx.graphics.getWidth() - 1;
        if (pixelY < 0) pixelY = Gdx.graphics.getHeight() - 1;
        if (pixelX > Gdx.graphics.getWidth() - 1) pixelX = 0;
        if (pixelY > Gdx.graphics.getHeight() - 1) pixelY = 0;

        getCurrentAnimation().update(delta);

        Door door = doorAtPixel(direction, pixelX, pixelY);

        if (door != null && !justEnteredDoor) {
            room = door.getNextDoor().getThisRoom();

            GameMap.Coordinate dest = door.getNextLocation();

            pixelX = GameMap.resolveCoordinateToPixel(dest.getX());
            pixelY = GameMap.resolveCoordinateToPixel(dest.getY());

            setDirection(direction.getOpposite());

            isMoving = false;
        } else if (room == null) {
            justEnteredDoor = false;
        }
    }

    public Texture returnFrame() {
        return isMoving ? getCurrentAnimation().returnCurrentFrame() : getCurrentIdleTexture();
    }

    public Rectangle getBoundingBox() {
        return boundingBox;
    }

    public void dispose() {
        upTexture.dispose();
        rightTexture.dispose();
        leftTexture.dispose();
        downTexture.dispose();
        upAnim.dispose();
        rightAnim.dispose();
        leftAnim.dispose();
        downAnim.dispose();
        downHurtTexture.dispose();
        leftHurtTexture.dispose();
        rightHurtTexture.dispose();
        upHurtTexture.dispose();
        defaultTexture.dispose();
    }
}
