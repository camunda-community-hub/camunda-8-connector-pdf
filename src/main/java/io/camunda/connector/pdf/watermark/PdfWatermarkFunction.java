package io.camunda.connector.pdf.watermark;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.CherryInput;
import io.camunda.connector.pdf.PdfInput;
import io.camunda.connector.pdf.PdfOutput;
import io.camunda.connector.pdf.sharedfunctions.LoadDocument;
import io.camunda.connector.pdf.sharedfunctions.LoadPdfDocument;
import io.camunda.connector.pdf.sharedfunctions.RetrieveStorageDefinition;
import io.camunda.connector.pdf.sharedfunctions.SavePdfDocument;
import io.camunda.connector.pdf.toolbox.PdfParameter;
import io.camunda.connector.pdf.toolbox.PdfSubFunction;
import io.camunda.connector.pdf.toolbox.PdfToolbox;
import io.camunda.filestorage.FileRepoFactory;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.StorageDefinition;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PdfWatermarkFunction implements PdfSubFunction {
  public static final String ERROR_INVALID_COLOR = "INVALID_COLOR";
  private static final Map<String, String> listBpmnErrors = new HashMap<>();

  static {
    listBpmnErrors.putAll(LoadDocument.getBpmnErrors());
    listBpmnErrors.putAll(RetrieveStorageDefinition.getBpmnErrors());
    listBpmnErrors.putAll(LoadPdfDocument.getBpmnErrors());
    listBpmnErrors.putAll(SavePdfDocument.getBpmnErrors());
    listBpmnErrors.put(PdfToolbox.ERROR_DURING_OPERATION, PdfToolbox.ERROR_DURING_OPERATION_LABEL);
    listBpmnErrors.put(ERROR_INVALID_COLOR, "Invalid color");
  }

  Logger logger = LoggerFactory.getLogger(PdfWatermarkFunction.class.getName());

  /**
   * Execute the sub-function
   *
   * @param pdfInput the input of connector
   * @param context  outbound connector execution
   * @return an Output object
   * @throws Exception in case of any error
   */
  @Override
  public PdfOutput executeSubFunction(PdfInput pdfInput, OutboundConnectorContext context) throws ConnectorException {
    logger.debug("{} Start Watermark", PdfToolbox.getLogSignature(this));

    FileRepoFactory fileRepoFactory = FileRepoFactory.getInstance();
    PDDocument sourceDocument = null;

    try {

      FileVariable docSource = LoadDocument.loadDocSource(pdfInput.getSourceFile(), fileRepoFactory, this);

      String destinationFileName = pdfInput.getDestinationFileName();

      StorageDefinition destinationStorageDefinition = RetrieveStorageDefinition.getStorageDefinition(pdfInput,
          docSource, true, this);

      String watermark = pdfInput.getWaterMark();

      PdfToolbox.WriterOption writerOption = getWriterOption(pdfInput);

      logger.info("{} Start Watermark [{}] Options{}", PdfToolbox.getLogSignature(this), watermark,
          writerOption.getSynthesis());

      // load the document now
      sourceDocument = LoadPdfDocument.loadPdfDocument(docSource, this);

      // add the watermark
      for (int i = 0; i < sourceDocument.getNumberOfPages(); i++) {
        PdfToolbox.addWatermarkText(sourceDocument, sourceDocument.getPage(i), writerOption, watermark);
      }

      // produce the result, and save it in the pdfOutput
      // Exception PdfToolbox.ERROR_CREATE_FILEVARIABLE, PdfToolbox.ERROR_SAVE_ERROR
      PdfOutput pdfOutput = SavePdfDocument.savePdfFile(new PdfOutput(), sourceDocument, destinationFileName,
          destinationStorageDefinition, fileRepoFactory, this);
      logger.info("{} finish Watermark [{}] document[{}] to [{}] ", PdfToolbox.getLogSignature(this), watermark,
          pdfInput.getSourceFile(), pdfInput.getDestinationFileName());
      return pdfOutput;
    } catch (ConnectorException ce) {
      // already logged
      throw ce;
    } catch (IOException e) {
      logger.error("{} exception during extraction ", PdfToolbox.getLogSignature(this), e);
      throw new ConnectorException(PdfToolbox.ERROR_DURING_OPERATION,
          PdfToolbox.getLogSignature(this) + "Can't execute watermark operation on [" + pdfInput.getSourceFile()
              + "] : " + e.getMessage());

    } finally {
      if (sourceDocument != null)
        try {
          sourceDocument.close();
        } catch (Exception e) {
          // don't care
        }
    }

  }

  /**
   * calculate the writer option from the input
   *
   * @param pdfInput inputs
   * @return writer option
   */
  private PdfToolbox.WriterOption getWriterOption(PdfInput pdfInput) {
    PdfToolbox.WriterOption writerOption = PdfToolbox.WriterOption.getInstance();

    PdfToolbox.TEXT_POSITION textPosition = PdfToolbox.TEXT_POSITION.valueOf(pdfInput.getWatermarkPosition());
    // textPosition is never null
    writerOption.setTextPosition(textPosition);

    String watermarkColorSt = pdfInput.getWatermarkcolor();
    if (watermarkColorSt != null) {
      Color color = getColorFromString(watermarkColorSt);
      if (color == null)
        throw new ConnectorException(ERROR_INVALID_COLOR,
            PdfToolbox.getLogSignature(this) + "Color [" + watermarkColorSt + "] is unknown]");

      writerOption.setColor(color);
    }
    Long watermarkRotation = pdfInput.getWatermarkRotation();
    if (watermarkRotation != null) {
      writerOption.setRotation(watermarkRotation.intValue() % 360);
    }
    Long fontHeight = pdfInput.getWatermarkFontHeight();
    if (fontHeight != null)
      writerOption.setFontHeight(fontHeight.intValue());
    return writerOption;
  }

  /**
   * return the color from a string. Constant are accepted and decoded.
   *
   * @param colorSt color to return
   * @return the Color object
   */
  private Color getColorFromString(String colorSt) {
    if (PdfInput.COLOR_RED.equalsIgnoreCase(colorSt))
      return Color.red;
    if (PdfInput.COLOR_GREEN.equalsIgnoreCase(colorSt))
      return Color.green;
    if (PdfInput.COLOR_BLACK.equalsIgnoreCase(colorSt))
      return Color.black;
    if (PdfInput.COLOR_BLUE.equalsIgnoreCase(colorSt))
      return Color.blue;
    if (PdfInput.COLOR_CYAN.equalsIgnoreCase(colorSt))
      return Color.cyan;
    if (PdfInput.COLOR_GRAY.equalsIgnoreCase(colorSt))
      return Color.gray;
    if (PdfInput.COLOR_DARKGRAY.equalsIgnoreCase(colorSt))
      return Color.darkGray;
    if (PdfInput.COLOR_LIGHTGRAY.equalsIgnoreCase(colorSt))
      return Color.lightGray;
    if (PdfInput.COLOR_MAGENTA.equalsIgnoreCase(colorSt))
      return Color.magenta;
    if (PdfInput.COLOR_ORANGE.equalsIgnoreCase(colorSt))
      return Color.orange;
    if (PdfInput.COLOR_PINK.equalsIgnoreCase(colorSt))
      return Color.pink;
    if (PdfInput.COLOR_WHITE.equalsIgnoreCase(colorSt))
      return Color.white;
    if (PdfInput.COLOR_YELLOW.equalsIgnoreCase(colorSt))
      return Color.yellow;
    return Color.getColor(colorSt);
  }

  @Override
  public List<PdfParameter> getSubFunctionParameters(TypeParameter typeParameter) {
    switch (typeParameter) {
    case INPUT:
      return Arrays.asList(new PdfParameter(PdfInput.INPUT_SOURCE_FILE, // name
              "Source file", // label
              Object.class, // class
              CherryInput.PARAMETER_MAP_LEVEL_REQUIRED, // level
              "FileVariable for the file to convert", 1),

          new PdfParameter(PdfInput.INPUT_WATERMARK, // name
              "Watermark", // label
              String.class, // class
              CherryInput.PARAMETER_MAP_LEVEL_REQUIRED, // level
              "Watermark to add in each page", 1),

          new PdfParameter(PdfInput.INPUT_WATERMARK_POSITION, // name
              "Position", // label
              String.class, // class
              CherryInput.PARAMETER_MAP_LEVEL_REQUIRED, // level
              "Watermark to add in each page", 1) // Param
              .addChoice(PdfInput.INPUT_WATERMARK_POSITION_TOP, "Top")
              .addChoice(PdfInput.INPUT_WATERMARK_POSITION_CENTER, "Center")
              .addChoice(PdfInput.INPUT_WATERMARK_POSITION_BOTTOM, "Bottom"),

          new PdfParameter(PdfInput.INPUT_WATERMARK_COLOR, // name
              "Color", // label
              String.class, // class
              CherryInput.PARAMETER_MAP_LEVEL_OPTIONAL, // level
              "Color to write the watermark", 1) // param
              .addChoice(PdfInput.COLOR_RED, "red")
              .addChoice(PdfInput.COLOR_GREEN, "green")
              .addChoice(PdfInput.COLOR_BLACK, "black")
              .addChoice(PdfInput.COLOR_BLUE, "blue")
              .addChoice(PdfInput.COLOR_CYAN, "cyan")
              .addChoice(PdfInput.COLOR_GRAY, "gray")
              .addChoice(PdfInput.COLOR_DARKGRAY, "darkGray")
              .addChoice(PdfInput.COLOR_LIGHTGRAY, "lightGray")
              .addChoice(PdfInput.COLOR_MAGENTA, "magenta")
              .addChoice(PdfInput.COLOR_ORANGE, "orange")
              .addChoice(PdfInput.COLOR_PINK, "pink")
              .addChoice(PdfInput.COLOR_WHITE, "white")
              .addChoice(PdfInput.COLOR_YELLOW, "yellow"),


          new PdfParameter(PdfInput.INPUT_WATERMARK_ROTATION, // name
              "Rotation", // label
              Long.class, // class
              CherryInput.PARAMETER_MAP_LEVEL_OPTIONAL, // level
              "Rotation (0-360)", 1),

          new PdfParameter(PdfInput.INPUT_WATERMARK_FONTHEIGHT, // name
              "Font Height", // label
              Long.class, // class
              CherryInput.PARAMETER_MAP_LEVEL_OPTIONAL, // level
              "Font height (30 is small)", 1),

          PdfInput.pdfParameterDestinationFileName, PdfInput.pdfParameterDestinationStorageDefinition);

    case OUTPUT:
      return List.of(PdfOutput.PDF_PARAMETER_DESTINATION_FILE);
    }
    return Collections.emptyList();
  }

  @Override
  public Map<String, String> getSubFunctionListBpmnErrors() {
    return listBpmnErrors;
  }

  @Override
  public String getSubFunctionName() {
    return "Watermark";
  }

  @Override
  public String getSubFunctionDescription() {
    return "Add a watermark";
  }

  @Override
  public String getSubFunctionType() {
    return "watermark";
  }
}
