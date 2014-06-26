package MaliciousPageAnalysisEngine;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import util.LoggerUtilProject;
import util.PropertiesUtil;

/**
 * This class is responsible for detecting cloaking
 *
 * @version 1.3
 */
public class CloakingDetection implements Runnable {

    /**
     * This variable stores the url currently being checked for cloaking
     */
    private String url;
    /**
     * It is the Cloaking Detection Engine's connection to the Database
     */
    java.sql.Connection con;
    /**
     * Boolean value which tells if cloaking was seen at the current URL
     */
    private boolean cloakingDetected;
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
     * Constructor
     */
    public CloakingDetection() {
        initComponents();
    }

    @Override
    public void run() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            LoggerUtilProject.getLogger().severe("Thread interrupted while sleeping: " + ex.toString());
        }
        LoggerUtilProject.getLogger().info("The cloaking detection engine has started");
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
                    LoggerUtilProject.getLogger().severe("Cloaking Thread interrupted while Sleeping");
                }
            }
            fetchNextURL();
            checkCloaking();
            writeToDatabase();

            /*            
             if (idPointer == idUpperLimit) {
             checkDatabaseLimits();
             if (idPointer == idUpperLimit) {
             try {
             Thread.sleep(10000);//Pause for another 10 secs before resuming
             } catch (InterruptedException ex) {
             LoggerUtilProject.getLogger().severe("Thread interrupted while sleeping: " + ex.toString());
             }
             //break;//If after checking the database limits the upper limit is same as idPointer, then break and pause the engine
             }
             }*/
        }
    }

    /**
     * This method initializes all components at the start
     */
    private void initComponents() {
        try {
            con = DriverManager.getConnection(PropertiesUtil.databaseConnectivityString, PropertiesUtil.databaseUserName, PropertiesUtil.databaseUserPassword);
        } catch (SQLException ex) {
            LoggerUtilProject.getLogger().severe("SQL Exception thrown by Cloaking Detection Eninge while connecting to database: \t" + ex.toString());
        }
        pstmt = null;
        url = null;
        cloakingDetected = false;
        idPointer = 0;
        idLowerLimit = 0;
        idUpperLimit = 10;
    }

    /**
     * Runs the Fuzzy logic for checking cloaking
     */
    private void checkCloaking() {
        Document docChrome = null;
        Document docFirefox = null;
        Document docInternetExplorer = null;
        Document docGoogleBot = null;

        try {//Get the page content using the URL emulating a Chrome browser
            docChrome = Jsoup.connect(url).parser(Parser.xmlParser()).ignoreContentType(true).userAgent("Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36").get();
            //LoggerUtilProject.getLogger().log(Level.INFO, "Fetched URL for Cloaking emulating Internet Explorer: {0}", url);
        } catch (IOException ex) {
            LoggerUtilProject.getLogger().severe("Error Fetching url while Cloaking Check using Chrome UA:" + url + "\n" + ex.toString());
        }

        /*
        try {//Get the page content using the URL emulating a Firefox browser
            docFirefox = Jsoup.connect(url).parser(Parser.xmlParser()).ignoreContentType(true).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20120101 Firefox/29.0").get();
            //LoggerUtilProject.getLogger().log(Level.INFO, "Fetched URL for Cloaking emulating Internet Explorer: {0}", url);
        } catch (IOException ex) {
            LoggerUtilProject.getLogger().severe("Error Fetching url while Cloaking Check using Firefox UA:" + url + "\n" + ex.toString());
        }
        */

        try {//Get the page content using the URL emulating a Internet Explorer browser
            docInternetExplorer = Jsoup.connect(url).parser(Parser.xmlParser()).ignoreContentType(true).userAgent("Mozilla/5.0 (Windows; U; MSIE 9.0; WIndows NT 9.0; en-US))").get();
            //LoggerUtilProject.getLogger().log(Level.INFO, "Fetched URL for Cloaking emulating Internet Explorer: {0}", url);
        } catch (IOException ex) {
            LoggerUtilProject.getLogger().severe("Error Fetching url while Cloaking Check using InternetExplorer UA:" + url + "\n" + ex.toString());
        }
        try {//Get the page content emulating google bot
            docGoogleBot = Jsoup.connect(url).parser(Parser.xmlParser()).ignoreContentType(true).userAgent("Googlebot/2.1 (+http://www.googlebot.com/bot.html)").get();
            //LoggerUtilProject.getLogger().log(Level.INFO, "Fetched URL for Cloaking emulating Internet Explorer: {0}", url);
        } catch (IOException ex) {
            LoggerUtilProject.getLogger().severe("Error Fetching url while Cloaking Check using Google Bot UA:" + url + "\n" + ex.toString());
        }
        try {
            if (docChrome.outerHtml().contentEquals(docInternetExplorer.outerHtml()) && docChrome.outerHtml().contentEquals(docGoogleBot.outerHtml())) {
                cloakingDetected = false;
                LoggerUtilProject.getLogger().info("No Cloaking Seen for URL: " + url);
            } else {
                cloakingDetected = true;
                LoggerUtilProject.getLogger().info("Cloaking Detected at URL: " + url);
            }
        } catch (Exception ex) {
            LoggerUtilProject.getLogger().severe("Severe Error while checking for cloaking: " + ex.toString());
        }
    }

    /**
     * Checks the Upper limit 'id' and the lower limit 'id' from the database
     */
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
            LoggerUtilProject.getLogger().severe("Error while checking Database Limits for Cloaking Engine: "+ex.toString());
        }
    }

    /**
     * This function get the URL from the database which needs to be checked.
     */
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
            LoggerUtilProject.getLogger().severe("Exception thrown wile getting URL for cloaking detection engine:"+ex.toString() );
        }
    }

    /**
     * This function is used for writing to database the status of the Cloaking
     * found for a URL
     */
    private void writeToDatabase() {
        try {
            pstmt = con.prepareStatement("UPDATE repository.webpage SET cloaking_detected=? WHERE url=?");
            pstmt.setBoolean(1, cloakingDetected);
            pstmt.setString(2, url);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            LoggerUtilProject.getLogger().severe("Error writing to database in cloaking engine: " + ex.toString());
        }
    }

    /**
     * Destructor called to dispose resources left open
     *
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        LoggerUtilProject.getLogger().severe("Cloaking Detection Engine is Shuting Down");
        try {
            if (pstmt != null) {
                pstmt.close();
            }
            if (con != null) {
                con.close();
            }
        } catch (SQLException ex) {
            LoggerUtilProject.getLogger().log(Level.SEVERE, "Exception thrown while closing the cloaking detection engine:\t{0}", ex.toString());
        }
        super.finalize();

    }
}
