package io.camunda.connector.pdf.pdftoimage;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.CherryInput;
import io.camunda.connector.pdf.PdfInput;
import io.camunda.connector.pdf.PdfOutput;
import io.camunda.connector.pdf.sharedfunctions.LoadDocument;
import io.camunda.connector.pdf.sharedfunctions.LoadPdfDocument;
import io.camunda.connector.pdf.sharedfunctions.RetrieveStorageDefinition;
import io.camunda.connector.pdf.sharedfunctions.SavePdfDocument;
import io.camunda.connector.pdf.toolbox.ExtractPageExpression;
import io.camunda.connector.pdf.toolbox.PdfParameter;
import io.camunda.connector.pdf.toolbox.PdfSubFunction;
import io.camunda.connector.pdf.toolbox.PdfToolbox;
import io.camunda.filestorage.FileRepoFactory;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.FileVariableReference;
import io.camunda.filestorage.StorageDefinition;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PdfToImageFunction implements PdfSubFunction {
  public static final String ERROR_NO_DESTINATION_STORAGE_DEFINITION_DEFINE = "ERROR_NO_DESTINATION_STORAGE_DEFINITION_DEFINE";
  public static final String ERROR_DRAW_IMAGE = "ERROR_DRAW_IMAGE";
  private final Logger logger = LoggerFactory.getLogger(PdfToImageFunction.class.getName());

  /**
   *
   * @param pdfInput input
   * @param context context of the task
   * @return the output
   * @throws ConnectorException in case of any error
   */
  @Override
  public PdfOutput executeSubFunction(PdfInput pdfInput, OutboundConnectorContext context) throws ConnectorException {

    logger.debug("{} Start PdfToImages", PdfToolbox.getLogSignature(this));

    FileRepoFactory fileRepoFactory = FileRepoFactory.getInstance();
    FileVariableReference docSourceReference;
    PDDocument docSourcePDF = null;

    try {
      FileVariable docSource = LoadDocument.loadDocSource(pdfInput.getSourceFile(), fileRepoFactory, this);

      logger.info("{} Start PdfToImages on doc {} ", PdfToolbox.getLogSignature(this), docSource.getName());

      StorageDefinition destinationStorageDefinition = RetrieveStorageDefinition.getStorageDefinition(pdfInput,
          docSource, true, this);

      // get the file
      docSourcePDF = LoadPdfDocument.loadPdfDocument(docSource, this);

      PDFRenderer pdfRenderer = new PDFRenderer(docSourcePDF);

      ExtractPageExpression extractPageExpression = new ExtractPageExpression(pdfInput.getExtractExpression(),
          docSourcePDF, this);

      // add all pages from sources
      PdfOutput pdfOutput = new PdfOutput();

      for (int pageIndex = 0; pageIndex < docSourcePDF.getNumberOfPages(); pageIndex++) {
        if (!extractPageExpression.isPageInRange(pageIndex + 1)) {
          logger.info("{} Page {}/{} Not in expression", PdfToolbox.getLogSignature(this), (pageIndex + 1),
              docSourcePDF.getNumberOfPages());

          continue;
        }
        long timeStep0Begin = System.currentTimeMillis();

        // Render PDF page to BufferedImage
        BufferedImage image = pdfRenderer.renderImageWithDPI(pageIndex, pdfInput.getDpi()); // 300 DPI
        long timeStep1Rendering = System.currentTimeMillis();

        // Convert BufferedImage to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageData = baos.toByteArray();
        long timeStep2WriteToByte = System.currentTimeMillis();

        FileVariable fileVariableOut = new FileVariable();

        fileVariableOut.setValue(baos.toByteArray());
        fileVariableOut.setName(PdfToolbox.getDocumentName(docSource, true) + "_" + (pageIndex + 1) + ".png");
        fileVariableOut.setStorageDefinition(destinationStorageDefinition);
        SavePdfDocument.saveFile(pdfOutput, fileVariableOut, fileRepoFactory, this);

        long timeStep3WriteFile = System.currentTimeMillis();
        logger.info("{} Page {}/{} Render {} ms, WriteImage {} ms, WriteFile {} ms imageSize {} Ko",
            PdfToolbox.getLogSignature(this), (pageIndex + 1), docSourcePDF.getNumberOfPages(),
            timeStep1Rendering - timeStep0Begin, // Rendering
            timeStep2WriteToByte - timeStep1Rendering, // Write Image
            timeStep3WriteFile - timeStep2WriteToByte, imageData.length / 1024); // Save Image
      } // end loop each page
      return pdfOutput;
    } catch (ConnectorException ce) {
      // already logged
      throw ce;
    } catch (Exception e) {
      logger.error("{} During operation : ",  PdfToolbox.getLogSignature(this), e);
      throw new ConnectorException(PdfToolbox.ERROR_DURING_OPERATION, "Error " + e);
    } finally {
      if (docSourcePDF!=null)
        try {
          docSourcePDF.close();
        } catch (Exception e)
        {
          logger.error("{} During close document : {}", PdfToolbox.getLogSignature(this), e.getMessage());
        }
    }

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

          new PdfParameter(PdfInput.INPUT_EXTRACT_EXPRESSION, // name
              "Extract Expression", // label
              String.class, // class
              CherryInput.PARAMETER_MAP_LEVEL_REQUIRED, // level
              "Extract pilot: example, 2-4 mean extract pages 2 to 4 (document page start at 1). Use \u0027n\u0027 to specify the end of the document (2-n) extract from page 2 to the end. Simple number is accepted to extract a page. Example: 4-5, 10, 15-n or 2-n, 1 (first page to the end)",
              1),

          new PdfParameter(PdfInput.INPUT_PDFTOIMAGE_DPI, // name
              "Dpi", // label
              Long.class, // class
              CherryInput.PARAMETER_MAP_LEVEL_OPTIONAL, // level
              "Each page will be convert to an image. Specify the DPI for the generation (default is 300 dpi)", 1),

          PdfInput.pdfParameterDestinationFileName, PdfInput.pdfParameterDestinationStorageDefinition);

    case OUTPUT:
      return List.of(PdfOutput.PDF_PARAMETER_LIST_DESTINATION_FILE);
    }
    return Collections.emptyList();
  }

  private static final Map<String, String> listBpmnErrors = new HashMap<>();

  static {
    listBpmnErrors.putAll(RetrieveStorageDefinition.getBpmnErrors());
    listBpmnErrors.putAll(LoadDocument.getBpmnErrors());
    listBpmnErrors.putAll(ExtractPageExpression.getBpmnErrorExtractExpression());
    listBpmnErrors.putAll(SavePdfDocument.getBpmnErrors());
    listBpmnErrors.put(PdfToolbox.ERROR_DURING_OPERATION, PdfToolbox.ERROR_DURING_OPERATION_LABEL);
  }

    @Override
  public Map<String, String> getSubFunctionListBpmnErrors() {
    return listBpmnErrors;
  }

  @Override
  public String getSubFunctionName() {
    return "Pdf To Images";
  }

  @Override
  public String getSubFunctionDescription() {
    return "Return a list of images, one per page";
  }

  @Override
  public String getSubFunctionType() {
    return "pdf-to-images";
  }
}
