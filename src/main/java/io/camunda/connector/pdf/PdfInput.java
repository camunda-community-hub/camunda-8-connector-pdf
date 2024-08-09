package io.camunda.connector.pdf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.camunda.connector.cherrytemplate.CherryInput;
import io.camunda.connector.pdf.toolbox.ParameterToolbox;
import io.camunda.connector.pdf.toolbox.PdfParameter;
import io.camunda.connector.pdf.toolbox.PdfSubFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * the JsonIgnoreProperties is mandatory: the template may contain additional widget to help the designer, especially on the OPTIONAL parameters
 * This avoids the MAPPING Exception
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PdfInput implements CherryInput {
  /**
   * Attention, each Input here must be added in the PdfFunction, list of InputVariables
   */
  public static final String INPUT_PDFFUNCTION = "pdfFunction";
  /**
   * Input need for ExtractPages
   */
  public static final String INPUT_SOURCE_FILE = "sourceFile";
  public static final String INPUT_LIST_SOURCE_FILE = "listSourceFile";
  public static final String INPUT_EXTRACT_EXPRESSION = "extractExpression";
  public static final String INPUT_DESTINATION_FILE_NAME = "destinationFileName";
  public static final String INPUT_DESTINATION_STORAGEDEFINITION = "destinationStorageDefinition";
  public static final String INPUT_FILE_TO_ADD = "fileToAdd";
  /**
   * Watermark
   */
  public static final String INPUT_WATERMARK = "waterMark";
  // default PdfToolbox.TEXT_POSITION.CENTER
  public static final String INPUT_WATERMARK_POSITION = "watermarkPosition";
  public static final String INPUT_WATERMARK_POSITION_TOP = "TOP";
  public static final String INPUT_WATERMARK_POSITION_CENTER = "CENTER";
  public static final String INPUT_WATERMARK_POSITION_BOTTOM = "BOTTOM";
  // default 0
  public static final String INPUT_WATERMARK_ROTATION = "watermarkRotation";
  public static final String INPUT_WATERMARK_FONTHEIGHT = "watermarkFontHeight";
  public static final String INPUT_WATERMARK_COLOR = "watermarkColor";
  public static final String COLOR_RED = "red";
  public static final String COLOR_GREEN = "green";
  public static final String COLOR_BLACK = "black";
  public static final String COLOR_BLUE = "blue";
  public static final String COLOR_CYAN = "cyan";
  public static final String COLOR_GRAY = "gray";
  public static final String COLOR_DARKGRAY = "darkGray";
  public static final String COLOR_LIGHTGRAY = "lightGray";
  public static final String COLOR_MAGENTA = "magenta";
  public static final String COLOR_ORANGE = "orange";
  public static final String COLOR_PINK = "pink";
  public static final String COLOR_WHITE = "white";
  public static final String COLOR_YELLOW = "yellow";
  /**
   * PdfToImage
   */
  public static final String INPUT_PDFTOIMAGE_DPI = "dpi";
  public static final PdfParameter pdfParameterDestinationFileName = new PdfParameter(
      PdfInput.INPUT_DESTINATION_FILE_NAME,
      // name
      "Destination file name", // label
      String.class, // class
      CherryInput.PARAMETER_MAP_LEVEL_REQUIRED, // level
      "Name of the new file created", 10);
  public static final PdfParameter pdfParameterDestinationStorageDefinition = new PdfParameter(
      PdfInput.INPUT_DESTINATION_STORAGEDEFINITION, // name
      "Storage Destination", // label
      String.class, // class
      CherryInput.PARAMETER_MAP_LEVEL_OPTIONAL, // level
      "Storage Definition use to describe how to save the file. If not set, the storage used to read the source file is used.",
      10);
  private final Logger logger = LoggerFactory.getLogger(PdfInput.class.getName());
  public Long dpi = 300L;
  private String pdfFunction;
  private String sourceFile;
  private List<String> listSourceFile;
  private String extractExpression;
  private String destinationFileName;
  private String destinationStorageDefinition;
  private String fileToAdd;
  private String waterMark;
  private String watermarkPosition;
  private String watermarkColor;
  private Long watermarkRotation;
  private Long watermarkFontHeight;

  public String getPdfFunction() {
    return pdfFunction;
  }

  public String getSourceFile() {
    return sourceFile;
  }

  public List<String> getListSourceFile() {
    return listSourceFile;
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

  public String getFileToAdd() {
    return fileToAdd;
  }

  public String getWaterMark() {
    return waterMark;
  }

  public String getWatermarkPosition() {
    return watermarkPosition;
  }

  public String getWatermarkcolor() {
    return watermarkColor;
  }

  public Long getWatermarkRotation() {
    return watermarkRotation;
  }

  public Long getWatermarkFontHeight() {
    return watermarkFontHeight;
  }

  public Long getDpi() {
    return dpi;
  }

  @Override
  public List<Map<String, Object>> getInputParameters() {
    return ParameterToolbox.getParameters(PdfSubFunction.TypeParameter.INPUT);
  }
}
