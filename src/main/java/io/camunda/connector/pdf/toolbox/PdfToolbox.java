package io.camunda.connector.pdf.toolbox;

import io.camunda.filestorage.FileVariable;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.blend.BlendMode;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfToolbox {
  public static final String ERROR_DURING_OPERATION = "ERROR_DURING_OPERATION";
  public static final String ERROR_DURING_OPERATION_LABEL = "An error occur during the operation";

  private static final Logger logger = LoggerFactory.getLogger(PdfToolbox.class.getName());

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
    float width = page.getMediaBox().getWidth();
    float height = page.getMediaBox().getHeight();
    float stringWidth = writerOption.font.getStringWidth(text) / 1000 * writerOption.fontHeight;

    float x = (width / 2) - (stringWidth / 2);

    float y = switch (writerOption.textPosition) {
      case TOP -> height - 25;
      case CENTER -> height / 2;
      case BOTTOM -> 5;
    };

    contentStream.setFont(writerOption.font, writerOption.fontHeight);

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
      int xText = (int) (width / 2.0 - Math.cos(Math.toRadians(writerOption.degree)) * stringWidth / 2.0);
      int yText = (int) (height / 2.0 - Math.sin(Math.toRadians(writerOption.degree)) * stringWidth / 2.0);

      Matrix matrix = Matrix.getRotateInstance(Math.toRadians(writerOption.degree), xText, yText);

      contentStream.setTextMatrix(matrix);

    } else {
      contentStream.newLineAtOffset(x, y);
    }
    contentStream.showText(text);
    contentStream.endText();
    contentStream.close();
  }

  private static void savePdfInFile(FileVariable sourceFileVariable) {
    File tempFile = null;
    FileOutputStream fos = null;
    try {
      tempFile = File.createTempFile("temp", ".pdf");
      // Write the byte array to the temporary file
      fos = new FileOutputStream(tempFile);
      fos.write(sourceFileVariable.getValue());
      logger.error("File saved [{}]", tempFile.getAbsolutePath());
    } catch (Exception e) {
      logger.error("Error during saving file[{}] : {}", tempFile == null ? "null" : tempFile.getAbsolutePath(),
          e.getMessage());
    } finally {
      if (fos != null) {
        try {
          fos.close();
        } catch (IOException e) {
          // don't care
        }
      }
    }
  }

  public static String getLogSignature(PdfSubFunction pdfSubFunction) {
    return "Connector [" + pdfSubFunction.getSubFunctionName() + "]:";
  }

  /**
   * A FileVariable may have a complete long name, containing http:/// in case of a URL document. This extract the name
   *
   * @param fileVariable file variable to use
   * @param removePrefix if true, the doc name does ont contains any prefix (myDoc.pdf=WmyName
   * @return a document name
   */
  public static String getDocumentName(FileVariable fileVariable, boolean removePrefix) {
    String completeName = fileVariable.getName();
    int index = completeName.lastIndexOf("/");
    if (index > -1)
      completeName = completeName.substring(index + 1);
    index = completeName.lastIndexOf("\\");
    if (index > -1)
      completeName = completeName.substring(index + 1);
    if (removePrefix) {
      index = completeName.lastIndexOf(".");
      if (index > -1)
        completeName = completeName.substring(0, index);
    }
    return completeName;
  }

  public enum TEXT_POSITION {TOP, CENTER, BOTTOM}

  public static class WriterOption {
    PDFont font = PDType1Font.HELVETICA_BOLD;
    Color color = Color.gray;
    TEXT_POSITION textPosition = TEXT_POSITION.TOP;
    int degree = 0;
    int fontHeight = 30;

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

    public void setFontHeight(int fontHeight) {
      this.fontHeight = fontHeight;
    }

    public String getSynthesis() {
      return "TextPosition[" + textPosition // textPosition
          + "] Font[" + (font == null ? "noFont" : font.getName()) // font
          + "] FontHeigh[" + fontHeight  // font height
          + "] Color[" + (color == null ? "noColor" : color.toString()) // color
          + "] Rotation[" + degree + "]";
    }

  }

}
