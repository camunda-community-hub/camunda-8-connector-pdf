package io.camunda.connector.pdf.mergedocument;

import io.camunda.connector.cherrytemplate.CherryOutput;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PdfMergeDocumentOutput implements CherryOutput {
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

  public List<Map<String, Object>> getOutputParameters() {
    return Arrays.asList(Map.of("name", PdfMergeDocumentOutput.OUTPUT_DESTINATION_FILE, // name
        "label", "Destination variable name", // label
        "class", String.class, // class
        "level", "REQUIRED", // level
        "explanation", "Process variable where the file reference is saved"));
  }
}
