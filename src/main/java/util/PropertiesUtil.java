/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

/**
 * This class load the Properties from the Properties File- "config.prop".
 *
 */
public class PropertiesUtil {

    public static Properties prop = new Properties();
    public static String databaseConnectivityString = null;
    public static String databaseUserName = null;
    public static String databaseUserPassword = null;
    public static String initialSeed = null;

    static {
        try {
            prop.load(new FileReader(new File("config.prop")));
            databaseConnectivityString = prop.getProperty("databaseConnectivityString");
            databaseUserName = prop.getProperty("databaseUserName");
            databaseUserPassword = prop.getProperty("databaseUserPassword");
            initialSeed = prop.getProperty("initialSeed");
        } catch (IOException | NumberFormatException ex) {
            LoggerUtilProject.getLogger().log(Level.SEVERE, "Problem loading or Reading Configuration "
                    + "file:\t {0}", ex.toString());
        }
    }
}
