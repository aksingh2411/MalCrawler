package MaliciousPageAnalysisEngine;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import util.LoggerUtilProject;
import util.PropertiesUtil;
import MaliciousPageAnalysisEngine.HTMLUnitBrowserEmulationEngine;
import java.util.logging.Logger;

/**
 * This class uses static heuristics to decide whether a webpage is likely to be
 * malicious or not.
 *
 * @version 1.3
 */
public class StaticHeuristics implements Runnable {

    /**
     * This variable stores the url currently being checked for maliciousness
     */
    private String url;
    /**
     * It is the maliciousness Detection Engine's connection to the Database
     */
    java.sql.Connection con;
    /**
     * Boolean value which tells if Maliciousness was seen at the current URL
     */
    private boolean maliciousness;
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
    public StaticHeuristics() {
        initComponents();
    }

    /**
     * This is the method which runs when the thread starts
     */
    @Override
    public void run() {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException ex) {
            LoggerUtilProject.getLogger().severe("Maliciousness Check Thread interrupted while sleeping: " + ex.toString());
        }
        LoggerUtilProject.getLogger().info("The Maliciousness detection engine has started");
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
                    LoggerUtilProject.getLogger().severe("Malicoiusness Detection Thread interrupted while Sleeping");
                }
            }
            fetchNextURL();
            checkMaliciousness();
            writeToDatabase();
            try {
                Thread.sleep(4000);
            } catch (InterruptedException ex) {
                LoggerUtilProject.getLogger().severe("Thread interrupted while sleeping");
            }
        }
    }

    /**
     * This function initializes all components
     */
    private void initComponents() {
        try {
            con = DriverManager.getConnection(PropertiesUtil.databaseConnectivityString, PropertiesUtil.databaseUserName, PropertiesUtil.databaseUserPassword);
        } catch (SQLException ex) {
            LoggerUtilProject.getLogger().severe("SQL Exception thrown by Redirection Detection Eninge while connecting to database: \t" + ex.toString());
        }
        pstmt = null;
        url = null;
        maliciousness = false;
        idPointer = 0;
        idLowerLimit = 0;
        idUpperLimit = 10;
    }

    /**
     * This checks the limits of the database for analysis It uses the id column
     * to make the judgement
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
            LoggerUtilProject.getLogger().severe("Error while checking Database Limits for Maliciousness Detection Engine: " + ex.toString());
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
            LoggerUtilProject.getLogger().severe("Sql exception thrown while fetching URL for Maliciousness Detection engine: " + ex.toString());
        }
    }

    /**
     * Static Heuristics Logic which will check for maliciousness
     */
    private void checkMaliciousness() {
        try {
            int maliciousnessScore = 0;
            pstmt = con.prepareStatement("SELECT redirection_detected,cloaking_detected from repository.webpage where url=?;");
            pstmt.setString(1, url);
            java.sql.ResultSet rs = pstmt.executeQuery();
            rs.next();
            boolean redirectionDetected = rs.getBoolean("redirection_detected");
            boolean cloakingDetected = rs.getBoolean("cloaking_detected");

            if (redirectionDetected) {
                maliciousnessScore += 2.5;
            }
            if (cloakingDetected) {
                maliciousnessScore += 1.5;
            }
            int l = HTMLUnitBrowserEmulationEngine.lengthOfJavaScript(url);
            if (1 < l && l < 20) {
                maliciousnessScore += 1;
            }
            if (19 < l && l < 200) {
                maliciousnessScore += 2;
            }
            if (199 < l) {
                maliciousnessScore += 3.5;
            }
            if (0 < HTMLUnitBrowserEmulationEngine.getNoOfEval(url)) {
                maliciousnessScore += 4;
            }
            if (0 < HTMLUnitBrowserEmulationEngine.noOfDocumentWrites(url)) {
                maliciousnessScore += 3.5;
            }
            //System.out.println("Checking maliciousness of : " + url + " malicousness score="+maliciousnessScore);


            if (maliciousnessScore > 10) {
                maliciousness = true;
            } else {
                maliciousness = false;
            }
        } catch (SQLException ex) {
            LoggerUtilProject.getLogger().severe("Sql exception thrown while fetching URL for Maliciousness Detection engine: " + ex.toString());
        }
    }

    /**
     * This function writes the result of maliciousness check to the database
     */
    private void writeToDatabase() {
        try {
            pstmt = con.prepareStatement("UPDATE repository.webpage SET maliciousness_found=? WHERE url=?");
            pstmt.setBoolean(1, maliciousness);
            pstmt.setString(2, url);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            LoggerUtilProject.getLogger().severe("Error writing to database in Maliciousness Detection engine: " + ex.toString());
        }
    }

    /**
     * Destructor called to dispose resources left open
     *
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        LoggerUtilProject.getLogger().severe("Maliciousness Detection Engine is Shuting Down");
        try {
            if (pstmt != null) {
                pstmt.close();
            }
            if (con != null) {
                con.close();
            }
        } catch (SQLException ex) {
            LoggerUtilProject.getLogger().log(Level.SEVERE, "Exception thrown while closing the Maliciousness detection engine:\t{0}", ex.toString());
        }
        super.finalize();

    }

    //Only for the purpose of testing. Needs to be integrated with JUnit Test Framework
    public static void main(String[] args) {
        Thread t = new Thread(new StaticHeuristics());
        t.start();

    }
}
