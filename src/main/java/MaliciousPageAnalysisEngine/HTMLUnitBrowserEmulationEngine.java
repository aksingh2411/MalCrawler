package MaliciousPageAnalysisEngine;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import util.LoggerUtilProject;
import util.PropertiesUtil;

/**
 * This class provides the HTMLUnit Browser Emulation facility
 *
 * @version 1.3
 */
public class HTMLUnitBrowserEmulationEngine {
    //Emulates form submission on a webpage

    public static void submittingForm(String url, String testForm) {
        try {
            WebClient webClient = new WebClient();
            // Get the first page
            HtmlPage page1 = webClient.getPage(url);
            // Get the form that we are dealing with and within that form, 
            // find the submit button and the field that we want to change.
            HtmlForm form = page1.getFormByName(testForm);
            HtmlSubmitInput button = form.getInputByName("submitbutton");
            //final HtmlTextInput textField = form.getInputByName("userid");
            // Change the value of the text field
            //textField.setValueAttribute("root");
            // Now submit the form by clicking the button and get back the second page.
            HtmlPage page2 = button.click();
            webClient.closeAllWindows();
        } catch (IOException | FailingHttpStatusCodeException ex) {
            Logger.getLogger(HTMLUnitBrowserEmulationEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This function returns the JavaScript Code in the URL
     * @version 1.3.3
     */
    public static String getJavaScript(String url) {
        String s = "";
        try {
            java.sql.Connection con = DriverManager.getConnection(PropertiesUtil.databaseConnectivityString, PropertiesUtil.databaseUserName, PropertiesUtil.databaseUserPassword);
            PreparedStatement pstmt = null;
            pstmt = con.prepareStatement("SELECT * from repository.webpage where url=?");
            pstmt.setString(1,url);
            java.sql.ResultSet rs = pstmt.executeQuery();
            rs.next();
            //System.out.println(rs.getString("page_content"));// to be deleted after testing

            Document doc = Parser.parse(rs.getString("page_content"),url);
            Elements tags = doc.getElementsByTag("script");//Get the JavaScript
            for (Element e : tags) {
                s += e.outerHtml();
            }
        } catch (SQLException ex) {
            LoggerUtilProject.getLogger().severe("SQL Exception thrown by HTML browser Emulation engine in getJavaScript() method \t" + ex.toString());
        }
        /*
         try {

         Document doc = Jsoup.connect(url).parser(Parser.xmlParser()).ignoreContentType(true).get();
                      
         WebClient webClient = new WebClient();
         // Get the  page
         HtmlPage page = webClient.getPage(url);
         DomNodeList<DomElement> e = page.getElementsByTagName("script");   //Get the JavaScript    
         for (int i = 0; i < e.getLength(); i++) {
         DomElement d = e.get(i);
         s = s + d.getTextContent();
         }
             
         } catch (IOException | FailingHttpStatusCodeException ex) {
         LoggerUtilProject.getLogger().info("Problem faced in JavaScript Exgtraction for url:" + url + ex.toString());
         }
         */
        return s;
    }

    /**
     * This function returns the number of eval() functions found in the
     * JavaScript Code
     *
     * @param url
     * @return
     */
    public static int getNoOfEval(String url) {
        int noEval = 0;
        String s = getJavaScript(url);
        Pattern pattern = Pattern.compile("eval");// Looking for the eval keyword
        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
            noEval++;
        }
        return noEval;
    }

    /**
     * This function is gives the length of JavaScript code
     *
     * @param url
     * @return
     */
    public static int lengthOfJavaScript(String url) {
        int lengthJavaScript = 0;
        try {
            String s = getJavaScript(url);
            if (s != null) {
                lengthJavaScript = s.length();
            }
        } catch (Exception ex) {
            LoggerUtilProject.getLogger().severe("Exception thrown while checking length of JavaScript: " + ex.toString());
        }
        return lengthJavaScript;
    }

    /**
     * This method will be used to find the number of document.writes by the
     * JavaScript
     *
     * @param url
     * @return
     */
    public static int noOfDocumentWrites(String url) {
        int noOfDocumentWrites = 0;
        String s = getJavaScript(url);
        Pattern pattern = Pattern.compile("document.write");// Looking for the eval keyword
        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
            noOfDocumentWrites++;
        }
        return noOfDocumentWrites;
    }

    /**
     * This function checks whether HTTPS has been used or not
     *
     * @param url
     * @return
     */
    public static boolean httpsUsed(String url) {
        if (url.contains("https")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Main function for testing features
     */
    public static void main(String[] args) {
        System.out.println(getJavaScript("http://www.nervetree.in/"));
        System.out.println(getNoOfEval("http://www.google.com"));
        System.out.println(lengthOfJavaScript("http://www.google.com"));
        System.out.println(noOfDocumentWrites("http://www.google.com"));
    }
}
