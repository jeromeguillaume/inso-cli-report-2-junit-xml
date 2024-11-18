package com.kong.insoclireport2junitxml;

public class InsoCliJunit {
  
  public static void main(String[] args) throws Exception {
    boolean   rc        = true;
    InsoTool  insoTool  = new InsoTool();
    
    try {
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
        rc = insoTool.readInsoIntput (args[0]);
      }

      // Convert the 'inso CLI' lines into JUnit XML
      if (rc){
        rc = insoTool.convertInsoLogToXML ();
      }
      
      // Dump the JUnit XML in the Console or in a File
      if (rc){
        rc = insoTool.dumpXML();
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