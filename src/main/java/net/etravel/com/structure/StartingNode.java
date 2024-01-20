package net.etravel.com.structure;

/**
 * Class representing the Starting Node
 * Attributes:
 * - Accessible only in the start of the game
 * - Is Starting Point
 */
public class StartingNode extends  Tile {

    public StartingNode(Point point) {
        super(point);
    }

    @Override
    public boolean isAccessible() {
        return super.isAccessible();
    }

    @Override
    public boolean isObstacle() {
        return false;
    }

    @Override
    public boolean isStartingPoint() {
        return true;
    }

    @Override
    public boolean isEndingPoint() {
        return false;
    }
}