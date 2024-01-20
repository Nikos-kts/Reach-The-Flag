package net.etravel.com.structure;

/**
 * Super class for Ending/Starting Tiles as well as normal Tiles
 * Attributes:
 * - Is accessible only when wanderer is exploring a new path
 */
public class Tile extends Node {

    private boolean accessible = true;

    /**
     * Number of times wanderer has visited this tile
     */
    private int visited = 0;


    public Tile(Point point) {
        this.point = point;
    }

    @Override
    public boolean isAccessible() {
        return this.accessible;
    }

    @Override
    public boolean isObstacle() {
        return false;
    }

    @Override
    public boolean isStartingPoint() {
        return false;
    }

    @Override
    public boolean isEndingPoint() {
        return false;
    }


    public void setAccessible(boolean accessible) {
        this.accessible = accessible;
    }

    public void resetInInitialState(){
        this.accessible = true;
    }
}
