package io.camunda.connector.pdf.toolbox;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.file.storage.FileVariable;
import io.camunda.file.storage.StorageDefinition;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.blend.BlendMode;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PdfToolbox {
  public static final String ERROR_SAVE_ERROR = "SAVE_ERROR";
  public static final String ERROR_LOAD_ERROR = "LOAD_ERROR";
  public static final String ERROR_ENCRYPTED_PDF_NOT_SUPPORTED = "ENCRYPTED_PDF_NOT_SUPPORTED";

  /**
   * Load a PDDocument from a file variable
   *
   * @param sourceFileVariable the source document to load
   * @return a PDocument
   */
  public static PDDocument loadPdfDocument(FileVariable sourceFileVariable, String connectorName)
      throws ConnectorException {
    PDDocument sourceDocument;
    try {
      sourceDocument = PDDocument.load(sourceFileVariable.getValue());

    } catch (Exception e) {
      throw new ConnectorException(ERROR_LOAD_ERROR,
          "Connector[" + connectorName + "] Can't load document [" + sourceFileVariable.getName() + "]");
    }
    if (sourceDocument.isEncrypted()) {
      throw new ConnectorException(ERROR_ENCRYPTED_PDF_NOT_SUPPORTED,
          "Connector[" + connectorName + "] Document is encrypted");
    }
    return sourceDocument;
  }

  /**
   * Save a ¨pdfDocument in an Output Parameter of the worker
   *
   * @param pdDocument                   Pdf document to save
   * @param fileName                     name of the Output document
   * @param destinationStorageDefinition how to save the document
   * @return a FileVariable
   */
  public static FileVariable saveOutputPdfDocument(PDDocument pdDocument,
                                                   String fileName,
                                                   StorageDefinition destinationStorageDefinition,
                                                   String connectorName) throws ConnectorException {
    FileVariable fileVariableOut = new FileVariable();
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    try {
      pdDocument.save(byteArrayOutputStream);

      fileVariableOut.setValue(byteArrayOutputStream.toByteArray());
      fileVariableOut.setName(fileName);
      fileVariableOut.setStorageDefinition(destinationStorageDefinition);
      return fileVariableOut;

    } catch (Exception e) {
      throw new ConnectorException(ERROR_SAVE_ERROR,
          "Connector [" + connectorName + "] cannot save destination[" + fileName + "] : " + e);
    }
  }

  /**
   * Add a text in background on a page
   *
   * @param doc  pdfDocument
   * @param page page to add the text
   * @param text test to add
   * @throws IOException can't add the text
   */

  public static void addWatermarkText(PDDocument doc, PDPage page, WriterOption writerOption, String text)
      throws IOException {
    PDPageContentStream contentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true,
        true);

    /*
     * (0,0) is as the bottom left (classical
     */
    float fontHeight = 30;
    float width = page.getMediaBox().getWidth();
    float height = page.getMediaBox().getHeight();
    float stringWidth = writerOption.font.getStringWidth(text) / 1000 * fontHeight;

    float x = (width / 2) - (stringWidth / 2);

    float y = switch (writerOption.textPosition) {
      case TOP -> height - 25;
      case CENTER -> height / 2;
      case BOTTOM -> 5;
    };

    contentStream.setFont(writerOption.font, fontHeight);

    PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
    gs.setNonStrokingAlphaConstant(2f);
    gs.setStrokingAlphaConstant(0.2f);
    gs.setBlendMode(BlendMode.MULTIPLY);
    gs.setLineWidth(3f);
    contentStream.setGraphicsStateParameters(gs);

    contentStream.setNonStrokingColor(writerOption.color);
    contentStream.setStrokingColor(writerOption.color);

    contentStream.beginText();

    if (writerOption.degree != 0 && writerOption.textPosition == TEXT_POSITION.CENTER) {
      /*
       * Center of the page is (width/2, height / 2)
       * If we start the at this position, then the text will not be center.
       * So we have to move back stringWidth pixel. Transform that in x,y according the degree
       * x =>
       *    - start at the middle of the page (width/2)
       *    - minus vector, projection on the X axis for this vector
       *     Vector is
       *      calculated the half length of the string (stringWidth/2)
       *     Projection on the X axis
       *      use the cosinus( rotation )
       *
       */
      int xText = (int) (((double) width) / 2.0
          - Math.cos(Math.toRadians(writerOption.degree)) * ((double) stringWidth) / 2.0);
      int yText = (int) (((double) height) / 2.0
          - Math.sin(Math.toRadians(writerOption.degree)) * ((double) stringWidth) / 2.0);

      Matrix matrix = Matrix.getRotateInstance(Math.toRadians(writerOption.degree), xText, yText);

      contentStream.setTextMatrix(matrix);

    } else {
      contentStream.newLineAtOffset(x, y);
    }
    contentStream.showText(text);
    contentStream.endText();
    contentStream.close();
  }

  public enum TEXT_POSITION {TOP, CENTER, BOTTOM}

  public static class WriterOption {
    PDFont font = PDType1Font.HELVETICA_BOLD;
    Color color = Color.gray;
    TEXT_POSITION textPosition = TEXT_POSITION.TOP;
    int degree = 0;

    public static WriterOption getInstance() {
      return new WriterOption();
    }

    public WriterOption setFont(PDFont font) {
      this.font = font;
      return this;
    }

    public WriterOption setColor(Color color) {
      this.color = color;
      return this;
    }

    public WriterOption setTextPosition(TEXT_POSITION textPosition) {
      this.textPosition = textPosition;
      return this;
    }

    public WriterOption setRotation(int degree) {
      this.degree = degree;
      return this;
    }
  }

}
