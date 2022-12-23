package io.camunda.connector.pdf.watermark;

import io.camunda.connector.pdf.toolbox.PdfToolbox;

import javax.validation.constraints.NotEmpty;

public class PdfWatermarkInput {

  public static final String INPUT_SOURCE_FILE = "sourceFile";
  public static final String INPUT_WATERMARK = "watermark";
  // default PdfToolbox.TEXT_POSITION.CENTER
  public static final String INPUT_WATERMARK_POSITION = "watermarkPosition";
  public static final String INPUT_WATERMARK_COLOR = "watermarkColor";
  // default 0
  public static final String INPUT_WATERMARK_ROTATION = "watermarkRotation";
  public static final String INPUT_DESTINATION_FILE_NAME = "destinationFileName";
  public static final String INPUT_DESTINATION_STORAGEDEFINITION = "destinationStorageDefinition";
  @NotEmpty String sourceFile;
  @NotEmpty String watermark;
  String watermarkPosition;
  String watermarkColor;
  Long watermarkRotation;
  @NotEmpty String destinationFileName;
  String destinationStorageDefinition;

  public String getSourceFile() {
    return sourceFile;
  }

  public String getWatermark() {
    return watermark;
  }

  public String getWatermarkPosition() {
    if (watermarkPosition == null || watermarkPosition.trim().isEmpty())
      return PdfToolbox.TEXT_POSITION.CENTER.toString();
    return watermarkPosition;
  }

  public String getWatermarkColor() {
    return watermarkColor;
  }

  public Long getWatermarkRotation() {
    return watermarkRotation;
  }

  public String getDestinationFileName() {
    return destinationFileName;
  }

  public String getDestinationStorageDefinition() {
    return destinationStorageDefinition;
  }
}
