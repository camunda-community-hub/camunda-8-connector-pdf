package io.camunda.connector.pdf.mergedocument;

public class PdfMergeDocumentOutput {
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

}
