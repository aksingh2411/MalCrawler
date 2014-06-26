package MaliciousPageAnalysisEngine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.LoggerUtilProject;
import util.PropertiesUtil;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 * This Class uses the WEKA engine for Data Mining. Dynamic data inputs are
 * pre-processed. Then classifiers are trained and then results taken.
 *
 * @version 1.3
 */
public class DataMiningEngine implements Runnable {

    /**
     * This variable stores the url currently being checked
     */
    private String url;
    /**
     * It is the Engine's connection to the Database
     */
    java.sql.Connection con;
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
     * These are the training Instances
     */
    Instances trainingData;
    /**
     * There are the testing Instances
     */
    Instances testingData;

    /**
     * Constructor
     */
    public DataMiningEngine() {
        initComponents();
    }

    @Override
    public void run() {
        checkDatabaseLimits();
        //System.out.println("/nLower limit is : " + idLowerLimit);
        //System.out.println("/nUpper Limit is :" + idUpperLimit);
        idPointer = idLowerLimit;
        preProcessData();
        runClassifier();
    }

    /**
     * This method is used for initializing all components.
     */
    private void initComponents() {
        try {
            con = DriverManager.getConnection(PropertiesUtil.databaseConnectivityString, PropertiesUtil.databaseUserName, PropertiesUtil.databaseUserPassword);
        } catch (SQLException ex) {
            LoggerUtilProject.getLogger().severe("SQL Exception thrown by Cloaking Detection Eninge while connecting to database: \t" + ex.toString());
        }
        pstmt = null;
        url = null;
        idPointer = 0;
        idLowerLimit = 0;
        idUpperLimit = 10;
        trainingData = null;
        testingData = null;
    }

    /**
     * Pre-processes all the data from the database to generate the ARFF
     */
    private void preProcessData() {
        FileWriter fw = null;
        File arffFile = null;
        try {
            arffFile = new File("testingDataset.arff");
            fw = new FileWriter(arffFile);
            fw.write("% TITLE: Pre-processed output of Malcious Mining for Classification \n");
            fw.append("% Collected using the Malicous Crawler \n\n");
            fw.append("@RELATION MaliciousMining \n\n");

            fw.append("@ATTRIBUTE id NUMERIC \n");// This attribute will not be used for mining
            fw.append("@ATTRIBUTE url_length NUMERIC \n");
            fw.append("@ATTRIBUTE https_used {0,1} \n");
            fw.append("@ATTRIBUTE document_length NUMERIC \n");
            fw.append("@ATTRIBUTE re_direction {0,1} \n");
            fw.append("@ATTRIBUTE cloaking {0,1} \n");
            fw.append("@ATTRIBUTE javascript_length NUMERIC \n");
            fw.append("@ATTRIBUTE no_eval NUMERIC \n");
            fw.append("@ATTRIBUTE no_document_writes NUMERIC \n");

            fw.append("@ATTRIBUTE maliciousness_found {YES,NO} \n\n");//This is the output of running classification
            fw.append("@DATA \n\n");

            while (idPointer <= idUpperLimit) {
                fetchNextURL();
                fw.append(idPointer + ",");// id
                fw.append(url.length() + ","); //url_length
                fw.append(httpsUsed() + ","); // https_used
                fw.append(documentLength() + ","); // document length
                fw.append(redirectionCheck() + ","); // re_direction
                fw.append(cloakingCheck() + ","); // cloaking check
                fw.append(HTMLUnitBrowserEmulationEngine.lengthOfJavaScript(url) + ","); // JavaScript length
                fw.append(HTMLUnitBrowserEmulationEngine.getNoOfEval(url) + ","); // No of eval() functions in javascript
                fw.append(HTMLUnitBrowserEmulationEngine.noOfDocumentWrites(url) + ",");// No of document writes
                fw.append(checkMaliciousness() + "\n");
                idPointer++;
            }
        } catch (IOException ex) {
            LoggerUtilProject.getLogger().severe("Error writing to the pre-processed file:" + ex.toString());
        } finally {
            try {
                fw.close();
            } catch (IOException ex) {
                LoggerUtilProject.getLogger().severe("Error closing the file writer" + ex.toString());
            }
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
            LoggerUtilProject.getLogger().severe("Error while checking Database Limits for Data Mining Engine: " + ex.toString());
        }
    }

    /**
     * This function get the URL from the database which needs to be checked.
     */
    private void fetchNextURL() {
        try {
            pstmt = con.prepareStatement("SELECT url from repository.webpage where id=?;");
            pstmt.setInt(1, idPointer);
            java.sql.ResultSet rs = pstmt.executeQuery();
            while (!rs.next()) {
                //idPointer++;
                pstmt.setInt(1, ++idPointer);
                rs = pstmt.executeQuery();
            }
            url = rs.getString("url");
            //System.out.println(url);
        } catch (SQLException ex) {
            LoggerUtilProject.getLogger().severe("Exception thrown wile getting URL for Data Mining engine:" + ex.toString());
        }
    }

    /**
     * This function reads from the database the value of redirection detected
     *
     * @return
     */
    private int redirectionCheck() {
        int i = 0;
        try {
            Statement stmt;
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = stmt.executeQuery("SELECT redirection_detected from repository.webpage where id=" + idPointer + ";");
            rs.next();
            boolean b = rs.getBoolean("redirection_detected");
            if (b) {
                i = 1;
            } else {
                i = 0;
            }
        } catch (SQLException ex) {
            LoggerUtilProject.getLogger().severe("Error while checking Database for Data Mining Engine: " + ex.toString());
        }
        return i;
    }

