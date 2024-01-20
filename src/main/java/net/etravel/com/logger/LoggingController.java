package net.etravel.com.logger;

import net.etravel.com.properties.PropertyReader;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingController {

    private static final LoggingController instance = new LoggingController();
    private static Logger logger;
    static boolean isInitialized;


    private LoggingController(){
    }

    private static void init(){
        isInitialized = true;
        logger = Logger.getLogger((instance.getClass().getSimpleName()));
        Level log_level = Level.parse(PropertyReader.getInstance().getProperty("log_level", Level.INFO.toString()));
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(log_level);
        logger.addHandler(consoleHandler);
        logger.setLevel(log_level);
        logger.setUseParentHandlers(false);
    }
    public static LoggingController getLogger() {
        if(!isInitialized){
            init();
        }
        return instance;
    }

    public void log(Level level, String log){
        logger.log(level, log);
    }

}
