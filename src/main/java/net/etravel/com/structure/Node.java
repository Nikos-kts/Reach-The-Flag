package net.etravel.com.structure;

/**
 * Abstract class handling all kind of different Tiles
 */
public abstract  class Node {


    /**
     * A Point class that holds the key for each node as well as the int values of rows and columns
     */
    protected  Point point;


    public abstract boolean isAccessible();

    public abstract boolean isObstacle();

    public abstract boolean isStartingPoint();

    public abstract boolean isEndingPoint();


    public Point getPoint() {
        return point;
    }

    @Override
    public String toString() {
        return point.getKeyLocation() + " ";
    }
}
