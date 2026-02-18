package com.tuvalutorture.gamejam;

public class Floor {
    private int floorNumber;
    private Item containedItem;
    public Room[] rooms;

    public Floor(Room[] rooms, int floorNumber) {
        this.rooms = rooms;
        this.floorNumber = floorNumber;
    }
}
