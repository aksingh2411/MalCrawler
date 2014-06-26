package CrawlingParsingEngine;

import ControlInterface.GUI;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import util.LoggerUtilProject;

/**
 * This class provides the functionality of Crawler Guidance
 *
 * @version 1.3
 */
public class CrawlerGuidanceEngine implements Runnable {

    /**
     * Stores the URL currently being crawled to keep a track of crawl
     */
    public static String urlCurrentlyBeingCrawled;
    /**
     * Stores the name of the Domain being currently crawled by the crawler
     */
    public static String domainCurrentlyBeingCrawled;
    /**
     * Keeps the running time for a domain crawl
     */
    public static Timer runningTimeForDomainCrawl;
    /**
     * Keeps track of current depth of crawl in a domain
     */
    public static int currentDepthOfCrawl;
    /**
     * Keeps track of current breadth of crawl in a domain
     */
    public static int currentBreadthOfCrawl;
    /**
     * Permitted level of depth for a domain
     */
    public static int permittedDepthOfCrawl;
    /**
     * Permitted level of breadth for a domain
     */
    public static int permittedBreadthOfCrawl;
    public static int urlsCrawledInCurrentDomain;

    /**
     * Fuzzy logic to avoid entanglement
     */
    private void entanglementFuzzyCheck() {
        if(currentDepthOfCrawl > permittedDepthOfCrawl){
            guideCrawler("EXIT_DOMAIN");
        }else if(currentBreadthOfCrawl>permittedBreadthOfCrawl){
            guideCrawler("EXIT_DOMAIN");
        }else{
            guideCrawler("CONTINUE_CRAWLING_CURRENT_DOMAIN");
        }
    }

    public CrawlerGuidanceEngine() {
        initComponents();
    }

    /**
     * Components are Initialized
     */
    private void initComponents() {
        urlCurrentlyBeingCrawled = null;
        domainCurrentlyBeingCrawled = null;
        runningTimeForDomainCrawl = null;
        currentDepthOfCrawl = 0;
        currentBreadthOfCrawl = 0;
        permittedDepthOfCrawl = 5;
        permittedBreadthOfCrawl = 20;
        urlsCrawledInCurrentDomain = 0;
    }

    /**
     * This functions is responsible for guiding the crawler
     */
    private void guideCrawler(String advice) {
    }

    public void updateStatus(String url) {

        url = urlCurrentlyBeingCrawled;//Update the URL being crawled
        String extractedDomain = url.split("/")[0];
        if (extractedDomain.equalsIgnoreCase(domainCurrentlyBeingCrawled)) {
            runningTimeForDomainCrawl.isRunning();//Check the timer with the timer limit for each domain
            entanglementFuzzyCheck();//Entanglement avoidance logic
        } else {
            domainCurrentlyBeingCrawled = extractedDomain;
            runningTimeForDomainCrawl = new Timer(30000, new TimeListener());//Reset timer.Start it for this new domain

        }
    }

    @Override
    public void run() {
        LoggerUtilProject.getLogger().info("The Crawler Guidance Engine has started");
        GUI.jTextAreaCrawlerLog.append("/n INFO: The crawler Guidance Engine has started");
    }

    /**
     * This is the Action listener which comes to play when the timer for each
     * domain's crawl expires
     */
    private static class TimeListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {// Acts on the Crawl timer being expired.
            LoggerUtilProject.getLogger().info("The timer for this domain has expired. The crawler is advised to move to a different domain");
            GUI.jTextAreaCrawlerLog.append("/n INFO: The timer for the current domains crawl has expired move to a different domain");
        }
    }
}
