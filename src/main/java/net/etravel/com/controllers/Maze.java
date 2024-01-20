package net.etravel.com.controllers;

import net.etravel.com.properties.PropertyReader;
import net.etravel.com.structure.*;

import java.util.Random;

public class Maze {

    public static int rowSize = 2;
    public static int colSize = 2;
    public static int matrixSize = 4;
    public static int obstacles = 0;
    Node[][] nodes;
    private final static Maze instance = new Maze();
    public StartingNode startingNode = null;
    public EndingNode endingNode = null;


    private Maze() {
        rowSize = PropertyReader.getInstance().getInteger("matrix_row", 2);
        colSize = PropertyReader.getInstance().getInteger("matrix_col", 2);
        double obstacleRatio = PropertyReader.getInstance().getDouble("obstacle_ratio", 0.20);
        matrixSize = rowSize * colSize;
        obstacles = (int) Math.round((matrixSize * obstacleRatio));
    }


    public static Maze getMaze() {
        return instance;
    }


    public void initMaze() {
        dispose();
        populate();
        printMazeInConsole();
    }

    private void populate() {
        if (nodes != null && nodes.length > 0) {
            dispose();
        }
        Random rand = new Random();
        nodes = new Node[rowSize][colSize];
        int obstaclesConstructed = 0;
        while (obstaclesConstructed < obstacles) {

            int row = Math.abs(rand.nextInt()) % rowSize;
            int col = Math.abs(rand.nextInt()) % colSize;
            Node randomNode = nodes[row][col];
            if (randomNode == null) {
                Obstacle o = new Obstacle(new Point(row, col));
                nodes[row][col] = o;
                DisplayController.getDisplayController().updateGuiNode(o);
                obstaclesConstructed++;
            }
        }
        boolean startingPointConstructed = false;
        while (!startingPointConstructed) {
            int row = Math.abs(rand.nextInt()) % rowSize;
            int col = Math.abs(rand.nextInt()) % colSize;
            Node randomNode = nodes[row][col];
            if (randomNode == null) {
                StartingNode s = new StartingNode(new Point(row, col));
                nodes[row][col] = s;
                this.startingNode = s;
                DisplayController.getDisplayController().updateGuiNode(s);
                startingPointConstructed = true;
            }
        }

        boolean endingPointConstructed = false;
        while (!endingPointConstructed) {
            int row = Math.abs(rand.nextInt()) % rowSize;
            int col = Math.abs(rand.nextInt()) % colSize;
            Node randomNode = nodes[row][col];
            if (randomNode == null) {
                EndingNode e = new EndingNode(new Point(row, col));
                nodes[row][col] = e;
                this.endingNode = e;
                DisplayController.getDisplayController().updateGuiNode(e);
                endingPointConstructed = true;
            }
        }

        for (int row = 0; row < rowSize; row++) {
            for (int col = 0; col < colSize; col++) {
                if (nodes[row][col] == null) {
                    Tile t = new Tile(new Point(row, col));
                    nodes[row][col] = t;
                    DisplayController.getDisplayController().updateGuiNode(t);
                }
            }
        }
    }

    public StartingNode getStartingNode() {
        return startingNode;
    }

    public void dispose() {
        nodes = null;
    }

    public Node getNode(int row, int col) {
        if (row < 0 || col < 0 || row > rowSize - 1 || col > colSize - 1) {
            return null;
        } else {
            return nodes[row][col];
        }
    }

    public Node getNode(String keyLocation) {
        String[] split = keyLocation.split("\\|");
        return nodes[Integer.parseInt(split[0])][Integer.parseInt(split[1])];
    }

    public void resetTiles() {
        for (Node[] n : nodes) {
            for (Node m : n)
                if (m instanceof Tile) {
                    ((Tile) m).resetInInitialState();
                    DisplayController.getDisplayController().updateGuiNode(m);
                }
        }
    }

    public boolean isMazeConstructed() {
        return nodes != null;
    }

    public void printMazeInConsole() {
        System.out.println();
        for (int i = 0; i < rowSize; i++) {
            for (int j = 0; j < colSize; j++) {
                Node n = nodes[i][j];
                if(n != null) {
                    if (n.isStartingPoint()) {
                        System.out.print("S");
                    } else if (n.isEndingPoint()) {
                        System.out.print("E");
                    } else if (n.isObstacle()) {
                        System.out.print("X");
                    } else {
                        System.out.print("_");
                    }
                }
            }
            System.out.println();
        }
    }
}
