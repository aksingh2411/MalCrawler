package CrawlingParsingEngine;

import ControlInterface.GUI;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import util.LoggerUtilProject;
import util.PropertiesUtil;
import org.apache.commons.validator.routines.UrlValidator;

/**
 * This class implements the Core Crawler Engine.
 *
 * @version 1.0
 */
public class CrawlerEngine implements Runnable {

    /**
     * URLs which are going to be crawled. Try to encapsulate above using
     * Collections SysnchronizedList to handle concurrency.
     */
    public static ArrayList<String> urlsToCrawl;
    /**
     * URLs which have been crawled, including which were crawled earlier and
     * saved it database
     */
    public static ArrayList<String> urlsCrawled;
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
     * The constructor
     */
    public CrawlerEngine() {
        if (initComponents()) {
            LoggerUtilProject.getLogger().info("Components of the Crawler Engine Initialized");
            GUI.jTextAreaCrawlerLog.append("\nINFO: Components of the Crawler Engine Initialized");
            GUI.jTextAreaParserLog.append(("\nINFO: Components of the Parser Engine Initalized"));
            GUI.jTextAreaSeedingEngineLog.append("\nINFO: Components of the Seeding Engine Initialized");
        } else {// Close application if the Initalization Fails
            JOptionPane.showMessageDialog(null, " Database Connection Failed.\n Shutting Down "
                    + "Appliccation.\n Check Database Connections Settings & Restart.\n Check "
                    + "'logfile.log' for More Details", "Error!", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    /**
     * Initializes all crawler components before run
     *
     * @return boolean True means initialization successful, false means
     * unsuccessful.
     */
    private boolean initComponents() {
        urlsToCrawl = new ArrayList<String>();
        urlsCrawled = new ArrayList<String>();
        try {
            con = DriverManager.getConnection(PropertiesUtil.databaseConnectivityString, PropertiesUtil.databaseUserName, PropertiesUtil.databaseUserPassword);
            //con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/MalwareMining", "postgres", "abc123");
        } catch (SQLException ex) {
            LoggerUtilProject.getLogger().severe("SQL Exception thrown while connecting to database: \t" + ex.toString());
            GUI.jTextAreaCrawlerLog.append("\nSEVERE: SQL Exception thrown while connecting to database: \t" + ex.toString());
            return false;
        }
        pstmt = null;
        engineShutdown = false;
        return true;
    }

    /**
     * Includes the code to be run in the Thread of Crawling Engine.
     */
    @Override
    public void run() {
        LoggerUtilProject.getLogger().info("The Crawling Engine Thread Started");
        GUI.jTextAreaCrawlerLog.append("\nINFO: The Crawling Engine Thread Started");
        try {
            addURLsToCrawl();//Fill urls in the urlsToCrawl FIFO queue
            loadURLsCrawled();//Load list of urls already crawled from the database
            while (!GUI.internetConnected) {//If Internet is not connected then Crawler Start will be delayed
                Thread.sleep(4000);
                LoggerUtilProject.getLogger().warning("Crawler Waiting for Internet Connectivity to Start");
                GUI.jTextAreaCrawlerLog.append("\nWARNING: Crawler Waiting for Internet Connectivity to Start");
            }
            LoggerUtilProject.getLogger().info("Internet Connectivity Detected - Crawler Starts");
            GUI.jTextAreaCrawlerLog.append("\nINFO: Internet Connectivity Detected - Crawler Starts");
            while (!engineShutdown) {
                if (urlsToCrawl.isEmpty()) {
                    LoggerUtilProject.getLogger().severe("There are No URLs to Crawl in the List");
                    GUI.jTextAreaCrawlerLog.append("\nSEVERE: There are No URLs to Crawl in the List");
                    GUI.jTextAreaSeedingEngineLog.append("\nSEVERE: The seeds have finished, there are no more URLs left");
                    break;
                }
                String url = urlsToCrawl.get(0);//Pick up the URL first in the queue
                try {
                    UrlValidator validator = new UrlValidator();//Validator for checking malformed URLs
                    if (url != null && validator.isValid(url) && !urlsCrawled.contains(url)) {
                        Document doc = getPage(url);//Fetch page
                        processPage(url, doc);//Process page fetched
                        urlsCrawled.add(url);//The URL crawled recently is added to urlsCrawled
                    } else {//Log the error for null or invalid URL
                        if (!urlsCrawled.contains(url)) {
                            LoggerUtilProject.getLogger().severe((url == null) ? "URL is NULL" : "URL is invalid");
                            GUI.jTextAreaCrawlerLog.append("\nSEVERE: URL is NULL and Invalid");
                        }
                    }
                } catch (Exception ex) {
                    LoggerUtilProject.getLogger().log(Level.SEVERE, "Error in Fetching or Processing Page:\t{0}", ex.toString());
                    GUI.jTextAreaParserLog.append("\nSEVERE: Error in Fetching or Processing Page:\t " + ex.toString());
                } finally {
                    urlsToCrawl.remove(0);//Remove the URL crawled, which is first in the queue, after it is processed   
                }
            }
        } catch (Exception ex) {
            LoggerUtilProject.getLogger().log(Level.SEVERE, "Crawling Engine Thread Shutting Down:/t{0}", ex.toString());
            GUI.jTextAreaCrawlerLog.append("SEVERE: Crawling Engine Thread Shutting Down:/t" + ex.toString());
            GUI.jTextAreaParserLog.append("SEVERE: Parsing Engine Closing");
            GUI.jTextAreaSeedingEngineLog.append("Seeding Engine Closing. Seeding will stop now.");
        } finally {
            gracefulShutdown();
        }
    }

    private void loadURLsCrawled() {
        LoggerUtilProject.getLogger().info("URLs Crawled Earlier Loaded from Database");
        GUI.jTextAreaSeedingEngineLog.append("\nINFO: URLs Crawled Earlier Loaded from Database");
    }

    /**
     * This function will add URLS to urlsToCrawl Array List.
     */
    private void addURLsToCrawl() {
        String[] initialSeedArray = PropertiesUtil.initialSeed.split(";");// Loading initial seeds from the config.prop file
        urlsToCrawl.addAll(Arrays.asList(initialSeedArray));
        LoggerUtilProject.getLogger().info("URLs added for crawl");
        GUI.jTextAreaSeedingEngineLog.append("\nINFO: URLs added for crawl");
    }

    /**
     * Gets the web page.
     *
     * @return Document
     */
    private Document getPage(String url) {
        Document doc = null;

        try {//Get the page content
            //LoggerUtil.getLogger().log(Level.INFO, "Fetching URL: {0}", url);
            doc = Jsoup.connect(url).parser(Parser.xmlParser()).ignoreContentType(true).get();
            LoggerUtilProject.getLogger().log(Level.INFO, "Fetched URL: {0}", url);
            GUI.jTextAreaCrawlerLog.append("\nINFO: Fetched URL:" + url);
            //System.out.println(doc.toString());
        } catch (IOException ex) {
            LoggerUtilProject.getLogger().severe("Error Fetching url:" + url + "\n" + ex.toString());
            GUI.jTextAreaCrawlerLog.append("\nSEVERE: Error Fetching url:" + url + "\n" + ex.toString());
        }
        return doc;
    }

    /**
     * Processes the Page Downloaded. It does two things <ul><li>Extracts
     * Links.</li>
     * <li>Saves page and other values to database.</li></ul>
     *
     * @param url The URL of the page
     * @param Document The page fetched and saved as Document
     */
    private void processPage(String url, Document doc) {
        if (doc != null) {
            Elements links = doc.select("a[href]");
            for (Element link : links) {//Extract links from page fetched & add them to urlToCrawl
                String linkRef = link.attr("abs:href");
                UrlValidator validator = new UrlValidator();
                if (!urlsCrawled.contains(linkRef) && validator.isValid(linkRef) && linkRef != null) {
                    urlsToCrawl.add(linkRef);//Add to Crawl List after checking for already crawled, null and invalid URLs
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
    }

    /**
     * It Gracefully Shuts Down the Crawler Engine
     */
    private void gracefulShutdown() {
        //Gracefully the database connection
        try {
            if (pstmt != null) {
                pstmt.close();
            }
            if (con != null) {
                con.close();
            }
        } catch (SQLException ex) {
            LoggerUtilProject.getLogger().log(Level.SEVERE, "Exception thrown while carrying out graceful "
                    + "shutdown of Crawling Engine:\t{0}", ex.toString());
            GUI.jTextAreaCrawlerLog.append("\nSEVERE: Exception thrown while carrying out graceful shutdown of Crawling Engine:\t" + ex.toString());
            GUI.jTextAreaParserLog.append("\nSEVERE: Exception thrown while carrying out graceful shutdown of Parsing Engine:\t" + ex.toString());
            GUI.jTextAreaSeedingEngineLog.append("\nSEVERE: Exception thrown while carrying out graceful shutdown of Seeding Engine:\t" + ex.toString());
        }
    }

    /**
     * Destructor. The finalize method will carry out a graceful shutdown of the
     * CrawlingEngine.
     *
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        gracefulShutdown();
        super.finalize();
    }

    /**
     * Nested Class. Refreshes the seeds of urlsToCrawl from the sites already
     * saved in database.
     */
    class seedRefresher implements Runnable {

        @Override
        public void run() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
}
