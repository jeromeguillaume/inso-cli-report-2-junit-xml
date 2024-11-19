package com.kong.insoclireport2junitxml;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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

public class InsoTool {
  
  // String declaration for structuring the 'inso CLI' input
  private String insoRunningReq     = "Running request: ";
  private String insoNetworkRes     = "[network] Response ";
  private String insoFailedRes      = "failed";
  private String insoErrRes         = "err=";
  private String insoTestResults    = "Test results:";
  private String insoTotalTests     = "Total tests: ";
  private String insoFailedTest     = "Failed: ";
  private String insoRegExErrorTest = "(^\\w*Error)(:\\s)(.*)";

  // 'inso CLI' input file in this ArrayList
  private List<String>  linesInsoInput  = new ArrayList<>();

  // JUnit XML document for structuring the 'inso CLI' input file 
  private Document      doc             = null;
  private Element       rootElement     = null;

  // Creation Time of the 'inso CLI' input file (example: 2024-11-10T15:23:34)
  private String        insoFileCreationTime = null;
  
  //-------------
  // Constructor
  //-------------
  public InsoTool (){
    DocumentBuilderFactory  dbFactory   = null;
    DocumentBuilder         dBuilder    = null;
    
    try {
      this.linesInsoInput = new ArrayList<>();

      // Creating a DocumentBuilder Object
      dbFactory = DocumentBuilderFactory.newInstance();
      dBuilder = dbFactory.newDocumentBuilder();
          
      // Create a new Document
      this.doc = dBuilder.newDocument();
          
      // Creating the <testsuites> element
      this.rootElement = this.doc.createElement("testsuites");
      doc.appendChild(this.rootElement);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  //---------------------------------------------------------
  // Read the 'inso CLI' input file and put it in the memory 
  //---------------------------------------------------------
  public boolean readInsoIntput (String insoReportFileName)
  {
    boolean rc = true;
    
    try {
      // Put in an ArrayList the content of the 'inso CLI' input
      List<String> lines = Files.readAllLines(Paths.get(insoReportFileName));
      for (String line : lines) {
        this.linesInsoInput.add(line);
      }

      // Get the creation Time of the 'inso CLI' input
      Path filePath = Paths.get(insoReportFileName);
      // Get the basic file attributes
      BasicFileAttributes attributes = Files.readAttributes(filePath, BasicFileAttributes.class);
      // Get the creation time, last modified time, and last access time
      FileTime creationTime = attributes.creationTime();

      // Parse the UTC timestamp to an Instant
      Instant instant = Instant.parse(creationTime.toString());

      // Get the system's default time zone
      ZoneId localZoneId = ZoneId.systemDefault();

      // Convert the Instant to a ZonedDateTime in the local time zone
      ZonedDateTime localDateTime = instant.atZone(localZoneId);

      // Format the local date-time for display
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
      insoFileCreationTime = localDateTime.format(formatter);

    } catch (Exception e) {
        rc = false;
        e.printStackTrace();
    }
      
    return rc;
  }

  //-----------------------------------------------
  // Convert the 'inso CLI' lines into a JUnit XML
  //-----------------------------------------------
  public boolean convertInsoLogToXML(boolean bReplaceBlank) throws Exception
  {  
    boolean             rc                = true;
    boolean             bTCFound          = false;
    boolean             executeNext       = true;
    List<InsoTestCase>  insoTestcases     = new ArrayList<>();
    Attr                attr              = null;
    Element             testSuite         = null;
    Element             testCase          = null;
    Element             sysErr            = null;
    Element             failure           = null;
    String              lineInso          = "";
    String              testSuiteName     = "";
    String              sep               = "";
    String              tmplineInso       = "";
    String[]            words             = null;
    int                 nb                = 0;
    int                 nb2               = 0;
    int                 index             = 0;
    int                 indexError        = 0;
    int                 nbLine            = 0;
    int                 testSuitesTotal   = 0;
    int                 testSuitesFailed  = 0;
    
    try {
      Iterator<String> iteratorLineInso = this.linesInsoInput.iterator();
      
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
        if (lineInso.startsWith(insoRunningReq)){
          tmplineInso = lineInso.substring(insoRunningReq.length());
          // Split by whitespace
          words = tmplineInso.split("\\s+");
          testSuiteName = "";
          // Replace ' ' by '_' except for the last word that is separeted by '.'
          for (nb = 0; nb < words.length; nb++)
          {
            if (bReplaceBlank) {
              sep = (nb == 0) ? "" : (nb == words.length - 1) ? "." : "_";
            }
            else
            {
              sep = (nb == 0) ? "" : (nb == words.length - 1) ? "." : " ";
            }
            
            testSuiteName += sep + words[nb];
          }
          if (!testSuiteName.isEmpty()) {
              // Appending <testsuite> element to the <testsuites> element
              testSuite = this.doc.createElement("testsuite");
              this.rootElement.appendChild(testSuite);
              // Setting attribute to the sub element
              attr = this.doc.createAttribute("name");
              attr.setValue(testSuiteName);
              testSuite.setAttributeNode(attr);            
          }
        }
        //-----------------------------------------------------------
        // Else If the line starts with: "[network] Response failed"
        //-----------------------------------------------------------        
        else if (lineInso.startsWith(insoNetworkRes + insoFailedRes)){
          if (testSuite == null) {
            throw new Exception("Line #" + nbLine + ": Found '" + insoNetworkRes + insoFailedRes + 
                                "' but there is no 'testsuite'");
          }
          
          // If the line contains 'err=''
          index = lineInso.lastIndexOf(insoErrRes);
          if (index == -1){
            throw new Exception("Line #" + nbLine + ": Found '" + insoNetworkRes + insoFailedRes + 
                                "' but there is no '" + insoErrRes + "' detail");
          }
          // Appending <system-err> element to the <testsuite> element
          sysErr = this.doc.createElement("system-err");
          testSuite.appendChild(sysErr);
          sysErr.appendChild(this.doc.createTextNode(lineInso.substring(index + insoErrRes.length())));          
        }
        //-----------------------------------------------------------
        // Else If the line starts with: "Test results:"
        //-----------------------------------------------------------        
        else if (lineInso.startsWith(insoTestResults)){
          insoTestcases.clear();

          // Parse and structure all 'Test results' associated to the <testsuite>
          while (iteratorLineInso.hasNext() && executeNext) {
            
            // Get next inso line
            lineInso = iteratorLineInso.next();
            nbLine++;
            
            // If there is a 'Test Result'
            if (!lineInso.isEmpty()){
              // Appending <testcase> element to the <testsuite> element
              testCase = this.doc.createElement("testcase");
              if (testSuite == null){
                throw new Exception("Line #" + nbLine + " invalid 'testSuite' Java object");
              }
              testSuite.appendChild(testCase);
              // If the 'Test Result' hasn't the right format (less than 2 chars, and not starting by '✅ ' nor by '❌ ')
              if (lineInso.length() < 2 ||
                  ( !lineInso.startsWith("✅ ") &&
                    !lineInso.startsWith("❌ "))) { 
                throw new Exception("Line #" + nbLine + "' Found: '" + lineInso + "' invalid '" + insoTestResults);
              }
              tmplineInso = lineInso.substring(2);
              // Adding 'name' and 'classname' attributes to <testcase>
              attr = this.doc.createAttribute("name");
              attr.setValue(tmplineInso);
              testCase.setAttributeNode(attr);
              attr = this.doc.createAttribute("classname");
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
        else if (lineInso.startsWith(insoTotalTests)){
          tmplineInso = lineInso.substring(insoTotalTests.length());
          if (testSuite == null){
            throw new Exception("Line #" + nbLine + " invalid 'testSuite' Java object");
          }
          // Adding 'tests' attributes to <testsuite>
          attr = this.doc.createAttribute("tests");
          attr.setValue(tmplineInso);
          testSuitesTotal += Integer.parseInt(tmplineInso);
          testSuite.setAttributeNode(attr);          
        }
        //-----------------------------------------------------------
        // Else If the line starts with: "Failed:"
        //-----------------------------------------------------------        
        else if (lineInso.startsWith(insoFailedTest)){
          tmplineInso = lineInso.substring(insoFailedTest.length());
          if (testSuite == null){
            throw new Exception("Line #" + nbLine + " invalid 'testSuite' Java object");
          }
          // Adding 'tests' attributes to <testsuite>
          attr = this.doc.createAttribute("failures");
          attr.setValue(tmplineInso);
          testSuitesFailed += Integer.parseInt(tmplineInso);
          testSuite.setAttributeNode(attr);          
        }
        //------------------------------------------------------------
        // Else If the line starts with: "***Error: "
        //   example: ReferenceError, AssertionError, TypeError, etc.
        //------------------------------------------------------------       
        else if (lineInso.matches(insoRegExErrorTest)) 
        {
          indexError = 0;
          NodeList nList = testSuite.getElementsByTagName("testcase");

          // Parse and structure all "***Error: "
          while (executeNext) {
            
            // If the line starts with: "***Error: "
            if (lineInso.matches(insoRegExErrorTest)){
              
              Pattern pattern = Pattern.compile(insoRegExErrorTest);
              Matcher matcher = pattern.matcher(lineInso);
              if (!matcher.matches() || matcher.groupCount() != 3) {
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
              // Appending <failure> element to the <testcase> element
              failure = this.doc.createElement("failure");
              nTestCase.appendChild(failure);
              
              // Setting 'type' and 'message' attributes to the <failure> element
              attr = this.doc.createAttribute("type");
              attr.setValue(matcher.group(1));
              failure.setAttributeNode(attr);
              
              attr = this.doc.createAttribute("message");
              attr.setValue(matcher.group(3));
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
            // Else it's the end of the '***Error: ' section (and now we are at "Running request: " level)
            else{
              executeNext = false;
            }            
          }                  
        }
      }
    } catch (Exception e) {
      rc = false;
      e.printStackTrace();
    }

    // Adding 'tests' and 'failures' attributes to <testsuites>
    attr = this.doc.createAttribute("tests");
    attr.setValue(Integer.toString(testSuitesTotal));
    this.rootElement.setAttributeNode(attr);
    attr = this.doc.createAttribute("failed");
    attr.setValue(Integer.toString(testSuitesFailed));
    this.rootElement.setAttributeNode(attr);

    // Adding 'timestamp' attribute to <testsuites>
    attr = this.doc.createAttribute("timestamp");
    attr.setValue(insoFileCreationTime.toString());
    this.rootElement.setAttributeNode(attr);    

    return rc;
  }

  //------------------------------------------------
  // Dump the JUnit XML in the Console or in a File
  //------------------------------------------------
  public boolean dumpXML (String junitXMLFileName) throws Exception
  {
    boolean rc = true;

    try{
      // Writing the content into XML file
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      // Enable pretty-printing with indentation
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      // Add a carriage return for line separators
      transformer.setOutputProperty("{http://xml.apache.org/xslt}line-separator", "\r\n");
      
      DOMSource source = new DOMSource(this.doc);

      if (junitXMLFileName.isEmpty()){
        // Output XML to the Standard Out
        StreamResult consoleResult = new StreamResult(System.out);
        transformer.transform(source, consoleResult);
      }
      else{
        // Output XML to a File
        StreamResult result = new StreamResult(new File(junitXMLFileName));
        transformer.transform(source, result);
      }      
    } catch (Exception e) {
      rc = false;
      e.printStackTrace();
    }
    return rc;

  }
}
