package io.camunda.connector.pdf.extractpages;

import io.camunda.connector.cherrytemplate.CherryOutput;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PdfExtractPagesOutput implements CherryOutput {
  public static final String OUTPUT_DESTINATION_FILE = "destinationFile";

  String destinationFile;

  /**
   * Attention, the first letter after the get must be in lower case
   *
   * @return the variable
   */
  public String getdestinationFile() {
    return destinationFile;
  }

  /**
   * this method is exploded by Cherry Runtime to produce a nice element-template
   *
   * @return list of parameters
   */
  public List<Map<String, Object>> getOutputParameters() {
    return Arrays.asList(Map.of("name", PdfExtractPagesOutput.OUTPUT_DESTINATION_FILE, // name
        "label", "Destination variable name", // label
        "class", String.class, // class
        "level", "REQUIRED", // level
        "explanation", "Process variable where the file reference is saved"));
  }

}
