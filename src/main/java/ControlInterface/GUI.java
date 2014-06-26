package ControlInterface;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.Timer;
import CrawlingParsingEngine.CrawlerEngine;
import MaliciousPageAnalysisEngine.CloakingDetection;
import MaliciousPageAnalysisEngine.DataMiningEngine;
import MaliciousPageAnalysisEngine.RedirectionDetection;
import MaliciousPageAnalysisEngine.StaticHeuristics;
import java.io.IOException;
import java.util.logging.Level;
import util.LoggerUtilProject;

/**
 * This Class is the main GUI of the Project. It has the 'Main Thread' which
 * spawns other threads.
 *
 * @version 1.0
 */
public class GUI extends javax.swing.JFrame {

    /**
     * Thread to check internet connectivity presence every 30 seconds
     */
    Thread threadCheckInternetConnectivity;
    /**
     * Thread for Crawling Engine
     */
    Thread threadCrawlingEngine;
    /**
     * Thread for Checking Cloaking
     */
    Thread threadCloakingCheck;
    /**
     * Thread for checking redirection by URL
     */
    Thread threadRedirectionCheck;
    /**
     * This thread checks for malicousness using static heuristics
     */
    Thread threadStaticHeuristics;
    /**
     * This thread runs the WEKA Data Mining Engine
     */
    Thread threadDataMiningEngine;
    /**
     * Boolean flag to know if the crawler is running
     */
    public static boolean crawlerRunning;
    /**
     * Boolean flag to check for internet connectivity
     */
    public static boolean internetConnected = false;
    /**
     * Time counter from the start of application (in seconds)
     */
    int timeCounter = 0;

    /**
     * The constructor initializes all components
     */
    public GUI() {
        initComponents();

        Timer timer = new Timer(1000, new TimerListener());//Swing Timer handling GUI Refreshes
        timer.start();

        threadCheckInternetConnectivity = new Thread(new InternetConnectivityCheck());
        threadCheckInternetConnectivity.start();//Fork new thread for Internet Connectivity Check Every 30 secs

        threadCrawlingEngine = new Thread(new CrawlerEngine());//Fork new thread for Crawling Engine
        threadCrawlingEngine.start();
        crawlerRunning = true;

        threadCloakingCheck = new Thread(new CloakingDetection());
        threadCloakingCheck.start();

        threadRedirectionCheck = new Thread(new RedirectionDetection());
        threadRedirectionCheck.start();

        threadStaticHeuristics = new Thread(new StaticHeuristics());
        threadStaticHeuristics.start();

        threadDataMiningEngine = new Thread(new DataMiningEngine());
    }

