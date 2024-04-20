package io.camunda.connector.pdf.watermark;

import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.connector.cherrytemplate.CherryConnector;
import io.camunda.connector.pdf.extractpages.PdfExtractPagesInput;
import io.camunda.connector.pdf.extractpages.PdfExtractPagesOutput;
import io.camunda.connector.pdf.toolbox.PdfToolbox;
import io.camunda.filestorage.FileRepoFactory;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.FileVariableReference;
import io.camunda.filestorage.StorageDefinition;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;
import java.util.Map;

@OutboundConnector(name = PdfWatermarkFunction.TYPE_PDF_WATERMARK, inputVariables = {
    PdfWatermarkInput.INPUT_SOURCE_FILE, // PDF source file
    PdfWatermarkInput.INPUT_WATERMARK, // text to add
    PdfWatermarkInput.INPUT_WATERMARK_POSITION, // position in the page
    PdfWatermarkInput.INPUT_WATERMARK_COLOR, // color
    PdfWatermarkInput.INPUT_WATERMARK_ROTATION, // rotation in degree
    PdfWatermarkInput.INPUT_DESTINATION_FILE_NAME, // destination file name
    PdfWatermarkInput.INPUT_DESTINATION_STORAGEDEFINITION }, type = PdfWatermarkFunction.TYPE_PDF_WATERMARK)

public class PdfWatermarkFunction implements OutboundConnectorFunction, CherryConnector {
  public static final String ERROR_OPERATION_ERROR = "OPERATION_ERROR";
  public static final String ERROR_DEFINITION_ERROR = "DEFINITION_ERROR";
  public static final String ERROR_INVALID_COLOR = "INVALID_COLOR";
  public static final String TYPE_PDF_WATERMARK = "c-pdf-watermark";

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

  Logger logger = LoggerFactory.getLogger(PdfWatermarkFunction.class.getName());

  @Override
  public PdfWatermarkOutput execute(OutboundConnectorContext context) throws ConnectorException {
    PdfWatermarkInput pdfWatermarkInput = context.bindVariables(PdfWatermarkInput.class);
    FileRepoFactory fileRepoFactory = FileRepoFactory.getInstance();
    FileVariableReference docSourceReference;
    PDDocument sourceDocument = null;

    try {
      docSourceReference = FileVariableReference.fromJson(pdfWatermarkInput.getSourceFile());
      FileVariable docSource = fileRepoFactory.loadFileVariable(docSourceReference);

      String destinationFileName = pdfWatermarkInput.getDestinationFileName();
      String destinationStorageDefinitionSt = pdfWatermarkInput.getDestinationStorageDefinition();

      // get the file

      if (docSource == null || docSource.getValue() == null) {
        throw new ConnectorException(PdfToolbox.ERROR_LOAD_ERROR,
            getLogSignature() + "Can't read file[" + pdfWatermarkInput.getSourceFile() + "]");
      }
      StorageDefinition destinationStorageDefinition;
      if (destinationStorageDefinitionSt != null && !destinationStorageDefinitionSt.trim().isEmpty()) {
        try {
          destinationStorageDefinition = StorageDefinition.getFromString(destinationStorageDefinitionSt);
        } catch (Exception e) {
          throw new ConnectorException(ERROR_DEFINITION_ERROR,
              getLogSignature() + "Can't decode StorageDefinition [" + destinationStorageDefinitionSt + "]");
        }
      } else {
        destinationStorageDefinition = docSource.getStorageDefinition();
      }

      String watermark = pdfWatermarkInput.getWatermark();
      PdfToolbox.WriterOption writerOption = PdfToolbox.WriterOption.getInstance();

      PdfToolbox.TEXT_POSITION textPosition = PdfToolbox.TEXT_POSITION.valueOf(
          pdfWatermarkInput.getWatermarkPosition());
      // textPosition is never null
      writerOption.setTextPosition(textPosition);

      String watermarkColorSt = pdfWatermarkInput.getWatermarkColor();
      if (watermarkColorSt != null) {
        Color color = getColorFromString(watermarkColorSt);
        if (color == null)
          throw new ConnectorException(ERROR_INVALID_COLOR,
              getLogSignature() + "Color [" + watermarkColorSt + "] is unknown]");

        writerOption.setColor(color);
      }
      Long watermarkRotation = pdfWatermarkInput.getWatermarkRotation();
      if (watermarkRotation != null) {
        writerOption.setRotation(watermarkRotation.intValue() % 360);
      }

      sourceDocument = PdfToolbox.loadPdfDocument(docSource, getName());

      for (int i = 0; i < sourceDocument.getNumberOfPages(); i++) {
        PdfToolbox.addWatermarkText(sourceDocument, sourceDocument.getPage(i), writerOption, watermark);
      }

      FileVariable outputFileVariable = PdfToolbox.saveOutputPdfDocument(sourceDocument, destinationFileName,
          destinationStorageDefinition, getName());
      FileVariableReference outputFileReference = fileRepoFactory.saveFileVariable(outputFileVariable);

      PdfWatermarkOutput pdfWatermarkOutput = new PdfWatermarkOutput();
      pdfWatermarkOutput.destinationFile = outputFileReference.toJson();
      logger.info("{} finish Watermark [{}] document[{}] to [{}] ", getLogSignature(), watermark,
          pdfWatermarkInput.getSourceFile(), pdfWatermarkInput.getDestinationFileName());
      return pdfWatermarkOutput;

    } catch (Exception e) {
      logger.error("{} exception during extraction ", getLogSignature(), e);
      throw new ConnectorException(ERROR_OPERATION_ERROR,
          getLogSignature() + "Can't execute watermark operation on [" + pdfWatermarkInput.getSourceFile() + "] : "
              + e);

    } finally {
      if (sourceDocument != null)
        try {
          sourceDocument.close();
        } catch (Exception e) {
          // don't care
        }
    }

  }

