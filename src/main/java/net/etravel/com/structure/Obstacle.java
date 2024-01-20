package net.etravel.com.structure;

/**
 * Class that represents the obstacles. Not accessible black color Nodes
 * Attributes:
 * - Never accessible
 */
public class Obstacle extends  Node {

    public Obstacle(Point point) {
      this.point = point;
    }

    @Override
    public boolean isAccessible() {
        return false;
    }

    @Override
    public boolean isObstacle() {
        return true;
    }

    @Override
    public boolean isStartingPoint() {
        return false;
    }

    @Override
    public boolean isEndingPoint() {
        return false;
    }
}
