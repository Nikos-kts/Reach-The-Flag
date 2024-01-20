import net.etravel.com.controllers.DisplayController;
import net.etravel.com.controllers.Maze;
import net.etravel.com.controllers.Wanderer;
import net.etravel.com.logger.LoggingController;
import net.etravel.com.properties.PropertyReader;
import org.eclipse.core.runtime.Assert;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TestMain {
    Logger logger = Logger.getLogger(TestMain.class.getName());

    @Test
    void testConfiguration() {
        logger.info("Checking configuration");
        int rowSize = PropertyReader.getInstance().getInteger("matrix_row", 2);
        Assert.isLegal(rowSize >=2 && rowSize < 50, "Maze size should be less than 50 for not being too slow");
        int colSize = PropertyReader.getInstance().getInteger("matrix_col", 2);
        Assert.isLegal(colSize >=2 && colSize < 50, "Maze size should be less than 50 for not being too slow");
        Assert.isLegal(colSize * rowSize < 300, "Maze size should be around 300 because UI does long time to paint");
        long simulation = PropertyReader.getInstance().getInteger("simulation_step_time", 0);
        Assert.isLegal(simulation >= 0, "Should be 0 to run instantly or greater to run as a simulation (better for small mazes)");
        double ratio = PropertyReader.getInstance().getDouble("obstacle_ratio", 0);
        Assert.isLegal(ratio < 1.0 && ratio > 0.0, "Should be a percentage of difficulty (obstacle appearance) in the range [0,1)");
        String levelStr = PropertyReader.getInstance().getProperty("log_level", "INFO");
        Level level = Level.parse(levelStr);
        Assert.isNotNull(level);
        Boolean optimal_path = PropertyReader.getInstance().getBoolean("show_optimal_path", true);
        Assert.isNotNull(optimal_path);


        Maze.getMaze().initMaze();
        Wanderer.getWanderer().reset();
    }

    @Test
    void checkSingletons() {
        logger.info("Checking singletons. . .");
        Assert.isNotNull(LoggingController.getLogger());
        Assert.isNotNull(Maze.getMaze());
        Assert.isNotNull(Wanderer.getWanderer());
        Assert.isNotNull(DisplayController.getDisplayController());
        Assert.isNotNull(PropertyReader.getInstance());
        logger.info("Singletons OK!");
    }

}
