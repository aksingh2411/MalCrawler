package MaliciousPageAnalysisEngine;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import util.LoggerUtilProject;
import util.PropertiesUtil;

/**
 * This class is responsible for checking re-direction of a URL We record the
 * number of times the browser is redirected to a different URI, for example, by
 * responses with HTTP status 302 or by the setting of specific JavaScript
 * properties, e.g., document.location. We also keep track of the targets of
 * each redirection, to identify redirect chains that involve an unusually-large
 * number of domains. The HTTP status 302 re-direction can be checked by the
 * response received by the browser (in our case the HTML Unit emulated
 * browser). And, document.location based re-direct can be picked up by running
 * the script using Rhino. Thus, this feature can be picked up as described
 * above, and this feature will have nominal with values {Yes, No}.
 *
 * @version 1.3
 */
public class RedirectionDetection implements Runnable {

    /**
     * This variable stores the url currently being checked for redirection
     */
    private String url;
    /**
     * It is the Redirection Detection Engine's connection to the Database
     */
    java.sql.Connection con;
    /**
     * Boolean value which tells if Redirection was seen at the current URL
     */
    private boolean redirectionDetected;
    /**
     * Prepared Statement which is used for reading and writing to the Postgres
     * Database
     */
    PreparedStatement pstmt;
    /**
     * ID pointer to identify the URL in the database
     */
    private int idPointer;
    /**
     * Lower limit of the database value of field 'id'
     */
    private int idLowerLimit;
    /**
     * Upper Limit of the database value of field 'id'
     */
    private int idUpperLimit;

    /**
     * Constructor of the Class
     */
    public RedirectionDetection() {
        initComponents();
    }

    @Override
    public void run() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            LoggerUtilProject.getLogger().severe("Redirection Detection Thread interrupted while sleeping: " + ex.toString());
        }
        LoggerUtilProject.getLogger().info("The Redirection detection engine has started");
        checkDatabaseLimits();
        //System.out.println("/nLower limit is : " + idLowerLimit);
        //System.out.println("/nUpper Limit is :" + idUpperLimit);
        idPointer = idLowerLimit;
        while (idPointer <= idUpperLimit) {
            while (idPointer == idUpperLimit) {
                try {
                    Thread.sleep(5000);//Wait for 5 secs for more records to be added for check
                    checkDatabaseLimits();
                } catch (InterruptedException ex) {
                    LoggerUtilProject.getLogger().severe("Redirection Detection Thread interrupted while Sleeping");
                }
            }
            fetchNextURL();
            checkRedirection();
            writeToDatabase();
        }
    }

    private void checkDatabaseLimits() {
        try {
            Statement stmt;
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = stmt.executeQuery("SELECT id from repository.webpage order by id");
            rs.first();
            idLowerLimit = rs.getInt("id");
            rs.last();
            idUpperLimit = rs.getInt("id");
        } catch (SQLException ex) {
            LoggerUtilProject.getLogger().severe("Error while checking Database Limits for Redirection Engine: "+ex.toString());
        }
    }

    private void fetchNextURL() {
        try {
            pstmt = con.prepareStatement("SELECT url from repository.webpage where id=?;");
            pstmt.setInt(1, idPointer++);
            java.sql.ResultSet rs = pstmt.executeQuery();
            while (!rs.next()) {
                //idPointer++;
                pstmt.setInt(1, idPointer++);
                rs = pstmt.executeQuery();
            }
            url = rs.getString("url");
            //System.out.println(url);
        } catch (SQLException ex) {
            LoggerUtilProject.getLogger().severe("Sql exception thrown while fetching URL for redirection engine: "+ ex.toString());
        }
    }

    /**
     * Fuzzy Logic which will check for re-direction
     */
    private void checkRedirection() {
        try {//Get the page content to check for 301 & 302 redirection
            Response urlResponse = Jsoup.connect(url).followRedirects(false).execute();
            //LoggerUtilProject.getLogger().log(Level.INFO, "Fetched URL for redirection check: {0}", url);
            if (urlResponse.statusCode() == 301 || urlResponse.statusCode() == 302) {
                redirectionDetected = true;
            } else if (urlResponse.statusCode() == 200) {
                redirectionDetected = false;
            } else {
                redirectionDetected = true;//Mark true even if some weird status code returned apart from 200
                LoggerUtilProject.getLogger().info("Weird status code" + urlResponse.statusCode() + "seen at url: " + url);
            }
        } catch (Exception ex) {
            LoggerUtilProject.getLogger().severe("Error Fetching url while re-direction check" + url + "\n" + ex.toString());
        }
    }

    /**
     * This function writes the result of redirection check to the database
     */
    private void writeToDatabase() {
        try {
            pstmt = con.prepareStatement("UPDATE repository.webpage SET redirection_detected=? WHERE url=?");
            pstmt.setBoolean(1, redirectionDetected);
            pstmt.setString(2, url);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            LoggerUtilProject.getLogger().severe("Error writing to database in Redirection engine: " + ex.toString());
        }
    }

    /**
     * This function initializes all variables
     */
    private void initComponents() {
        try {
            con = DriverManager.getConnection(PropertiesUtil.databaseConnectivityString, PropertiesUtil.databaseUserName, PropertiesUtil.databaseUserPassword);
        } catch (SQLException ex) {
            LoggerUtilProject.getLogger().severe("SQL Exception thrown by Redirection Detection Eninge while connecting to database: \t" + ex.toString());
        }
        pstmt = null;
        url = null;
        redirectionDetected = false;
        idPointer = 0;
        idLowerLimit = 0;
        idUpperLimit = 10;
    }

    /**
     * Destructor called to dispose resources left open
     *
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        LoggerUtilProject.getLogger().severe("Redirection Detection Engine is Shuting Down");
        try {
            if (pstmt != null) {
                pstmt.close();
            }
            if (con != null) {
                con.close();
            }
        } catch (SQLException ex) {
            LoggerUtilProject.getLogger().log(Level.SEVERE, "Exception thrown while closing the Redirection detection engine:\t{0}", ex.toString());
        }
        super.finalize();

    }

    /* Only for the purpose of testing. Needs to be integrated with JUnit Test Framework
    public static void main(String[] args) {
     Thread t = new Thread (new RedirectionDetection());
     t.start();
     
     }*/
}