package util;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * This class provides logging capability to Log File and to Console.
 *
 * @version 1.0
 */
public class LoggerUtilProject {

    /**
     * The Name of the Logger.
     */
    private static final String LOGGERNAME = "project.logging";
    /**
     * This stores a strong reference to the logger.
     */
    private static final Logger logger = Logger.getLogger(LOGGERNAME);

    static {
        try {
            logger.setUseParentHandlers(false);
            final Handler ch = new ConsoleHandler();
            logger.addHandler(ch);

            final Handler fh = new FileHandler("logfile.log");
            final SimpleFormatter sf = new SimpleFormatter();
            fh.setFormatter(sf);
            logger.addHandler(fh);

            setHandlersLevel(Level.ALL);
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(LoggerUtilProject.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * *
     * Sets the Level of the handlers (File or console handlers) in the Logger
     *
     * @param Level Defines the Level of logging to be enabled
     */
    public static void setHandlersLevel(Level level) {
        Handler[] handlers = logger.getHandlers();
        for (Handler h : handlers) {
            h.setLevel(level);
        }
        logger.setLevel(level);
    }

    /**
     * Getter for the class
     *
     * @return Logger Returns the Logger of the utility
     */
    public static Logger getLogger() {
        return logger;
    }

    /**
     * This method will be used to close FileHandler while disposing off in
     * garbage collector
     *
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        Handler handlers[] = logger.getHandlers();
        for (Handler h : handlers) {
            h.close();
        }
        super.finalize();
    }
}
