package io.camunda.connector.pdf.mergedocument;

import io.camunda.connector.cherrytemplate.CherryInput;
import io.camunda.connector.pdf.toolbox.PdfToolbox;
import jakarta.validation.constraints.NotEmpty;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PdfMergeDocumentInput implements CherryInput {

  public static final String INPUT_SOURCE_FILE = "sourceFile";
  public static final String INPUT_FILE_TO_ADD = "fileToAdd";
  public static final String INPUT_DESTINATION_FILE_NAME = "destinationFileName";
  public static final String INPUT_DESTINATION_STORAGEDEFINITION = "destinationStorageDefinition";
  public static final String LABEL = "label";
  public static final String NAME = "name";
  public static final String CLASS = "class";
  public static final String LEVEL = "level";
  public static final String EXPLANATION = "explanation";
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

  @Override
  public List<Map<String, Object>> getInputParameters() {
    return Arrays.asList(Map.of(NAME, PdfMergeDocumentInput.INPUT_SOURCE_FILE, // name
            PdfToolbox.LABEL, "Source file", // label
            PdfToolbox.CLASS, Object.class, // class
            PdfToolbox.LEVEL, PdfToolbox.LEVEL_REQUIRED, // level
            PdfToolbox.EXPLANATION, "FileVariable for the first file"),
        Map.of(NAME, PdfMergeDocumentInput.INPUT_FILE_TO_ADD, // name
            PdfToolbox.LABEL, "File to add", // label
            PdfToolbox.CLASS, Object.class, // class
            PdfToolbox.LEVEL, PdfToolbox.LEVEL_REQUIRED, // level
            PdfToolbox.EXPLANATION, "FileVariable for the first file"),
        Map.of(PdfToolbox.NAME, PdfMergeDocumentInput.INPUT_DESTINATION_FILE_NAME, // name
            PdfToolbox.LABEL, "Destination file name", // label
            PdfToolbox.CLASS, String.class, // class
            PdfToolbox.LEVEL, PdfToolbox.LEVEL_REQUIRED, // level
            PdfToolbox.EXPLANATION, "Name of the new file created"),
        Map.of(PdfToolbox.NAME, PdfMergeDocumentInput.INPUT_DESTINATION_STORAGEDEFINITION, // name
            PdfToolbox.LABEL, "Storage Destination", // label
            PdfToolbox.CLASS, String.class, // class
            PdfToolbox.LEVEL, PdfToolbox.LEVEL_OPTIONAL, // level
            PdfToolbox.EXPLANATION,
            "Storage Definition use to describe how to save the file. If not set, the storage used to read the source file is used."));

  }
}
