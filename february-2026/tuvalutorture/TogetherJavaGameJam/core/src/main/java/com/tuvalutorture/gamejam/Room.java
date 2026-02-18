package com.tuvalutorture.gamejam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Room {
    public enum RoomType {
        STAIRWELL,
        BLOOD_ALTAR,
        ENRICHMENT_CENTRE,
        MEETING_ROOM,
        OFFICE_ROOM,
        CUBICLES,
        CAR_CRASH_SITE,
        WITCHES_ROOM,
        DETONATION_SITE,
        LAVATORY
    }

    public enum RoomDirection {
        CENTRE,
        CORNER_TOP_LEFT,
        CORNER_TOP_RIGHT,
        CORNER_BOTTOM_LEFT,
        CORNER_BOTTOM_RIGHT,
        RIGHT,
        LEFT,
        TOP,
        BOTTOM
    }

    public enum Direction {
        NORTH("north"),
        EAST("east"),
        SOUTH("south"),
        WEST("west"),
        CENTRE("centre");

        private final String attributeName;
        private static final Map<String, Direction> attributeNameToDirection = new HashMap<>();

        static {
            for (Direction direction : Direction.values()) {
                attributeNameToDirection.put(direction.attributeName, direction);
            }
        }

        private Direction(String attributeName) {
            this.attributeName = attributeName;
        }

        public static Direction getDirection(String attributeName) {
            return attributeNameToDirection.get(attributeName);
        }

        public String getAttributeName() {
            return attributeName;
        }
    }

    public static class Door {
        private Room thisRoom;
        private Door nextDoor;
        private Room.Direction direction;
        private GameMap.Coordinate location;

        public static Room.Direction findOpposingDirection(Room.Direction d) {
            switch (d) {
                case NORTH: return Room.Direction.SOUTH;
                case EAST: return Room.Direction.WEST;
                case SOUTH: return Room.Direction.NORTH;
                case WEST: return Room.Direction.EAST;
                default: return Room.Direction.CENTRE;
            }
        }

        public Room.Direction oppositeDirection() {
            return findOpposingDirection(direction);
        }

        public Door(Room roomIn, Door nextDoor, Room.Direction direction, GameMap.Coordinate location) {
            this.thisRoom = roomIn;
            this.nextDoor = nextDoor;
            this.direction = direction;
            this.location = location;
        }

        public void setNext(Door next) {
            this.nextDoor = next;
        }

        public GameMap.Coordinate getLocation() {
            return location;
        }

        public GameMap.Coordinate getNextLocation() {
            return nextDoor.getLocation();
        }

        public Door getNextDoor() {
            return nextDoor;
        }

        public Room getThisRoom() {
            return thisRoom;
        }
    }

    public GameMap roomMap;

    private RoomType roomType;
    private RoomDirection roomDirection;

    private final HashMap<Direction, ArrayList<Door>> doorways = new HashMap<>();
    private final HashMap<Direction, Room> rooms = new HashMap<>();
    private Item keyItem;
    private float shutDownTimer;
    private boolean isShutDown;

    public Room(GameMap map) {
        this.roomMap = map;
    }

    public void addDoorway(Direction direction) {
        doorways.put(direction, new ArrayList<>());
    }

    public void addDoor(Room.Direction direction, Door door) {
        doorways.get(direction).add(door);
    }

    public Door[] getDoors(Direction direction) {
        ArrayList<Door> doors = doorways.get(direction);
        if (doors == null) return null;
        return doors.toArray(new Door[0]);
    }

    public void populateDoors() {
        GameMap.GameMapTile[] doors = roomMap.findAllTilesWithAttribute("door");
        for (GameMap.GameMapTile door : doors) {
            Direction direction = null;
            for (Direction d : Direction.values()) if (door.hasAttribute(d.attributeName)) direction = d;
            if (this.doorways.get(direction) == null && direction != null) addDoorway(direction);
            System.out.println("registered door at" + door.coordinate.getX() + "," + door.coordinate.getY());
            this.doorways.get(direction).add(new Door(this, null, direction, door.coordinate));
        }
    }

    public static void linkDoor(Door door, Door source) {
        door.setNext(source);
    }

    public static void linkDoorways(Direction direction, Room dest, Room source) {
        for (int i = 0; i < dest.doorways.get(direction).size(); i++) {
            dest.doorways.get(direction).get(i).setNext(source.doorways.get(Door.findOpposingDirection(direction)).get(i));
            source.doorways.get(Door.findOpposingDirection(direction)).get(i).setNext(dest.doorways.get(direction).get(i));
        }
    }

    public void makeDoorwayFromRoom(Direction direction, Room source) {
        ArrayList<Door> doorway = doorways.get(direction), sourceDoorWay = source.doorways.get(direction);
        if (doorway == null) {
            doorway = new ArrayList<Door>();
            doorways.put(direction, doorway);
        }

        doorway.clear();
        GameMap.GameMapTile[] doors = roomMap.findAllTilesWithAttribute("door");
        GameMap.GameMapTile[] doorsOfDirection = new GameMap.GameMapTile[sourceDoorWay.size()];
        int index = 0;
        for (GameMap.GameMapTile door : doors) {
            if (door.hasAttribute(direction.attributeName)) doorsOfDirection[index++] = door;
        }

        for (int i = 0; i < sourceDoorWay.size(); ++i) {
            doorway.add(new Door(this, sourceDoorWay.get(i), direction, doorsOfDirection[i].coordinate));
        }
    }
}
