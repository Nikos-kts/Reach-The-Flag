package net.etravel.com.controllers;

import net.etravel.com.logger.LoggingController;
import net.etravel.com.properties.PropertyReader;
import net.etravel.com.structure.Node;
import net.etravel.com.structure.Tile;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Wanderer {

    private final static Wanderer instance = new Wanderer();
    private Node startingNode;
    private Node previousNode, currentNode;
    private int numberOfSolutions = 0;
    /**
     * Solutions Registry
     */
    private final ConcurrentHashMap<Integer, List<String>> solutions;
    /**
     * boolean showing if at least 1 solution is found
     */
    private boolean foundSolution = false;
    /**
     * The path that wanderer has followed since he started exploring
     */
    private final List<String> path;
    /**
     * List containing an optimal solution path
     */
    private final List<String> optimalSolution;
    /**
     * Simulation sleep time of each wanderer move (in millis). Defined from configuration.properties
     */
    private long simulationSleepTime = 0;
    /**
     * The thread running the simulation (in order to still interact with the UI)
     */
    private Thread simulationThread = null;


    private Wanderer() {
        solutions = new ConcurrentHashMap<Integer, List<String>>();
        path = new ArrayList<String>();
        optimalSolution = new ArrayList<String>();
    }


    /**
     * Initializes the wanderer in the starting Node and setting initial conditions
     */
    public void init() {
        startingNode = Maze.getMaze().getStartingNode();
        currentNode = startingNode;
        previousNode = currentNode;
        path.clear();
        optimalSolution.clear();
        path.add(startingNode.getPoint().getKeyLocation());
    }

    public static Wanderer getWanderer() {
        return instance;
    }

    /**
     * Checkes whether wanderer is currently stepping on the Goal Tile
     * @return true if the wanderer is on Goal tile, false otherwise
     */
    private boolean checkIfGoal() {
        if (currentNode.isEndingPoint()) {
            LoggingController.getLogger().log(Level.FINE, "Reached ending point!");
            foundSolution = true;
            return true;
        }
        return false;
    }

    /**
     * @return true if the simulation is running, false otherwise
     */
    public boolean isCurrentlyExploring() {
        return simulationThread != null && simulationThread.isAlive();
    }

    /**
     * This method checks whether wanderer can move to a node (Starting Tile, Ending Tile, visitable Tile, Obstacle)
     * @param node the node that is being examined
     * @return true if wanderer can move to node while searching for Goal tile, false otherwise
     */
    private boolean canMoveToNode(Node node) {
        boolean canMove;
        canMove = (node != null);
        if (canMove) {
            canMove = !node.isObstacle();
            canMove &= node.isAccessible();
            canMove &= !node.equals(previousNode);
            canMove &= !node.equals(startingNode);
            if (node instanceof Tile && !node.isEndingPoint()) {
                if (!path.contains(node.getPoint().getKeyLocation())) {
//                    canMove &= !((Tile) node).isVisitedOnce();
                } else {
                    canMove = false;
                }
            }
        }

        return canMove;
    }

    /**
     * Main methoc that starts the Maze exploration by starting the simulation in a separate thread
     */
    public void exploreMaze() {
        simulationSleepTime = PropertyReader.getInstance().getInteger("simulation_step_time", 0);
        if(!isCurrentlyExploring()) {
            simulationThread = new Thread("Simulation") {
                @Override
                public void run() {
                    LoggingController.getLogger().log(Level.INFO, "Exploring maze. . .");
                    explore(startingNode, path, 0);
                    displaySolutions();
                    DisplayController.getDisplayController().showResultMessage(foundSolution, solutions, optimalSolution);
                }
            };
            simulationThread.start();
        }
    }


    /**
     * Recursive algorithm which exhausts every possible path the wanderer can take in order to find all the possible solutions towards the Goal Tile
     * @param node The node that wanderer should explore for
     * @param path The path with the row|col coordinates that wanderer is currently traversing. Adds an entry when moving to and deletes one when returning from
     * @param depth The number of the recursive function calls. Used in the algorithm in order to delete path entries fast when returning
     */
    private void explore(Node node, List<String> path, int depth) {
        depth++;

        LoggingController.getLogger().log(Level.FINEST, "---------------------------------------------------------------");
        LoggingController.getLogger().log(Level.FINEST, "Exploring Node -> " + node.toString() + " Depth: " + depth);
        Set<Node> choices = getAvailableChoices();
        LoggingController.getLogger().log(Level.FINEST, "Available paths for node: " + currentNode.toString() + " are -> ");
        choices.forEach(c ->  LoggingController.getLogger().log(Level.FINEST, (c.toString() + " ")));
        if (!currentNode.isAccessible()) {
            ((Tile) currentNode).setAccessible(true);
        }
        LoggingController.getLogger().log(Level.FINEST, "");
        if (!choices.isEmpty()) {
            try {
                List<Tile> listOfChoices = choices.stream().map(Tile.class::cast).sorted(Comparator.comparing(Tile::isEndingPoint).reversed()).collect(Collectors.toList());
                Node lastVisited;
                for (Tile t : listOfChoices) {
                    lastVisited = t;
                    if (t != null && t.isAccessible()) {
                        boolean foundGoal = moveToTile(t);
                        path.add(t.getPoint().getKeyLocation());

                        // Perform sleep operation whether sleep duration is defined in configuration.properties
                        if (simulationSleepTime > 0) {
                            try {
                                Thread.sleep(simulationSleepTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        if (!foundGoal) {
                            //If the explored tile is not the Goal then keep exploring on this path
                            explore(currentNode, path, depth);
                            //After returning from each tile when getting in a deadlock, reset these tiles in initial state in order to re investigate later for a different path
                            ((Tile) currentNode).resetInInitialState();
                            //Also remove from the path this last tile, as it is not in the solution
                            path.remove(depth);
                        } else {
                            // We reached our goal so we register the solution in the solutions registry
                            registerSolution(path);
                            //Deleting last entry (goal)
                            deleteLastEntry(path);
                            //Set the current node to the other choice that we had found before finding the Goal
                            currentNode = lastVisited;
                        }
                    }
                }
            } catch (IndexOutOfBoundsException iobe) {
                // In case we are out of bounds (never happened)
                iobe.printStackTrace();
                explore(currentNode, path, depth);
            }


        } else {
            // Found a deadlock, so the algorithm starts returning
            LoggingController.getLogger().log(Level.FINER, ("Found DEADLOCK!"));
        }
        //Setting our wanderer in the previous tile while returning
        if (path.size() > 1) {
            currentNode = Maze.getMaze().getNode(path.get(depth - 1));
            previousNode = Maze.getMaze().getNode(path.get(depth - 2));
        } else {
            currentNode = startingNode;
            previousNode = currentNode;
        }
        LoggingController.getLogger().log(Level.FINEST, "---------------------------------------------------------------");
    }

    /**
     * Deletes the last entry of a list from strings
     * @param list
     */
    private void deleteLastEntry(List<String> list) {
        if (!list.isEmpty()) {
            list.remove(list.size() - 1);
        }
    }

    /**
     * Constructing the displayble solutions as well as the optimal solution, if at least 1 solution is found
     */
    public void displaySolutions() {
        StringBuilder res = new StringBuilder("\n");
        if(solutions.keySet().size() > 0) {
            for (Integer key : solutions.keySet()) {
                List<String> sols = solutions.get(key);
                res.append("Number ").append(key).append(" solution: ");
                if (sols != null) {
                    res.append(constructDisplayableSolution(sols));
                }
                res.append("\n");
            }
            LoggingController.getLogger().log(Level.INFO, res.toString());

            if (!optimalSolution.isEmpty()) {
                res = new StringBuilder(constructDisplayableSolution(optimalSolution));
                StringBuilder stars = new StringBuilder();
                for (int i = 0; i < res.length() + 30; i++) {
                    stars.append("*");
                }
                StringBuilder logMessage = new StringBuilder(stars).append("\n").append("\tOptimal solution is: ").append(res).append("\n\t").append(stars);
                LoggingController.getLogger().log(Level.INFO, logMessage.toString());
            }
            DisplayController.getDisplayController().paintOptimalSolution(optimalSolution);
        } else {
            LoggingController.getLogger().log(Level.INFO, "No solutions found for this maze");
        }
    }

    /**
     * Get all solutions as a displayable string
     * @param includeOptimal boolean whether to include the optimal solution in the returning string
     * @return a human-readable representation of the solutions
     */
    public String getAllSolutions(boolean includeOptimal) {
        StringBuilder res = new StringBuilder();
        for (Integer key : solutions.keySet()) {
            List<String> sols = solutions.get(key);
            res.append("Number ").append(key).append(" solution: ");
            if (sols != null) {
                res.append(constructDisplayableSolution(sols));
            }
            res.append("\n");
        }
        if (includeOptimal) {
            if (!optimalSolution.isEmpty()) {
                res.append("\nOptimal solution is: ");
                res.append(constructDisplayableSolution(optimalSolution));
            }
        }
        return res.toString();
    }

    /**
     * Constructs the human-readable string from the path of the solution
     * @param solution
     * @return the human-readable string of the solution
     */
    public String constructDisplayableSolution(List<String> solution) {
        StringBuilder res = new StringBuilder();
        for (String key : solution) {
            Node node = Maze.getMaze().getNode(key);
            if (node.isStartingPoint()) {
                res.append("((S) ");
            } else if (node.isEndingPoint()) {
                res.append("((E) ");
            } else {
                res.append("((T) ");
            }
            res.append(node.getPoint().getRow()).append(",").append(node.getPoint().getCol()).append("), ");
        }
        res = new StringBuilder(res.substring(0, res.length() - 2));
        return res.toString();
    }


    /**
     * Moves the wanderer towards this tile
     * @param tile the tile that wanderer should move on to
     * @return true if the tile is the goal, false otherwise
     */
    private boolean moveToTile(Tile tile) {
        DisplayController.getDisplayController().paintVisitedTile(currentNode);
        previousNode = currentNode;
        currentNode = tile;
        Set<Node> availableChoices = getAvailableChoices();
        // This is a safety measure in order for the wanderer to not endlessly loop over a path
        if (!currentNode.isAccessible()) {
//            ((Tile) currentNode).setMaxVisitTries(availableChoices.size());
            ((Tile) currentNode).setAccessible(true);
        }
        LoggingController.getLogger().log(Level.FINE, "Moving from " + previousNode.toString() + " to " + tile);
        return checkIfGoal();
    }

    /**
     * Gets a set of nodes that are valid possible moves from the current Node
     * @return
     */
    private Set<Node> getAvailableChoices() {
        int row = currentNode.getPoint().getRow();
        int col = currentNode.getPoint().getCol();
        List<Node> list = new ArrayList<Node>();
        Node northNode = Maze.getMaze().getNode(row - 1, col);
        Node southNode = Maze.getMaze().getNode(row + 1, col);
        Node eastNode = Maze.getMaze().getNode(row, col + 1);
        Node westNode = Maze.getMaze().getNode(row, col - 1);
        if (canMoveToNode(northNode)) {
            list.add(northNode);
        }
        if (canMoveToNode(southNode)) {
            list.add(southNode);
        }
        if (canMoveToNode(eastNode)) {
            list.add(eastNode);
        }
        if (canMoveToNode(westNode)) {
            list.add(westNode);
        }

        return list.stream().filter(Objects::nonNull).collect(Collectors.toSet());
    }

    /**
     * Resets the wanderer in initial state by clearing all datas
     * and initializing him
     */
    public void reset() {
        foundSolution = false;
        solutions.clear();
        numberOfSolutions = 0;
        init();
    }

    /**
     * Registers a solution path in the registry
     * @param path the path containing the coordinates of each node
     */
    private void registerSolution(List<String> path) {
        LoggingController.getLogger().log(Level.INFO, "Found solution!!!");
        LoggingController.getLogger().log(Level.FINER, constructDisplayableSolution(path));
        solutions.put(++numberOfSolutions, new ArrayList<String>(path.stream().collect(Collectors.toList())));
        if (!optimalSolution.isEmpty() && path.size() < optimalSolution.size()) {
            optimalSolution.clear();
            optimalSolution.addAll(path);
        } else if (optimalSolution.isEmpty()) {
            optimalSolution.addAll(path);
        }
    }

}
