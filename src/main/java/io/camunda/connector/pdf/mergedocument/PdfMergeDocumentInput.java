package io.camunda.connector.pdf.mergedocument;

import javax.validation.constraints.NotEmpty;

public class PdfMergeDocumentInput {

  public static final String INPUT_SOURCE_FILE = "sourceFile";
  public static final String INPUT_FILE_TO_ADD = "fileToAdd";
  public static final String INPUT_DESTINATION_FILE_NAME = "destinationFileName";
  public static final String INPUT_DESTINATION_STORAGEDEFINITION = "destinationStorageDefinition";
  @NotEmpty String sourceFile;
  @NotEmpty String fileToAdd;
  @NotEmpty String destinationFileName;
  String destinationStorageDefinition;

  public String getSourceFile() {
    return sourceFile;
  }

  public String getFileToAdd() {
    return fileToAdd;
  }

  public String getDestinationFileName() {
    return destinationFileName;
  }

  public String getDestinationStorageDefinition() {
    return destinationStorageDefinition;
  }
}
