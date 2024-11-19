package com.kong.insoclireport2junitxml;

public class InsoCliJunit {

  private static String versionInsoCliJunit = "1.0.0";
  
  public static void main(String[] args) throws Exception {
    InsoTool  insoTool      = new InsoTool();
    boolean   rc            = true;
    boolean   bHelp         = false;
    boolean   bReplaceBlank = false;
    boolean   bVersion      = false;
    String    inputFile     = "";
    String    outputFile    = "";
    
    try {
      int nb =  0;
      while (nb < args.length) {
        if (args[nb].compareToIgnoreCase("--help") == 0){
          bHelp = true;
        }
        else if (args[nb].compareToIgnoreCase("--input") == 0){
          if (nb + 1 < args.length) {
            nb++;
            inputFile = args[nb];
          }        
        }
        else if (args[nb].compareToIgnoreCase("--output") == 0){
          if (nb + 1 < args.length) {
            nb++;
            outputFile = args[nb];
          }        
        }
        else if (args[nb].compareToIgnoreCase("--replaceBlank") == 0){
          bReplaceBlank = true;
        }
        else if (args[nb].compareToIgnoreCase("--version") == 0){
          bVersion = true;
        }
        nb++;
      }
      if (bVersion){
        rc = false;
        System.out.println(versionInsoCliJunit);
      }
      else if (args.length == 0 || bHelp || inputFile.isEmpty()){
        rc = false;
        System.out.println("Usage: java -jar InsoCliJunit.jar [command] [options]");
        System.out.println("");
        System.out.println("With this tool you can convert the 'inso CLI' report into a JUnit XML format");
        System.out.println("Only '--reporter spec' is supported. The 'spec' is the default reporter");
        System.out.println("The 'inso CLI' report is returned by calling 'inso run collection' (For instance: 'inso run collection wrk_XYZ > inso-cli-report.log')");
        System.out.println("");
        System.out.println("  Examples:");
        System.out.println("  $ java -jar InsoCliJunit.jar --input inso-cli-report.log");
        System.out.println("  $ java -jar InsoCliJunit.jar --input inso-cli-report.log --reporter spec");
        System.out.println("  $ java -jar InsoCliJunit.jar --input inso-cli-report.log --output inso-cli-junit.xml");
        System.out.println("");
        System.out.println("Available Commands:");
        System.out.println("  --input          'inso CLI' report file");
        System.out.println("  --help           Display help");
        System.out.println("  --version        Output the version number");
        System.out.println("");
        System.out.println("Available Options:");
        System.out.println("  --output         'JUnit XML' file. If omitted, the standard out ('stdout') is used");
        System.out.println("  --replaceBlank    Replace the blank ' ' character by '_' in the 'name' XML attribute");
      }      

      // Read the 'inso CLI' input file and put it in the memory
      if (rc){
        rc = insoTool.readInsoIntput (inputFile);
      }

      // Convert the 'inso CLI' lines into JUnit XML
      if (rc){
        rc = insoTool.convertInsoLogToXML (bReplaceBlank);
      }
      
      // Dump the JUnit XML in the Console or in a File
      if (rc){
        rc = insoTool.dumpXML(outputFile);
      }
      
      // If successful, return 0
      if (rc) {
        System.exit(0);
      } 
      else {        
        System.exit(1);
      }
    } catch (Exception e) {
      // Print the error and exit with a non-zero code
      e.printStackTrace();
      System.exit(1);
    }
  }
}