  public String getName() {
    return "PDF Add watermark";
  }

  private String getLogSignature() {
    return "Connector [" + getName() + "]:";
  }

  /**
   * return the color from a string. Constant are accepted and decoded.
   *
   * @param colorSt color to return
   * @return the Color object
   */
  private Color getColorFromString(String colorSt) {
    if (COLOR_RED.equalsIgnoreCase(colorSt))
      return Color.red;
    if (COLOR_GREEN.equalsIgnoreCase(colorSt))
      return Color.green;
    if (COLOR_BLACK.equalsIgnoreCase(colorSt))
      return Color.black;
    if (COLOR_BLUE.equalsIgnoreCase(colorSt))
      return Color.blue;
    if (COLOR_CYAN.equalsIgnoreCase(colorSt))
      return Color.cyan;
    if (COLOR_GRAY.equalsIgnoreCase(colorSt))
      return Color.gray;
    if (COLOR_DARKGRAY.equalsIgnoreCase(colorSt))
      return Color.darkGray;
    if (COLOR_LIGHTGRAY.equalsIgnoreCase(colorSt))
      return Color.lightGray;
    if (COLOR_MAGENTA.equalsIgnoreCase(colorSt))
      return Color.magenta;
    if (COLOR_ORANGE.equalsIgnoreCase(colorSt))
      return Color.orange;
    if (COLOR_PINK.equalsIgnoreCase(colorSt))
      return Color.pink;
    if (COLOR_WHITE.equalsIgnoreCase(colorSt))
      return Color.white;
    if (COLOR_YELLOW.equalsIgnoreCase(colorSt))
      return Color.yellow;
    return Color.getColor(colorSt);
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public String getLogo() {
    return PdfToolbox.getLogo();
  }

  @Override
  public String getCollectionName() {
    return PdfToolbox.getCollectionName();
  }

  @Override
  public Map<String, String> getListBpmnErrors() {
    return null;
  }

  @Override
  public Class<PdfExtractPagesInput> getInputParameterClass() {
    return null;
  }

  @Override
  public Class<PdfExtractPagesOutput> getOutputParameterClass() {
    return null;
  }

  @Override
  public List<String> appliesTo() {
    return null;
  }
}
