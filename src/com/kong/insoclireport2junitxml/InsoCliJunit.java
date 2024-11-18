package com.kong.insoclireport2junitxml;
import com.kong.insoclireport2junitxml.InsoTestCase;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class InsoCliJunit {

  // String declaration for strcuturing the 'inso CLI' input
  private String insoRunningReq     = "Running request: ";
  private String insoNetworkRes     = "[network] Response ";
  private String insoFailedRes      = "failed";
  private String insoErrRes         = "err=";
  private String insoTestResults    = "Test results:";
  private String insoTotalTests     = "Total tests: ";
  private String insoFailedTest     = "Failed: ";
  private String insoRegExErrorTest = "(^\\w*Error:\\s)(.*)";

  // Read the 'inso CLI' input file and put it in the memory 
  private boolean readInsoIntput (String fileName, List<String> linesList)
  {
    boolean rc = true;
    try {
      List<String> lines = Files.readAllLines(Paths.get(fileName));
      for (String line : lines) {
        linesList.add(line);
      }
      } catch (Exception e) {
        rc = false;
        e.printStackTrace();
      }
      
      return rc;
  }
  public static void main(String[] args) throws Exception {
    boolean             rc              = true;
    boolean             bTCFound        = false;
    InsoCliJunit        insoCliJunit    = new InsoCliJunit();
    List<String>        linesInsoInput  = new ArrayList<>();
    List<InsoTestCase>  insoTestcases   = new ArrayList<>();
    Attr                attr            = null;
    Element             testSuite       = null;
    Element             testCase        = null;
    Element             sysErr          = null;
    Element             failure         = null;
    String              lineInso        = "";
    String              testSuiteName   = "";
    String              sep             = "";
    String              tmplineInso     = "";
    String[]            words           = null;
    int                 nb              = 0;
    int                 nb2             = 0;
    int                 index           = 0;
    int                 indexError      = 0;
    
    if (args.length == 0){
      rc = false;
      System.out.println("Usage: java insoCliJunit [inso-cli-input]");
      System.out.println("");
      System.out.println("With this tool you can convert the 'inso cli' input into an XML JUnit format.");
      System.out.println("The 'inso-cli-input' is returned by calling 'inso run collection' (For instance: 'inso run collection wrk_XYZ > inso-cli-input.log')");
      System.out.println("");
      System.out.println("  Example:");
      System.out.println("  $ java insoCliJunit inso-cli-input.log");
    }

    // Read the 'inso CLI' input file and put it in the memory
    if (rc){
      rc = insoCliJunit.readInsoIntput (args[0], linesInsoInput);
    }

    if (rc){
      // Creating a DocumentBuilder Object
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      
      // Create a new Document
      Document doc = dBuilder.newDocument();
      
      // Creating the <testsuites> element
      Element rootElement = doc.createElement("testsuites");
      doc.appendChild(rootElement);
      
      Iterator<String> iteratorLineInso = linesInsoInput.iterator();
      int nbLine = 0;
      boolean executeNext = true;
      //-------------------------------------------------------
      // Parse and structure all lines of the 'inso CLI' input
      //-------------------------------------------------------
      while (iteratorLineInso.hasNext()) {
        
        // Get next inso line
        if (executeNext){
          lineInso = iteratorLineInso.next();
          nbLine++;
        }
        else {
          executeNext = true;
        }

        //--------------------------------------------
        // If the line starts with: "Running request:"
        //--------------------------------------------
        if (lineInso.startsWith(insoCliJunit.insoRunningReq)){
          tmplineInso = lineInso.substring(insoCliJunit.insoRunningReq.length());
          // Split by whitespace
          words = tmplineInso.split("\\s+");
          testSuiteName = "";
          // Replace ' ' by '_' except for the last word that is separeted by '.'
          for (nb = 0; nb < words.length; nb++)
          {            
            sep = (nb == 0) ? "" : (nb == words.length - 1) ? "." : "_";
            testSuiteName += sep + words[nb];
          }
          if (!testSuiteName.isEmpty()) {
              // Appending <testsuite> element to the <testsuites> element
              testSuite = doc.createElement("testsuite");
              rootElement.appendChild(testSuite);
              // Setting attribute to the sub element
              attr = doc.createAttribute("name");
              attr.setValue(testSuiteName);
              testSuite.setAttributeNode(attr);            
          }
        }
        //-----------------------------------------------------------
        // Else If the line starts with: "[network] Response failed"
        //-----------------------------------------------------------        
        else if (lineInso.startsWith(insoCliJunit.insoNetworkRes + insoCliJunit.insoFailedRes)){
          if (testSuite == null) {
            throw new Exception("Line #" + nbLine + ": Found '" + insoCliJunit.insoNetworkRes + insoCliJunit.insoFailedRes + 
                                "' but there is no 'testsuite'");
          }
          
          // If the line contains 'err=''
          index = lineInso.lastIndexOf(insoCliJunit.insoErrRes);
          if (index == -1){
            throw new Exception("Line #" + nbLine + ": Found '" + insoCliJunit.insoNetworkRes + insoCliJunit.insoFailedRes + 
                                "' but there is no '" + insoCliJunit.insoErrRes + "' detail");
          }
          // Appending <system-err> element to the <testsuite> element
          sysErr = doc.createElement("system-err");
          testSuite.appendChild(sysErr);
          sysErr.appendChild(doc.createTextNode(lineInso.substring(index + insoCliJunit.insoErrRes.length())));          
        }
        //-----------------------------------------------------------
        // Else If the line starts with: "Test results:"
        //-----------------------------------------------------------        
        else if (lineInso.startsWith(insoCliJunit.insoTestResults)){
          insoTestcases.clear();

          // Parse and structure all 'Test results' associated to the <testsuite>
          while (iteratorLineInso.hasNext() && executeNext) {
            
            // Get next inso line
            lineInso = iteratorLineInso.next();
            nbLine++;
            
            // If there is a 'Test Result'
            if (!lineInso.isEmpty()){
              // Appending <testcase> element to the <testsuite> element
              testCase = doc.createElement("testcase");
              if (testSuite == null){
                throw new Exception("Line #" + nbLine + " invalid 'testSuite' Java object");
              }
              testSuite.appendChild(testCase);
              // If the 'Test Result' hasn't the right format (less than 2 chars, and not starting by '✅ ' nor by '❌ ')
              if (lineInso.length() < 2 ||
                  ( !lineInso.startsWith("✅ ") &&
                    !lineInso.startsWith("❌ "))) { 
                throw new Exception("Line #" + nbLine + "' Found: '" + lineInso + "' invalid '" + insoCliJunit.insoTestResults);
              }
              tmplineInso = lineInso.substring(2);
              // Adding 'name' and 'classname' attributes to <testcase>
              attr = doc.createAttribute("name");
              attr.setValue(tmplineInso);
              testCase.setAttributeNode(attr);
              attr = doc.createAttribute("classname");
              attr.setValue(testSuiteName);
              testCase.setAttributeNode(attr);
              
              // Add the result to internal List
              insoTestcases.add(new InsoTestCase(tmplineInso, lineInso.startsWith("✅ ")));
              
            }
            // Else it's the end of the 'Test Results' section
            else{
              executeNext = false;
            }
          }
        }
        //-----------------------------------------------------------
        // Else If the line starts with: "Total tests: "
        //-----------------------------------------------------------        
        else if (lineInso.startsWith(insoCliJunit.insoTotalTests)){
          tmplineInso = lineInso.substring(insoCliJunit.insoTotalTests.length());
          if (testSuite == null){
            throw new Exception("Line #" + nbLine + " invalid 'testSuite' Java object");
          }
          // Adding 'tests' attributes to <testsuite>
          attr = doc.createAttribute("tests");
          attr.setValue(tmplineInso);
          testSuite.setAttributeNode(attr);          
        }
        //-----------------------------------------------------------
        // Else If the line starts with: "Failed:"
        //-----------------------------------------------------------        
        else if (lineInso.startsWith(insoCliJunit.insoFailedTest)){
          tmplineInso = lineInso.substring(insoCliJunit.insoFailedTest.length());
          if (testSuite == null){
            throw new Exception("Line #" + nbLine + " invalid 'testSuite' Java object");
          }
          // Adding 'tests' attributes to <testsuite>
          attr = doc.createAttribute("failures");
          attr.setValue(tmplineInso);
          testSuite.setAttributeNode(attr);          
        }
        //------------------------------------------------------------
        // Else If the line starts with: "***Error: "
        //   example: ReferenceError, AssertionError, TypeError, etc.
        //------------------------------------------------------------       
        else if (lineInso.matches(insoCliJunit.insoRegExErrorTest)) 
        {
          System.out.println("***Error: " + lineInso);
          indexError = 0;
          NodeList nList = testSuite.getElementsByTagName("testcase");

          // Parse and structure all "***Error: "
          while (executeNext) {
            
            // If the line starts with: "***Error: "
            if (lineInso.matches(insoCliJunit.insoRegExErrorTest)){
              
              Pattern pattern = Pattern.compile(insoCliJunit.insoRegExErrorTest);
              Matcher matcher = pattern.matcher(lineInso);
              if (!matcher.matches() || matcher.groupCount() != 2) {
                throw new Exception("Line #" + nbLine + " Found: '" + lineInso + "' invalid 'Error'");
              }
              if (testSuite == null){
                throw new Exception("Line #" + nbLine + " invalid 'testSuite' Java object");
              }
              bTCFound  = false;
              nb        = 0;
              nb2       = 0;
              // Find the nth iteration of 'failed' <testcase> in the 'insoTestcases' <List>
              for (InsoTestCase insoTestcase : insoTestcases) {
                // If the 'test' is failed
                if (!insoTestcase.getPassed()){
                  //  If the nth iteration is found
                  if (nb == indexError) {
                    bTCFound = true;
                    break;
                  }
                  nb++;
                }
                nb2++;
              }
              if (!bTCFound) {
                throw new Exception("Line #" + nbLine + " unable to find the '" + indexError + "' indexError of 'failed' tests in the 'insoTestcases' <List>");
              }
              
              // If the nb2 can't match with the number of <testcase>
              if (nb2 >= nList.getLength()) {
                throw new Exception("Line #" + nbLine + " invalid number: '" + nb + "' in <testcase> list");
              }
              Node nTestCase = nList.item(nb2);
              if (nTestCase.getNodeType() != Node.ELEMENT_NODE) {
                throw new Exception("Line #" + nbLine + " invalid 'NodeType': '" + nTestCase.getNodeType() + "'");
              }
              Element eElement = (Element) nTestCase;
              System.out.println("Test Case Name : " + eElement.getAttribute("name"));

              // Appending <failure> element to the <testcase> element
              failure = doc.createElement("failure");
              nTestCase.appendChild(failure);
              
              // Setting 'message' and 'type' attributes to the <failure> element
              attr = doc.createAttribute("message");
              attr.setValue(matcher.group(2));
              failure.setAttributeNode(attr);
              
              // Go on next Error
              indexError++;
              
              // Get next inso line
              if (iteratorLineInso.hasNext()) {
                lineInso = iteratorLineInso.next();
                nbLine++;
              }
              else{
                executeNext = false;
              }              
            }
            // Else it's the end of the '***Error: ' section (and now we are at "Running request: " level or at the End of file)
            else{
              executeNext = false;
            }            
          }                  
        }
      }

      // Writing the content into XML file
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      // Enable pretty-printing with indentation
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      // Add a carriage return for line separators
      transformer.setOutputProperty("{http://xml.apache.org/xslt}line-separator", "\r\n");
    
      DOMSource source = new DOMSource(doc);

      // Output XML to console for testing
      StreamResult result = new StreamResult(new File("inso-cli-junit.xml"));
      transformer.transform(source, result);

      // Output to console for testing
      StreamResult consoleResult = new StreamResult(System.out);
      transformer.transform(source, consoleResult);

    }
  }
}