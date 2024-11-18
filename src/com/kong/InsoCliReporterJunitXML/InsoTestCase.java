package com.kong.InsoCliReporterJunitXML;

public class InsoTestCase {
  
  private String name;
  private boolean passed;

  // Constructor
  public InsoTestCase(String name, boolean passed) {
      this.name = name;
      this.passed = passed;
  }

  // Getter for name
  public String getName() {
      return name;
  }

  // Getter for passed
  public boolean getPassed() {
      return passed;
  }

  // toString method for easy printing
  @Override
  public String toString() {
      return "TestCase{name='" + name + "', passed=" + passed + "}";
  }
}