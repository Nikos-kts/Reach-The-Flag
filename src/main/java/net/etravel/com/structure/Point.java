package net.etravel.com.structure;

/**
 * Helper wrapper class of each node "location"
 */
public class Point {

    /**
     * Contains the String value of key per node in the form of row|col
     */
    private String keyLocation;
    /**
     * int value of row index in the matrix (maze)
     */
    private int row;
    /**
     * int value of col index in the matrix (maze)
     */
    private int col;

    public Point(int row, int col) {
        this.row = row;
        this.col = col;
        this.keyLocation = row + "|" + col;
    }

    public String getKeyLocation() {
        return keyLocation;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}
