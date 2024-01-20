package net.etravel.com.controllers;

import javafx.beans.binding.MapExpression;
import net.etravel.com.properties.PropertyReader;
import net.etravel.com.resources.ResourceManager;
import net.etravel.com.structure.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton controller responsible for all UI operations
 */
public class DisplayController {

    // singleton
    private final static DisplayController instance = new DisplayController();
    /**
     * The GUI represantation of the maze
     */
    private final ConcurrentHashMap<String, Label> guiNodes;
    /**
     * the parent UI component of maze
     */
    private Composite guiCanvas = null;

    private String previousPaintedNodeKey = "";

    /**
     * Predefined colors of tiles
     */
    private final Color startPointColor = ResourceManager.getColor(245, 155, 66);
    private final Color endingPointColor = ResourceManager.getColor(135, 245, 66);
    private final Color visitedTileColor = ResourceManager.getColor(134, 163, 147);


    private DisplayController() {
        guiNodes = new ConcurrentHashMap<>();
    }

    public static DisplayController getDisplayController() {
        return instance;
    }


    /**
     * Initializes the GUI of maze and storing each node in a map structure
     * @param parent The UI component on which the maze will be painted
     */
    public void initMazeGui(Composite parent) {
        Display.getDefault().syncExec(() -> {
            guiCanvas = parent;
            disposeMazeGui();
            int col  = 0;
            int row = 0;
            for(int i = 0; i < Maze.matrixSize; i++) {

                if(col == Maze.colSize) {
                    col = 0;
                    row++;
                }
                Label lbl = new Label(parent, SWT.BORDER);
                GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
                data.grabExcessVerticalSpace = true;
                lbl.setLayoutData(data);
                lbl.setAlignment(SWT.CENTER);
                lbl.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
                lbl.setText(row + "|" + col);
                lbl.setData(row + "|" + col);
                guiNodes.put((String) lbl.getData(), lbl);
                col++;
            }
            parent.redraw();
            parent.layout();
        });

    }

    /**
     * Paints the node
     * @param node the node that should be updated/painted
     */
    public void updateGuiNode(Node node){
        Label guiNode = guiNodes.get(node.getPoint().getKeyLocation());
        if(guiNode != null) {
            if(node instanceof Obstacle) {
                guiNode.setBackground(ResourceManager.getColor(SWT.COLOR_BLACK));
            } else if(node instanceof StartingNode) {
                guiNode.setBackground(startPointColor);
            } else if(node instanceof EndingNode) {
                guiNode.setBackground(endingPointColor);
            }
            guiNode.redraw();
        }

    }

    /**
     * Paint the current tile that the algorithm is visiting (asynchronously)
     * @param currentNode the node that is currently visited
     */
    public void paintVisitedTile(Node currentNode) {
        // Paint the current visited Tile with a unique color (useful for simulation)
        Display.getDefault().asyncExec(() -> {
            Label guiNode = guiNodes.get(currentNode.getPoint().getKeyLocation());
            if(guiNode != null) {
                if (currentNode instanceof Tile && !currentNode.isEndingPoint() && !currentNode.isStartingPoint()) {
                    guiNode.setBackground(visitedTileColor);
                } else if(currentNode.isEndingPoint()) {
                    guiNode.setBackground(endingPointColor);
                } else if(currentNode.isStartingPoint()) {
                    guiNode.setBackground(startPointColor);
                }
                guiNode.redraw();
                guiNode.update();
            }

            Label previousGuiNode = guiNodes.get(previousPaintedNodeKey);
            if(previousGuiNode != null) {
                Node previousNode = Maze.getMaze().getNode(previousPaintedNodeKey);
                if (previousNode instanceof Tile && !previousNode.isEndingPoint() && !previousNode.isStartingPoint()) {
                    previousGuiNode.setBackground(ResourceManager.getColor(SWT.COLOR_GRAY));
                } else if(previousNode.isEndingPoint()) {
                    previousGuiNode.setBackground(endingPointColor);
                } else if(previousNode.isStartingPoint()) {
                    previousGuiNode.setBackground(startPointColor);
                }
                previousGuiNode.redraw();
                previousGuiNode.update();
            }

            previousPaintedNodeKey = currentNode.getPoint().getKeyLocation();
        });
    }

    public void disposeMazeGui() {
        Display.getDefault().syncExec(() -> {
            guiNodes.values().stream().forEach(Label::dispose);
            guiNodes.clear();
            guiCanvas.redraw();
        });

    }

    public void paintOptimalSolution(List<String> optimalSolution) {
        Display.getDefault().asyncExec(() -> {
            if(PropertyReader.getInstance().getBoolean("show_optimal_path", true)) {
                Color whiteColor = ResourceManager.getColor(SWT.COLOR_WHITE);
                guiCanvas.setRedraw(false);
                guiNodes.values().stream().forEach(l -> {
                    Node node = Maze.getMaze().getNode((String) l.getData());
                    if (node instanceof Tile && !node.isStartingPoint() && !node.isEndingPoint()) {
                        l.setBackground(whiteColor);
                    }
                });

                for (int i = 1; i < optimalSolution.size() - 1; i++) {
                    guiNodes.get(optimalSolution.get(i)).setBackground(visitedTileColor);
                }
                guiCanvas.setRedraw(true);
            }
        });

    }

    public void resetGui() {
        previousPaintedNodeKey = "";
        Display.getCurrent().asyncExec(() -> {
            guiNodes.values().stream().forEach(l -> {
                Node node = Maze.getMaze().getNode((String) l.getData());
                if (node instanceof Tile && !node.isStartingPoint() && !node.isEndingPoint()) {
                    l.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
                    l.redraw();
                }
            });
        });
    }

    public void showResultMessage(final boolean foundSolution, final Map<Integer, List<String>> solutions, final List<String> optimalSolution) {
        String str = "";
        if (foundSolution) {

            if(solutions.keySet().size() > 1) {
                str += "Found " + solutions.keySet().size() + " solutions";
                str += "\nOptimal solution is: ";
                str += Wanderer.getWanderer().constructDisplayableSolution(optimalSolution);
            } else {
                str += "Found 1 Solution";
                str += "!\nOptimal solution is: ";
                str += Wanderer.getWanderer().constructDisplayableSolution(optimalSolution);
            }
        } else {
            str += "No solution was found for this maze";
        }
        String finalStr = str;
        Display.getDefault().asyncExec(() ->
                MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Game Status Report", finalStr));
    }
}
