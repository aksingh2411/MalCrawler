package CrawlingParsingEngine;

import ControlInterface.GUI;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import util.LoggerUtilProject;
import util.PropertiesUtil;

/**
 * This Class is responsible for the task of parsing web pages.
 *
 * @version 1.3
 */
public class ParsingEngine implements Runnable{

    /**
     * This variable stores the URL being parsed
     */
    private String url;
    /**
     * It is the Crawling Engine's connection to the Database
     */
    java.sql.Connection con;
    /**
     * Prepared Statement which is used for reading and writing to the Postgres
     * Database
     */
    PreparedStatement pstmt;
    /**
     * A flag which when true will start the Engine Shutdown
     */
    boolean engineShutdown;
    /**
     * This semaphore is used to indicate that the engine is ready to process web page
     */
     boolean readyToProcess;
     /**
      * This semaphone tells whether the engine is processing a page or not
      */
     boolean processing;
     
    /**
     * Constructor
     */
    ParsingEngine() {
        initComponents(); // Initalize all components
    }

    /**
     * Initializes all Components
     */
    private void initComponents() {
        url = null;
        engineShutdown = false;
        processing =false;
        try {
            con = DriverManager.getConnection(PropertiesUtil.databaseConnectivityString, PropertiesUtil.databaseUserName, PropertiesUtil.databaseUserPassword);
            //con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/MalwareMining", "postgres", "abc123");
        } catch (SQLException ex) {
            LoggerUtilProject.getLogger().severe("SQL Exception thrown while connecting to database: \t" + ex.toString());
            GUI.jTextAreaParserLog.append("\nSEVERE: SQL Exception thrown while connecting to database: \t" + ex.toString());            
        }
        pstmt = null;
    }

    /**
     * Processes the Page Downloaded. It does two things <ul><li>Extracts
     * Links.</li>
     * <li>Saves page and other values to database.</li></ul>
     *
     * @param url The URL of the page
     * @param Document The page fetched and saved as Document
     */
    public void processPage(String url, Document doc) {
        processing = true;
        if (doc != null) {
            Elements links = doc.select("a[href]");
            for (Element link : links) {//Extract links from page fetched & add them to urlToCrawl
                String linkRef = link.attr("abs:href");
                UrlValidator validator = new UrlValidator();
                if (!CrawlerEngine.urlsCrawled.contains(linkRef) && validator.isValid(linkRef) && linkRef != null) {
                    CrawlerEngine.urlsToCrawl.add(linkRef);//Add to Crawl List after checking for already crawled, null and invalid URLs
                    LoggerUtilProject.getLogger().log(Level.INFO, "New URL added to Crawl List: {0}", linkRef);
                    GUI.jTextAreaSeedingEngineLog.append("\nINFO: New URL added to Crawl List:" + linkRef);
                }
            }
            try {//Save the page and other details in the Database
                MessageDigest md = MessageDigest.getInstance("MD5");//Digest for computing Hash
                //pstmt = con.prepareStatement("INSERT INTO repository.webPage(\"url\",\"pageHash\",\"IPAddress\",\"pageContent\",\"visitTime\",\"revisitRequired\") VALUES(?,?,?,?,?,?);");
                pstmt = con.prepareStatement("INSERT INTO repository.webpage(url,page_hash,ip_address,page_content,visit_time,revisit_required) VALUES(?,?,?,?,?,?);");
                pstmt.setString(1, url);
                pstmt.setString(2, String.format("%1$032x", new BigInteger(1, md.digest(doc.outerHtml().getBytes("UTF-8")))));
                pstmt.setString(3, java.net.InetAddress.getByName(url.replace("http://", "").replace("https://", "").replace("ftp://", "").split("/")[0]).getHostAddress());//Getting the IP Address of the Web Server
                pstmt.setString(4, doc.outerHtml());
                pstmt.setString(5, (new SimpleDateFormat("YYYY-MM-dd HH:mm:ss")).format(Calendar.getInstance().getTime()));
                pstmt.setBoolean(6, false);
                pstmt.executeUpdate();
            } catch (NoSuchAlgorithmException | UnknownHostException | UnsupportedEncodingException | SQLException ex) {
                LoggerUtilProject.getLogger().log(Level.SEVERE, "Error in Writing Page Details to Database:\t{0}", ex.toString());
                GUI.jTextAreaParserLog.append("\nSEVERE: Error in Writing Page Details to Database:\t{" + ex.toString());
            }
        }
        processing =false;
    }

    /**
     * Run method which starts and tracks all page processing activities
     */
    @Override
    public void run() {
        LoggerUtilProject.getLogger().info("The Crawler Guidance Engine has started");
        GUI.jTextAreaCrawlerLog.append("/n INFO: The crawler Guidance Engine has started");       
    }
}