    /**
     * This function tests whether HTTPs has been used by the current URL
     *
     * @return
     */
    private int httpsUsed() {
        int i = 0;
        boolean b = HTMLUnitBrowserEmulationEngine.httpsUsed(url);
        if (b) {
            i = 1;
        } else {
            i = 0;
        }
        return i;
    }

    /**
     * This function reads from the database the value of cloaking detected
     *
     * @return
     */
    private int cloakingCheck() {
        int i = 0;
        try {
            Statement stmt;
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = stmt.executeQuery("SELECT cloaking_detected from repository.webpage where id=" + idPointer + ";");
            rs.next();
            boolean b = rs.getBoolean("cloaking_detected");
            if (b) {
                i = 1;
            } else {
                i = 0;
            }
        } catch (SQLException ex) {
            LoggerUtilProject.getLogger().severe("Error while checking Database for Data Mining Engine: " + ex.toString());
        }
        return i;
    }

    private String checkMaliciousness() {
        String s = null;
        try {
            Statement stmt;
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = stmt.executeQuery("SELECT maliciousness_found from repository.webpage where id=" + idPointer + ";");
            rs.next();
            boolean b = rs.getBoolean("maliciousness_found");
            if (b) {
                s = "YES";
            } else {
                s = "NO";
            }
        } catch (SQLException ex) {
            LoggerUtilProject.getLogger().severe("Error while checking Database for Data Mining Engine: " + ex.toString());
        }
        return s;
    }

    private int documentLength() {
        int i = 0;
        try {
            Statement stmt;
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = stmt.executeQuery("SELECT page_content from repository.webpage where id=" + idPointer + ";");
            rs.next();
            String s = rs.getString("page_content");
            i = s.length();
        } catch (SQLException ex) {
            LoggerUtilProject.getLogger().severe("Error while checking Database for Data Mining Engine: " + ex.toString());
        }
        return i;
    }

    /**
     * This function is used for trainign and testing the WEKA classifier
     *
     */
    private void runClassifier() {
        FileWriter fw = null;
        try {
            // load training dataset
            ArffLoader trainingLoader = new ArffLoader();
            trainingLoader.setFile(new File("trainingDataset.arff"));
            trainingData = trainingLoader.getDataSet();
            trainingData.setClassIndex(trainingData.numAttributes() - 1);// Setting the last attribute as the output class
            
            // load testing dataset
            ArffLoader testingLoader = new ArffLoader();
            testingLoader.setFile(new File("testingDataset.arff"));
            testingData = testingLoader.getDataSet();
            testingData.setClassIndex(testingData.numAttributes() - 1);// Setting the last attribute as the output class
            System.out.println("No of attributes:" + testingData.numAttributes());

            Remove rm = new Remove();
            rm.setAttributeIndices("1");  // remove 1st attribute as it is only the id of a url

            // classifier
            J48 j48 = new J48(); // J48 classifer
            j48.setUnpruned(true);        // using an unpruned J48
            // meta-classifier
            FilteredClassifier fc = new FilteredClassifier();
            fc.setFilter(rm);
            fc.setClassifier(j48);
            // train and make predictions
            fc.buildClassifier(trainingData);

            fw = new FileWriter(new File("classifier.result"));// File for saving the result

            for (int i = 0; i < testingData.numInstances(); i++) {
                double pred = fc.classifyInstance(testingData.instance(i));
                fw.write("ID: " + (int) testingData.instance(i).value(0));
                fw.append(", actual: " + testingData.classAttribute().value((int) testingData.instance(i).classValue()));
                fw.append(", predicted: " + testingData.classAttribute().value((int) pred)+ "\n");
                //System.out.print("ID: " + testingData.instance(i).value(0));
                //System.out.print(", actual: " + testingData.classAttribute().value((int) testingData.instance(i).classValue()));
                //System.out.println(", predicted: " + testingData.classAttribute().value((int) pred));
            }
        } catch (Exception ex) {
            LoggerUtilProject.getLogger().severe("Error while training and testing using WEKA: " + ex.toString());
        } finally {
            try {
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(DataMiningEngine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Destructor called to dispose resources left open
     *
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        LoggerUtilProject.getLogger().severe("Data Mining Engine is Shuting Down");
        try {
            if (pstmt != null) {
                pstmt.close();
            }
            if (con != null) {
                con.close();
            }
        } catch (SQLException ex) {
            LoggerUtilProject.getLogger().log(Level.SEVERE, "Exception thrown while closing the Data Mining engine:\t{0}", ex.toString());
        }
        super.finalize();
    }

    /**
     * Only for the purpose of testing this class
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            Thread threadDataMiningEngine = new Thread(new DataMiningEngine());
            threadDataMiningEngine.start();
            //DataMiningEngine d = new DataMiningEngine();
            //d.runClassifier();
            // load data
            /*
             ArffLoader loader = new ArffLoader();
             loader.setFile(new File("cpu.arff"));
             Instances structure = loader.getDataSet();
             //structure.setClassIndex(structure.numAttributes() - 1);
             System.out.println(structure.numInstances());
             */
        } catch (Exception ex) {
            Logger.getLogger(DataMiningEngine.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
