package io.camunda.connector.pdf.extractpages;

import io.camunda.connector.cherrytemplate.CherryInput;
import io.camunda.connector.pdf.toolbox.PdfToolbox;
import jakarta.validation.constraints.NotEmpty;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PdfExtractPagesInput implements CherryInput {

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


  /*
   * this method is exploded by Cherry Runtime to produce a nice element-template
   *
   * @return list of parameters
   */
  @Override
  public List<Map<String, Object>> getInputParameters() {
    return Arrays.asList(Map.of(PdfToolbox.NAME, PdfExtractPagesInput.INPUT_SOURCE_FILE, // name
            PdfToolbox.LABEL, "Source file", // label
            PdfToolbox.CLASS, Object.class, // class
            PdfToolbox.LEVEL, PdfToolbox.LEVEL_REQUIRED, // level
            PdfToolbox.EXPLANATION, "FileVariable for the file to convert"),
        Map.of(PdfToolbox.NAME, PdfExtractPagesInput.INPUT_EXTRACT_EXPRESSION, // name
            PdfToolbox.LABEL, "Extract Expression", // label
            PdfToolbox.CLASS, String.class, // class
            PdfToolbox.LEVEL, PdfToolbox.LEVEL_REQUIRED, // level
            PdfToolbox.EXPLANATION,
            "Extract pilot: example, 2-4 mean extract pages 2 to 4 (document page start at 1). Use \u0027n\u0027 to specify the end of the document (2-n) extract from page 2 to the end. Simple number is accepted to extract a page. Example: 4-5, 10, 15-n or 2-n, 1 (first page to the end)"),
        Map.of(PdfToolbox.NAME, PdfExtractPagesInput.INPUT_DESTINATION_FILE_NAME, // name
            PdfToolbox.LABEL, "Destination file name", // label
            PdfToolbox.CLASS, String.class, // class
            PdfToolbox.LEVEL, PdfToolbox.LEVEL_REQUIRED, // level
            PdfToolbox.EXPLANATION, "Name of the new file created"),
        Map.of(PdfToolbox.NAME, PdfExtractPagesInput.INPUT_DESTINATION_STORAGEDEFINITION, // name
            PdfToolbox.LABEL, "Storage Destination", // label
            PdfToolbox.CLASS, String.class, // class
            PdfToolbox.LEVEL, PdfToolbox.LEVEL_OPTIONAL, // level
            PdfToolbox.EXPLANATION,
            "Storage Definition use to describe how to save the file. If not set, the storage used to read the source file is used."));
  }
}
