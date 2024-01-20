package net.etravel.com.main;

import net.etravel.com.controllers.DisplayController;
import net.etravel.com.logger.LoggingController;
import net.etravel.com.resources.ResourceManager;
import net.etravel.com.controllers.Maze;
import net.etravel.com.controllers.Wanderer;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import java.util.logging.Level;

/**
 * Application Maze.
 * This app inits a GUI in a separate window in which user can interact with
 */
public class Main {
    private static Composite cmpMazeContainer;

    public static void main(String[] args) {

        // Initializing application window
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setSize(1200, 1200);
        shell.setLayout(new GridLayout(1, false));
        shell.setText("Maze");
        shell.setImage(ResourceManager.getImage("/icons/maze.png"));

        shell.addDisposeListener(disposeEvent -> {
            ResourceManager.dispose();
            LoggingController.getLogger().log(Level.INFO, "Resources disposed");
        });

        buildUI(shell);

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        display.dispose();

    }

    /**
     * Creates the initial UI using SWT lib
     *
     * @param parent the parent element on which the UI will be constructed
     */
    private static void buildUI(Shell parent) {
        Composite cmpMainContainer = new Composite(parent, SWT.NONE);
        GridLayout cmpMainContainerLayout = new GridLayout(1, true);
        cmpMainContainerLayout.verticalSpacing = 30;
        cmpMainContainer.setLayout(cmpMainContainerLayout);
        GridData cmpMainContainerData = new GridData(SWT.FILL, SWT.FILL, true, true);
        cmpMainContainer.setLayoutData(cmpMainContainerData);
        cmpMainContainer.setBackground(ResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        Composite cmpToolbarContainer = new Composite(cmpMainContainer, SWT.NONE);
        RowLayout cmpToolbarContainerLayout = new RowLayout(SWT.HORIZONTAL);
        cmpToolbarContainerLayout.spacing = 30;
        cmpToolbarContainer.setLayout(cmpToolbarContainerLayout);
        GridData cmpToolbarContainerData = new GridData(SWT.FILL, SWT.FILL, false, false);
        cmpToolbarContainer.setLayoutData(cmpToolbarContainerData);
        cmpToolbarContainer.setBackground(ResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        Label lblLoadSimulation = new Label(cmpToolbarContainer, SWT.NONE);
        lblLoadSimulation.setImage(ResourceManager.getImage("/icons/loadSimulation.png"));
        lblLoadSimulation.setToolTipText("Create a random maze!");

        Label lblStart = new Label(cmpToolbarContainer, SWT.NONE);
        lblStart.setImage(ResourceManager.getImage("/icons/start.png"));
        lblStart.setToolTipText("Start game!");

        Label lblSolutions = new Label(cmpToolbarContainer, SWT.NONE);
        lblSolutions.setImage(ResourceManager.getImage("/icons/solution.png"));
        lblSolutions.setToolTipText("Show Solutions!");

        cmpMazeContainer = new Composite(cmpMainContainer, SWT.NONE);
        GridLayout cmpMazeContainerLayout = new GridLayout(Maze.colSize, true);
        cmpMazeContainer.setLayout(cmpMazeContainerLayout);
        GridData cmpMazeContainerData = new GridData(SWT.FILL, SWT.FILL, true, true);
        cmpMazeContainer.setLayoutData(cmpMazeContainerData);
        cmpMazeContainer.setBackground(ResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        lblLoadSimulation.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent mouseEvent) {
                if (Wanderer.getWanderer().isCurrentlyExploring()) {
                    showErroeMessageWhileSimulationIsRunning();
                    return;
                }
                initializeGame();

            }
        });

        lblStart.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent mouseEvent) {
                if (!Maze.getMaze().isMazeConstructed()) {
                    showErroeMessageIfMazeIsNotConstructed();
                    return;
                }
                if (Wanderer.getWanderer().isCurrentlyExploring()) {
                    showErroeMessageWhileSimulationIsRunning();
                    return;
                }
                resetConditions();
                Wanderer.getWanderer().exploreMaze();
            }
        });

        lblSolutions.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent mouseEvent) {
                Shell shell = new Shell(Display.getCurrent().getActiveShell(), SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.MAX | SWT.RESIZE);
                shell.setText("Solutions Found");
                shell.setImage(ResourceManager.getImage("/icons/solution_small.png"));
                shell.setLayout(new GridLayout(1, false));
                Label lbl = new Label(shell, SWT.NONE);
                lbl.setText(Wanderer.getWanderer().getAllSolutions(true));
                shell.pack();
                shell.open();
            }
        });
    }

    /**
     * Initializes game by constructor the GUI of the maze, the Maze's data
     * and resetting our wanderer in the initial state inside the created maze
     */
    private static void initializeGame() {
        LoggingController.getLogger().log(Level.INFO, "Initializing game. . .");
        DisplayController.getDisplayController().initMazeGui(cmpMazeContainer);
        Maze.getMaze().initMaze();

        Wanderer.getWanderer().reset();
        LoggingController.getLogger().log(Level.INFO, "Game Initialized!");
    }

    /**
     * Resetting the game in order to start again
     */
    private static void resetConditions() {
        LoggingController.getLogger().log(Level.INFO, "Resetting conditions. . .");
        Maze.getMaze().resetTiles();
        Wanderer.getWanderer().reset();
        DisplayController.getDisplayController().resetGui();
        LoggingController.getLogger().log(Level.INFO, "Conditions resetted!");
    }

    /**
     * UI message & logging of error message when user presses play but the simulation is already started
     */
    private static void showErroeMessageWhileSimulationIsRunning() {
        MessageDialog.openError(Display.getDefault().getActiveShell(), "Start game", "Error! Game is already started. Wait to finish!");
        LoggingController.getLogger().log(Level.WARNING, "Error! Game is already started. Wait to finish!");
    }

    /**
     * UI message & logging of error message when forcing start of game before the maze is constructed
     */
    private static void showErroeMessageIfMazeIsNotConstructed() {
        MessageDialog.openError(Display.getDefault().getActiveShell(), "Start game", "Error! Maze has not been generated. You need to create a maze first!");
        LoggingController.getLogger().log(Level.WARNING, "Error! Maze has not been generated. You need to create a maze first!");
    }

}
