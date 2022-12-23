package io.camunda.connector.pdf.extractpages;

import javax.validation.constraints.NotEmpty;

public class PdfExtractPagesInput {

  public static final String INPUT_SOURCE_FILE = "sourceFile";
  public static final String INPUT_EXTRACT_EXPRESSION = "extractExpression";
  public static final String INPUT_DESTINATION_FILE_NAME = "destinationFileName";
  public static final String INPUT_DESTINATION_STORAGEDEFINITION = "destinationStorageDefinition";
  @NotEmpty String sourceFile;
  @NotEmpty String extractExpression;
  @NotEmpty String destinationFileName;
  String destinationStorageDefinition;

  public String getSourceFile() {
    return sourceFile;
  }

  public String getExtractExpression() {
    return extractExpression == null || extractExpression.trim().isEmpty() ? "1-n" : extractExpression;
  }

  public String getDestinationFileName() {
    return destinationFileName;
  }

  public String getDestinationStorageDefinition() {
    return destinationStorageDefinition;
  }
}