    /**
     * Runnable interface implementation for Internet Connectivity Check
     */
    private class InternetConnectivityCheck implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    URL url;
                    url = new URL("http://bot.whatismyipaddress.com");
                    //Proxy pxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.155.7.6", 80));//Using Pxoxy
                    //URLConnection urlCon = url.openConnection(pxy);
                    URLConnection urlCon = url.openConnection();
                    urlCon.setConnectTimeout(2000);
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
                    labelIPAddressDisplay.setText(br.readLine());
                    internetConnected = true;
                    br.close();
                    if (internetConnected) {
                        labelInternetIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/InternetConnected.JPG")));
                    }
                } catch (IOException ex) {
                    LoggerUtilProject.getLogger().log(Level.SEVERE, "Internet Connectivity Check fails: \t{0}", ex.toString());
                    internetConnected = false;
                    if (!internetConnected) {
                        labelInternetIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ControlInterface/InternetDisconnected.JPG")));
                    }
                    labelIPAddressDisplay.setText("Network Failure");
                } finally {
                    try {
                        Thread.currentThread().sleep(30000);
                    } catch (InterruptedException ex) {
                        LoggerUtilProject.getLogger().log(Level.SEVERE, "Error putting the Internet "
                                + "Connectivity Thread to Sleep:\t{0}", ex.toString());
                    }
                }
            }
        }
    }

    /**
     * This Class implements an ActionListener for the 1 second GUI Refresh
     * Timer
     */
    private class TimerListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            labelCrawlingTimeDisplay.setText(Integer.toString(timeCounter++));//Time from start of Crawl     
            //jTextAreaCrawlerLog.append("This is the dummy log message(Implementation Pending) \n");
            //jTextAreaParserLog.append("This is the dummy log message(Implementation Pending)  \n");
            //jTextAreaSeedingEngineLog.append("This is the dummy log message(Implementation Pending) \n");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelCrawlingTimeDisplay = new java.awt.Label();
        labelInternetCrawlTime = new java.awt.Label();
        labelIPAddress = new java.awt.Label();
        jSeparator1 = new javax.swing.JSeparator();
        jPanelGraphicalDisplay = new javax.swing.JPanel();
        labelInternetIcon = new javax.swing.JLabel();
        buttonCrawler = new java.awt.Button();
        labelCrawlerParserConnection = new java.awt.Label();
        labelCrawlerParserConnection1 = new java.awt.Label();
        buttonParser = new java.awt.Button();
        buttonSeedingEngine = new java.awt.Button();
        labelParserDatabaseConnection = new java.awt.Label();
        labelCrawlerParserConnection3 = new java.awt.Label();
        buttonDatabase = new java.awt.Button();
        buttonCrawlerGuidance = new java.awt.Button();
        buttonMaliciousPageAnalysis = new java.awt.Button();
        jPanelBottom = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaCrawlerLog = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextAreaParserLog = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextAreaSeedingEngineLog = new javax.swing.JTextArea();
        labelCrawlerLog = new java.awt.Label();
        labelCrawlerLog1 = new java.awt.Label();
        labelCrawlerLog2 = new java.awt.Label();
        labelIPAddressDisplay = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Mining of Malicious Web Sites");
        setResizable(false);

        labelCrawlingTimeDisplay.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        labelCrawlingTimeDisplay.setName("labelCrawlingTimeDisplay"); // NOI18N
        labelCrawlingTimeDisplay.setText("0");

        labelInternetCrawlTime.setText("Time Elapsed Crawling (in secs):");

        labelIPAddress.setText("IP Address (As seen to Web Server):");

        jPanelGraphicalDisplay.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        labelInternetIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/InternetDisconnected.JPG"))); // NOI18N
        labelInternetIcon.setText("Internet");

        buttonCrawler.setBackground(new java.awt.Color(51, 102, 255));
        buttonCrawler.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        buttonCrawler.setForeground(new java.awt.Color(51, 0, 51));
        buttonCrawler.setLabel("Crawler");
        buttonCrawler.setName("labelCrawler"); // NOI18N
        buttonCrawler.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                buttonCrawlerMouseClicked(evt);
            }
        });
        buttonCrawler.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCrawlerActionPerformed(evt);
            }
        });

        labelCrawlerParserConnection.setText("------------------->---------------------------------");

        labelCrawlerParserConnection1.setText("<---------------------------");

        buttonParser.setBackground(new java.awt.Color(51, 102, 255));
        buttonParser.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        buttonParser.setForeground(new java.awt.Color(51, 0, 51));
        buttonParser.setLabel("Parser");
        buttonParser.setName("labelCrawler"); // NOI18N

        buttonSeedingEngine.setBackground(new java.awt.Color(0, 153, 153));
        buttonSeedingEngine.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        buttonSeedingEngine.setForeground(new java.awt.Color(51, 0, 51));
        buttonSeedingEngine.setLabel("Seeding Engine");
        buttonSeedingEngine.setName("labelCrawler"); // NOI18N
        buttonSeedingEngine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSeedingEngineActionPerformed(evt);
            }
        });

        labelParserDatabaseConnection.setText("------------------->>>>>>>>>>>>>>");

        labelCrawlerParserConnection3.setText("<----------------------------------------------");

        buttonDatabase.setBackground(new java.awt.Color(204, 0, 204));
        buttonDatabase.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        buttonDatabase.setForeground(new java.awt.Color(51, 0, 51));
        buttonDatabase.setLabel("Database");
        buttonDatabase.setName("labelCrawler"); // NOI18N

        buttonCrawlerGuidance.setBackground(new java.awt.Color(204, 153, 0));
        buttonCrawlerGuidance.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        buttonCrawlerGuidance.setLabel("Crawler Guidance");
        buttonCrawlerGuidance.setName(""); // NOI18N

        buttonMaliciousPageAnalysis.setBackground(new java.awt.Color(255, 51, 51));
        buttonMaliciousPageAnalysis.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        buttonMaliciousPageAnalysis.setLabel("Malicious Page Analysis");
        buttonMaliciousPageAnalysis.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                buttonMaliciousPageAnalysisMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanelGraphicalDisplayLayout = new javax.swing.GroupLayout(jPanelGraphicalDisplay);
        jPanelGraphicalDisplay.setLayout(jPanelGraphicalDisplayLayout);
        jPanelGraphicalDisplayLayout.setHorizontalGroup(
            jPanelGraphicalDisplayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelGraphicalDisplayLayout.createSequentialGroup()
                .addGroup(jPanelGraphicalDisplayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelGraphicalDisplayLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(labelInternetIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(buttonCrawler, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(labelCrawlerParserConnection1, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(buttonSeedingEngine, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(labelCrawlerParserConnection3, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(buttonDatabase, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelGraphicalDisplayLayout.createSequentialGroup()
                        .addGroup(jPanelGraphicalDisplayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelGraphicalDisplayLayout.createSequentialGroup()
                                .addGap(391, 391, 391)
                                .addComponent(labelCrawlerParserConnection, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanelGraphicalDisplayLayout.createSequentialGroup()
                                .addGap(333, 333, 333)
                                .addComponent(buttonCrawlerGuidance, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(1, 1, 1)
                        .addComponent(buttonParser, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanelGraphicalDisplayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelGraphicalDisplayLayout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(labelParserDatabaseConnection, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanelGraphicalDisplayLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(buttonMaliciousPageAnalysis, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelGraphicalDisplayLayout.setVerticalGroup(
            jPanelGraphicalDisplayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelGraphicalDisplayLayout.createSequentialGroup()
                .addGroup(jPanelGraphicalDisplayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelGraphicalDisplayLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanelGraphicalDisplayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelCrawlerParserConnection3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(buttonSeedingEngine, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanelGraphicalDisplayLayout.createSequentialGroup()
                        .addGroup(jPanelGraphicalDisplayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelGraphicalDisplayLayout.createSequentialGroup()
                                .addGap(63, 63, 63)
                                .addComponent(labelInternetIcon))
                            .addGroup(jPanelGraphicalDisplayLayout.createSequentialGroup()
                                .addGap(71, 71, 71)
                                .addComponent(buttonCrawler, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanelGraphicalDisplayLayout.createSequentialGroup()
                                .addGroup(jPanelGraphicalDisplayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanelGraphicalDisplayLayout.createSequentialGroup()
                                        .addGap(26, 26, 26)
                                        .addComponent(buttonCrawlerGuidance, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(21, 21, 21)
                                        .addComponent(labelCrawlerParserConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanelGraphicalDisplayLayout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(buttonMaliciousPageAnalysis, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(labelParserDatabaseConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(29, 29, 29)
                                .addComponent(labelCrawlerParserConnection1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanelGraphicalDisplayLayout.createSequentialGroup()
                                .addGap(73, 73, 73)
                                .addComponent(buttonDatabase, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap(18, Short.MAX_VALUE))
            .addGroup(jPanelGraphicalDisplayLayout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addComponent(buttonParser, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTextAreaCrawlerLog.setColumns(20);
        jTextAreaCrawlerLog.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        jTextAreaCrawlerLog.setRows(5);
        jScrollPane1.setViewportView(jTextAreaCrawlerLog);

        jTextAreaParserLog.setColumns(20);
        jTextAreaParserLog.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        jTextAreaParserLog.setRows(5);
        jScrollPane2.setViewportView(jTextAreaParserLog);

        jTextAreaSeedingEngineLog.setColumns(20);
        jTextAreaSeedingEngineLog.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        jTextAreaSeedingEngineLog.setRows(5);
        jScrollPane3.setViewportView(jTextAreaSeedingEngineLog);

        labelCrawlerLog.setAlignment(java.awt.Label.CENTER);
        labelCrawlerLog.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        labelCrawlerLog.setName("labelCrawlerLog"); // NOI18N
        labelCrawlerLog.setText("CRAWLER LOG");

        labelCrawlerLog1.setAlignment(java.awt.Label.CENTER);
        labelCrawlerLog1.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        labelCrawlerLog1.setName("lableParserLog"); // NOI18N
        labelCrawlerLog1.setText("PARSER LOG");

        labelCrawlerLog2.setAlignment(java.awt.Label.CENTER);
        labelCrawlerLog2.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        labelCrawlerLog2.setName("LabelSeedingEngineLog"); // NOI18N
        labelCrawlerLog2.setText("SEEDING ENGINE LOG");

        javax.swing.GroupLayout jPanelBottomLayout = new javax.swing.GroupLayout(jPanelBottom);
        jPanelBottom.setLayout(jPanelBottomLayout);
        jPanelBottomLayout.setHorizontalGroup(
            jPanelBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelBottomLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 396, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 369, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanelBottomLayout.createSequentialGroup()
                .addGap(145, 145, 145)
                .addComponent(labelCrawlerLog, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(251, 251, 251)
                .addComponent(labelCrawlerLog1, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(labelCrawlerLog2, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(114, 114, 114))
        );
        jPanelBottomLayout.setVerticalGroup(
            jPanelBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelBottomLayout.createSequentialGroup()
                .addGroup(jPanelBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelCrawlerLog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelCrawlerLog1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelCrawlerLog2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(jPanelBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                    .addComponent(jScrollPane2)
                    .addComponent(jScrollPane3)))
        );

        labelIPAddressDisplay.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        labelIPAddressDisplay.setText("Network Connecting..");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanelGraphicalDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(182, 182, 182))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jPanelBottom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelInternetCrawlTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelCrawlingTimeDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelIPAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(labelIPAddressDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labelInternetCrawlTime, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelCrawlingTimeDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labelIPAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelIPAddressDisplay))
                .addGap(0, 0, 0)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelGraphicalDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelBottom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Event handler for Crawler Button Press
     *
     * @param evt
     */
    private void buttonCrawlerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCrawlerActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_buttonCrawlerActionPerformed

    /**
     * Event handler for Seeding Button Press
     *
     * @param evt
     */
    private void buttonSeedingEngineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSeedingEngineActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_buttonSeedingEngineActionPerformed

    private void buttonMaliciousPageAnalysisMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonMaliciousPageAnalysisMouseClicked
        //javax.swing.JOptionPane.showMessageDialog(null, threadDataMiningEngine.getState().name());
        int i = javax.swing.JOptionPane.showConfirmDialog(null, "Do you want to run the WEKA Data Mining Engine");
        if (i == 0) {
            if (threadDataMiningEngine.getState().name().contentEquals("NEW")) {
                //threadDataMiningEngine = new Thread(new DataMiningEngine());
                threadDataMiningEngine.start();
                javax.swing.JOptionPane.showMessageDialog(null, "WEKA Data Mining Engine Started");
            } else if (threadDataMiningEngine.getState().name().contentEquals("TERMINATED")) {
                threadDataMiningEngine = new Thread(new DataMiningEngine());
                threadDataMiningEngine.start();
            } else {
                javax.swing.JOptionPane.showMessageDialog(null, "WEKA engine cannot be started because its thread is in the following stage: " + threadDataMiningEngine.getState());
            }
        }
    }//GEN-LAST:event_buttonMaliciousPageAnalysisMouseClicked

    private void buttonCrawlerMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonCrawlerMouseClicked
        if (crawlerRunning) {
            int i = javax.swing.JOptionPane.showConfirmDialog(null, "Do you want to stop the Crawling engine");
            if (i == 0) {
                threadCrawlingEngine.suspend();
                LoggerUtilProject.getLogger().severe("The crawler has been paused by the GUI");
                GUI.jTextAreaCrawlerLog.append("The crawler has been paused by the GUI");
                buttonCrawler.setLabel("Crawler(P)");
                crawlerRunning = false;
            }
        } else {
            int i = javax.swing.JOptionPane.showConfirmDialog(null, "Do you want to start the Crawling engine");
            if (i == 0) {
                threadCrawlingEngine.resume();
                LoggerUtilProject.getLogger().severe("The crawler has been resumed by the GUI");
                GUI.jTextAreaCrawlerLog.append("The crawler has been resumed by the GUI");
                buttonCrawler.setLabel("Crawler");
                crawlerRunning = true;
            }
        }
    }//GEN-LAST:event_buttonCrawlerMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            LoggerUtilProject.getLogger().log(Level.SEVERE, "Error in the Main Thread Start:\t{0}", ex.toString());
        }
        //</editor-fold>
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GUI().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private java.awt.Button buttonCrawler;
    private java.awt.Button buttonCrawlerGuidance;
    private java.awt.Button buttonDatabase;
    private java.awt.Button buttonMaliciousPageAnalysis;
    private java.awt.Button buttonParser;
    private java.awt.Button buttonSeedingEngine;
    private javax.swing.JPanel jPanelBottom;
    private javax.swing.JPanel jPanelGraphicalDisplay;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    public static javax.swing.JTextArea jTextAreaCrawlerLog;
    public static javax.swing.JTextArea jTextAreaParserLog;
    public static javax.swing.JTextArea jTextAreaSeedingEngineLog;
    private java.awt.Label labelCrawlerLog;
    private java.awt.Label labelCrawlerLog1;
    private java.awt.Label labelCrawlerLog2;
    private java.awt.Label labelCrawlerParserConnection;
    private java.awt.Label labelCrawlerParserConnection1;
    private java.awt.Label labelCrawlerParserConnection3;
    private java.awt.Label labelCrawlingTimeDisplay;
    private java.awt.Label labelIPAddress;
    private javax.swing.JLabel labelIPAddressDisplay;
    private java.awt.Label labelInternetCrawlTime;
    private javax.swing.JLabel labelInternetIcon;
    private java.awt.Label labelParserDatabaseConnection;
    // End of variables declaration//GEN-END:variables
}
