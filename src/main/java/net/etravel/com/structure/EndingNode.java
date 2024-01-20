package net.etravel.com.structure;

/**
 * Class representing the Ending Node
 * Attributes:
 * - Always accessible
 * - is Ending Point
 */
public class EndingNode extends  Tile {


    public EndingNode(Point point) {
        super(point);
    }

    @Override
    public boolean isAccessible() {
        return true;
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
        return true;
    }
}
