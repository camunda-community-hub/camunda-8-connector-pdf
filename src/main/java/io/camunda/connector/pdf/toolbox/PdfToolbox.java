package io.camunda.connector.pdf.toolbox;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.StorageDefinition;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfToolbox {
  public static final String ERROR_SAVE_ERROR = "SAVE_ERROR";
  public static final String ERROR_LOAD_ERROR = "LOAD_ERROR";
  public static final String ERROR_ENCRYPTED_PDF_NOT_SUPPORTED = "ENCRYPTED_PDF_NOT_SUPPORTED";
  public static final String LABEL = "label";
  public static final String NAME = "name";
  public static final String CLASS = "class";
  public static final String LEVEL = "level";
  public static final String EXPLANATION = "explanation";
  public static final String LEVEL_REQUIRED = "REQUIRED";
  public static final String LEVEL_OPTIONAL = "OPTIONAL";
  public static final String WORKER_LOGO = "data:image/svg+xml,%3Csvg xmlns\\u003d\\u0027http://www.w3.org/2000/svg\\u0027 width\\u003d\\u002718\\u0027 height\\u003d\\u002718.0\\u0027 viewBox\\u003d\\u00270 0 18 18.0\\u0027 %3E%3Cg id\\u003d\\u0027XMLID_238_\\u0027%3E %3Cpath id\\u003d\\u0027XMLID_239_\\u0027 d\\u003d\\u0027m 14.708846 10.342394 c -1.122852 0.0,-2.528071 0.195852,-2.987768 0.264774 C 9.818362 8.6202,9.277026 7.4907875,9.155265 7.189665 C 9.320285 6.765678,9.894426 5.155026,9.976007 3.0864196 C 10.016246 2.0507226,9.797459 1.2768387,9.325568 0.7862517 C 8.854491 0.29647747,8.284297 0.2583872,8.120788 0.2583872 c -0.573329 0.0,-1.5351098 0.28991616,-1.5351098 2.2313614 c 0.0 1.6845098,0.7853807 3.4719677,1.0024838 3.933813 C 6.444349 9.754026,5.216342 12.03393,4.9555745 12.502801 C 0.35941938 14.233297,0.0 15.906485,0.0 16.380697 c 0.0 0.852155,0.6068903 1.360916,1.6234258 1.360916 c 2.4697742 0.0,4.7236066 -4.146503,5.096265 -4.866503 c 1.754129 -0.698923,4.101909 -1.131852,4.6987553 -1.235148 c 1.711974 1.6308,3.691916 2.065935,4.514109 2.065935 c 0.61862 0.0,2.067387 0.0,2.067387 -1.489529 C 18.0 10.833156,16.227118 10.342394,14.708846 10.342394 m -0.119033 0.977865 c 1.334032 0.0,1.6866 0.441174,1.6866 0.674419 c 0.0 0.146381,-0.05557 0.623962,-0.770632 0.623962 c -0.641207 0.0,-1.748265 -0.370568,-2.837497 -1.174646 c 0.454238 -0.05969,1.126394 -0.123735,1.921529 -0.123735 M 8.050761 1.2062323 c 0.1216452 0.0,0.2017161 0.039077,0.2677355 0.1306452 C 8.702187 1.8692712,8.392819 3.6088843,8.016271 4.9702067 C 7.6527867 3.8029358,7.3799996 2.0118778,7.7638063 1.3817617 C 7.838826 1.2587807,7.9245877 1.2062323,8.050761 1.2062323 M 7.402878 11.626084 C 7.885859 10.650368,8.427195 9.228368,8.722046 8.424116 C 9.312098 9.411736,10.105723 10.32869,10.564724 10.825839 C 9.135581 11.127136,8.054303 11.428143,7.402878 11.626084 m -6.443478 4.884794 c -0.0318194 -0.03774,-0.0365226 -0.11729,-0.0125419 -0.212806 c 0.0502839 -0.200149,0.4345548 -1.192297,3.2139292 -2.435575 c -0.3979743 0.626865,-1.0201355 1.522568,-1.703613 2.191704 c -0.4811226 0.450348,-0.8557549 0.678716,-1.1135033 0.678716 c -0.092206 1.0E-6,-0.2192516 -0.02514,-0.384271 -0.222039 z\\u0027/%3E%3C/g%3E%3Cpath fill\\u003d\\u0027%23AA0000\\u0027 style\\u003d\\u0027stroke-width:0.414187\\u0027 id\\u003d\\u0027path49\\u0027 d\\u003d\\u0027M 17.801788 5.406512 C 17.740654 6.1878333,17.117386 6.562838,16.990644 6.6314273 C 16.315935 6.999722,15.180484 6.87149,14.63177 6.0991144 C 14.329082 5.674159,14.176993 4.997958,14.471479 4.529762 l 0.0037 -0.00746 C 14.581045 4.350083,14.826327 4.059324,15.278867 4.049632 h 0.0164 c 0.161781 0.0,0.348165 0.049205,0.512928 0.092446 c 0.115558 0.030567,0.21546 0.056661,0.290014 0.061879 c 0.04697 0.00298,0.114067 -0.00895,0.199058 -0.023857 c 0.254227 -0.044732,0.678437 -0.1192857,1.049714 0.1431429 c 0.505473 0.3586031,0.457759 1.0541877,0.454777 1.0832635 z\\u0027/%3E%3Cpath fill\\u003d\\u0027%23AA0000\\u0027 style\\u003d\\u0027stroke-width:0.414187\\u0027 id\\u003d\\u0027path51\\u0027 d\\u003d\\u0027m 14.711542 4.1592307 c -0.145379 0.1028839,-0.241554 0.2303706,-0.299705 0.3243081 l -0.0037 0.00596 c -0.150598 0.2400625,-0.192348 0.5188929,-0.164018 0.791759 c -0.003 0.00149,-0.006 0.00298,-0.0082 0.00522 c -0.552442 0.4510491,-1.176455 0.3347455,-1.418755 0.2639196 l -0.01417 -0.00373 C 12.03134 5.354325,11.269403 4.520816,11.409564 3.6328826 C 11.487104 3.143811,11.856885 2.581677,12.414546 2.4534447 c 0.09617 -0.022366,0.598665 -0.115558,1.002746 0.2229152 c 0.123759 0.1043751,0.21546 0.2564644,0.295977 0.3906608 c 0.05741 0.09692,0.107358 0.1796741,0.161782 0.2340983 c 0.03504 0.03504,0.09617 0.069335,0.17371 0.1133214 c 0.227388 0.1274867,0.570335 0.3205804,0.659799 0.7365894 c 7.45E-4 0.00298,0.0015 0.00596,0.003 0.0082 z\\u0027/%3E%3Cpath fill\\u003d\\u0027%23502D16\\u0027 style\\u003d\\u0027stroke-width:0.414187\\u0027 id\\u003d\\u0027path53\\u0027 d\\u003d\\u0027m 16.22647 4.1174803 c -0.04399 0.00746,-0.08275 0.012674,-0.110339 0.012674 c -0.0045 0.0,-0.0089 -7.456E-4,-0.01267 -7.456E-4 c -0.02088 -0.00149,-0.04548 -0.00447,-0.07157 -0.00969 c 0.228879 -0.5845001,0.323563 -1.519402,-0.471924 -2.7033129 c -0.0015 -0.00298,-0.0037 -0.00522,-0.0067 -0.00746 c 0.04622 -0.1834018,0.06188 -0.311634,0.06561 -0.3429465 c 0.0022 -0.020875,-0.01267 -0.038768,-0.0328 -0.041004 c -0.02088 -0.00298,-0.03877 0.012674,-0.04101 0.032804 c -0.0089 0.079772,-0.09841 0.7992145,-0.600156 1.4381388 c -0.313871 0.3996073,-0.808161 0.6970761,-0.978889 0.7820671 c -0.01566 -0.010438,-0.02908 -0.020875,-0.03802 -0.029821 c -0.0246 -0.024603,-0.04846 -0.056661,-0.07306 -0.094683 c 0.01417 -0.00746,0.02908 -0.015656,0.04473 -0.023857 c 0.178183 -0.095429,0.476398 -0.2542278,0.840219 -0.6351966 c 0.430175 -0.4503036,0.512184 -1.1399243,0.391407 -1.3277993 c -0.05815 -0.090955,-0.151344 -0.099902,-0.225898 -0.1066116 c -0.09767 -0.0082,-0.156562 -0.014165,-0.167 -0.14687057 c -0.0097 -0.11555806,0.04473 -0.21769646,0.152835 -0.28628576 c 0.14538 -0.093192,0.405572 -0.12301341,0.657563 0.0201295 c 0.157308 0.0887188,0.155817 0.24006253,0.155071 0.38618752 c -7.45E-4 0.097665,-0.0015 0.1901116,0.04697 0.2564643 l 0.02088 0.02833 c 0.119286 0.1617813,0.398116 0.5390224,0.565116 1.1973305 c 0.178183 0.7045312,0.0097 1.3464376,-0.110339 1.6021564 z\\u0027/%3E%3Cpath style\\u003d\\u0027stroke-width:0.414187\\u0027 id\\u003d\\u0027path55\\u0027 d\\u003d\\u0027m 14.033104 1.060038 c -0.006 0.00298,-0.01193 0.00447,-0.01789 0.00447 c -0.01342 0.0,-0.02609 -0.00745,-0.0328 -0.019384 c -7.46E-4 -7.455E-4,-0.05293 -0.0939375,-0.146871 -0.1796741 c -0.01566 -0.0134196,-0.0164 -0.0372768,-0.003 -0.0521875 c 0.01417 -0.0156563,0.03728 -0.0164018,0.05293 -0.002982 c 0.10363 0.0939375,0.159545 0.1945848,0.161782 0.1990581 c 0.01044 0.017893,0.0037 0.040259,-0.01417 0.050696 z\\u0027/%3E%3Cpath style\\u003d\\u0027stroke-width:0.414187\\u0027 id\\u003d\\u0027path57\\u0027 d\\u003d\\u0027m 14.108404 1.7705336 c -0.07754 0.091701,-0.117795 0.1081027,-0.178929 0.1327054 l -0.01938 0.0082 c -0.0045 0.00224,-0.0097 0.00298,-0.01417 0.00298 c -0.01491 0.0,-0.02833 -0.00895,-0.0343 -0.023112 c -0.0082 -0.018638,7.46E-4 -0.041005,0.02013 -0.04846 l 0.01938 -0.0082 c 0.05592 -0.023112,0.08425 -0.03504,0.150598 -0.1118304 c 0.01342 -0.016402,0.03728 -0.017893,0.05293 -0.00447 c 0.01566 0.01342,0.01715 0.036531,0.0037 0.052188 z\\u0027/%3E%3Cpath style\\u003d\\u0027stroke-width:0.414187\\u0027 fill\\u003d\\u0027%23008000\\u0027 id\\u003d\\u0027path59\\u0027 d\\u003d\\u0027m 15.068654 1.2069086 c -0.01938 -0.030567,-0.04473 -0.046969,-0.07306 -0.055915 c -0.09916 0.1334509,-0.448067 0.3354911,-0.798469 0.3354911 c -0.09319 0.0,-0.187129 -0.014165,-0.275848 -0.047714 C 13.682706 1.3478158,13.519433 1.2210743,13.36138 1.0988064 C 13.23613 1.0011412,13.117589 0.90944034,12.972955 0.8468153 c -0.01938 -0.008946,-0.02758 -0.030567,-0.01938 -0.0492054 c 0.0082 -0.0193839,0.02982 -0.0275848,0.04921 -0.0193839 c 0.152835 0.0670982,0.275103 0.16103578,0.40408 0.26168305 c 0.153581 0.1192858,0.313125 0.2422992,0.540514 0.3287813 c 0.231861 0.087973,0.492054 0.030567,0.68962 -0.058152 c -0.08499 -0.2624286,-0.634451 -1.0608975,-1.540277 -0.85736626 c -0.14016 0.0313125,-0.262428 0.0603884,-0.369785 0.0857366 c -0.454777 0.10661162,-0.649362 0.15208936,-0.851402 0.0984107 c 0.01864 0.0939376,0.06784 0.17370984,0.146125 0.23782597 c 0.156562 0.12599558,0.383951 0.15730807,0.492053 0.15879917 c -0.05517 -0.062625,-0.114067 -0.10288399,-0.186384 -0.12823221 c -0.01938 -0.00671,-0.02982 -0.0275848,-0.02311 -0.0469688 c 0.0067 -0.0201295,0.02833 -0.0298214,0.04771 -0.0231116 c 0.234844 0.0827545,0.342947 0.2929955,0.517402 0.72167873 c 0.182656 0.4488126,0.821581 0.8670582,1.339728 0.7119868 c 0.527839 -0.1580536,0.518893 -0.7142233,0.518147 -0.7201876 c 0.0 -0.014911,0.0089 -0.029076,0.02237 -0.03504 c 0.03504 -0.015656,0.214715 -0.097665,0.341456 -0.2169509 c 0.0022 -0.00224,0.0045 -0.00373,0.0067 -0.00373 v -7.436E-4 c -0.006 -0.036531,-0.0164 -0.065607,-0.02908 -0.085737 M 13.832556 0.8132656 c 0.01417 -0.0156562,0.03728 -0.0164018,0.05293 -0.002982 c 0.103629 0.0939375,0.159544 0.1945848,0.161781 0.1990581 c 0.01044 0.017893,0.0037 0.040259,-0.01417 0.050696 c -0.006 0.00298,-0.01193 0.00447,-0.01789 0.00447 c -0.01342 0.0,-0.02609 -0.00745,-0.0328 -0.019384 c -7.45E-4 -7.456E-4,-0.05293 -0.0939375,-0.14687 -0.17967416 c -0.01566 -0.0134196,-0.0164 -0.0372768,-0.003 -0.0521875 M 13.204814 1.3209755 c -0.0015 0.0,-0.02311 0.00373,-0.06561 0.00373 c -0.03355 0.0,-0.07903 -0.00224,-0.137178 -0.00969 c -0.02088 -0.00224,-0.03504 -0.020875,-0.03206 -0.04175 c 0.0022 -0.02013,0.02088 -0.034295,0.04175 -0.032058 c 0.120777 0.015656,0.179675 0.00671,0.18042 0.00671 c 0.02013 -0.00373,0.03951 0.00969,0.0425 0.030567 c 0.0037 0.020129,-0.0097 0.038768,-0.02982 0.042496 m 0.90359 0.4495581 c -0.07754 0.091701,-0.117795 0.1081027,-0.178929 0.1327054 l -0.01938 0.0082 c -0.0045 0.00224,-0.0097 0.00298,-0.01417 0.00298 c -0.01491 0.0,-0.02833 -0.00895,-0.0343 -0.023112 c -0.0082 -0.018638,7.46E-4 -0.041005,0.02013 -0.04846 l 0.01938 -0.0082 c 0.05592 -0.023112,0.08425 -0.03504,0.150598 -0.1118304 c 0.01342 -0.016402,0.03728 -0.017893,0.05293 -0.00447 c 0.01566 0.01342,0.01715 0.036531,0.0037 0.052188 z\\u0027/%3E%3Cpath style\\u003d\\u0027stroke-width:0.414187\\u0027 id\\u003d\\u0027path61\\u0027 d\\u003d\\u0027m 13.234636 1.2784799 c 0.0037 0.020129,-0.0097 0.038768,-0.02982 0.042496 c -0.0015 0.0,-0.02311 0.00373,-0.06561 0.00373 c -0.03355 0.0,-0.07903 -0.00224,-0.137178 -0.00969 c -0.02088 -0.00224,-0.03504 -0.020875,-0.03206 -0.04175 c 0.0022 -0.02013,0.02088 -0.034295,0.04175 -0.032058 c 0.120777 0.015656,0.179675 0.00671,0.18042 0.00671 c 0.02013 -0.00373,0.03951 0.00969,0.0425 0.030567 z\\u0027/%3E%3C/svg%3E\"";
  private static final Logger logger = LoggerFactory.getLogger(PdfToolbox.class.getName());

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
      // savePdfInFile(sourceFileVariable);

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
      int xText = (int) (width / 2.0
          - Math.cos(Math.toRadians(writerOption.degree)) * stringWidth / 2.0);
      int yText = (int) ( height / 2.0
          - Math.sin(Math.toRadians(writerOption.degree)) * stringWidth / 2.0);

      Matrix matrix = Matrix.getRotateInstance(Math.toRadians(writerOption.degree), xText, yText);

      contentStream.setTextMatrix(matrix);

    } else {
      contentStream.newLineAtOffset(x, y);
    }
    contentStream.showText(text);
    contentStream.endText();
    contentStream.close();
  }

  /**
   * Propose a default logo for all connectors
   *
   * @return logo
   */
  public static String getLogo() {
    return WORKER_LOGO;
  }

  /**
   * Propose the same collectionName
   *
   * @return collection Name
   */
  public static String getCollectionName() {
    return "PDF";
  }

  private static void savePdfInFile(FileVariable sourceFileVariable) {
    File tempFile = null;
    try {
      tempFile = File.createTempFile("temp", ".pdf");
      // Write the byte array to the temporary file
      FileOutputStream fos = new FileOutputStream(tempFile);
      fos.write(sourceFileVariable.getValue());
      fos.close();
      logger.error("File saved [{}]", tempFile.getAbsolutePath());

    } catch (Exception e) {
      logger.error("Error during saving file[{}] : {}", tempFile == null ? "null" : tempFile.getAbsolutePath(),
          e.getMessage());
    }
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
