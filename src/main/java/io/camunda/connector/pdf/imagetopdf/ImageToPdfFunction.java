package io.camunda.connector.pdf.imagetopdf;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.CherryInput;
import io.camunda.connector.pdf.PdfInput;
import io.camunda.connector.pdf.PdfOutput;
import io.camunda.connector.pdf.sharedfunctions.LoadDocument;
import io.camunda.connector.pdf.sharedfunctions.SavePdfDocument;
import io.camunda.connector.pdf.toolbox.PdfParameter;
import io.camunda.connector.pdf.toolbox.PdfSubFunction;
import io.camunda.connector.pdf.toolbox.PdfToolbox;
import io.camunda.filestorage.FileRepoFactory;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.FileVariableReference;
import io.camunda.filestorage.StorageDefinition;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageToPdfFunction implements PdfSubFunction {
  public static final String ERROR_ACCESS_SOURCE_IMAGE = "ACCESS_SOURCE_IMAGE";
  public static final String ERROR_DRAW_IMAGE = "DRAW_IMAGE";
  public static final String ERROR_NO_DESTINATION_STORAGE_DEFINITION_DEFINE = "ERROR_NO_DESTINATION_STORAGE_DEFINITION_DEFINE";
  public static final String ERROR_DEFINITION_ERROR = "DEFINITION_ERROR";

  Logger logger = LoggerFactory.getLogger(ImageToPdfFunction.class.getName());

  /**
   * Execute the sub-function
   *
   * @param pdfInput the input of connector
   * @param context  outbound connector execution
   * @return an Output object
   * @throws Exception in case of any issue
   */

  @Override
  public PdfOutput executeSubFunction(PdfInput pdfInput, OutboundConnectorContext context) throws ConnectorException {
    logger.debug("{} Start ImageToPDF", PdfToolbox.getLogSignature(this));
    // Create a new PDF document
    PDDocument destinationDocument = new PDDocument();

    List<FileVariableReference> listImages = new ArrayList<>();
    try {
      FileVariable defaultDocSource = null;
      // ------------------ list of images
      List<String> listDocSourceReference = pdfInput.getListSourceFile();
      if (listDocSourceReference != null) {
        logger.info("{} Start ImageToPDF {} images to build the PDF", PdfToolbox.getLogSignature(this),
            listDocSourceReference.size());
        for (String reference : listDocSourceReference) {
          try {
            FileVariableReference docSourceReference = FileVariableReference.fromJson(reference);
            if (docSourceReference != null) {
              listImages.add(docSourceReference);
            }
          } catch (Exception e) {
            logger.error("{} exception during load file [{}] : {}", PdfToolbox.getLogSignature(this), reference, e);
            throw new ConnectorException(ERROR_ACCESS_SOURCE_IMAGE, "Error " + e);
          }
        }
      }

      // create the PDF now
      FileRepoFactory fileRepoFactory = FileRepoFactory.getInstance();
      int pageNumber = 0;
      // Loop through each JPEG image file
      for (FileVariableReference fileVariableReference : listImages) {
        pageNumber++;

        long timeStep0Begin = System.currentTimeMillis();
        // Load JPEG image
        FileVariable fileVariable = LoadDocument.loadDocSourceFromReference(fileVariableReference, fileRepoFactory,
            this);

        long timeStep1LoadFileVariable = System.currentTimeMillis();
        if (defaultDocSource == null)
          defaultDocSource = fileVariable;
        ByteArrayInputStream bis = new ByteArrayInputStream(fileVariable.getValue());
        BufferedImage bufferedImage = ImageIO.read(bis);
        long timeStep2BufferedImage = System.currentTimeMillis();

        PDImageXObject image = JPEGFactory.createFromImage(destinationDocument, bufferedImage);
        long timeStep3CreateImage = System.currentTimeMillis();

        // Create a new page in the PDF document
        PDPage page = new PDPage(new PDRectangle(image.getWidth(), image.getHeight()));
        destinationDocument.addPage(page);
        long timeStep4AddPage = System.currentTimeMillis();

        // Draw the JPEG image onto the page
        PDPageContentStream contentStream = null;
        try {
          contentStream = new PDPageContentStream(destinationDocument, page);
          contentStream.drawImage(image, 0, 0);

        } catch (Exception e) {
          logger.error("{} Exception during draw page {}: {}", PdfToolbox.getLogSignature(this), pageNumber, e);
          throw new ConnectorException(ERROR_DRAW_IMAGE, "Error " + e);
        } finally {
          if (contentStream != null)
            try {
              contentStream.close();
            } catch (Exception e) {
              logger.error("{} Exception during close contentStream page {}: {}", PdfToolbox.getLogSignature(this),
                  pageNumber, e);
            }
        }
        long timeStep5DrawImage = System.currentTimeMillis();
        logger.info(
            "{} Load image {} LoadFile {} ms, BufferedImage {} ms, CreateImage {} ms, AddPage {} ms, CopyImageInPage {} ms, Image WidthxHeight {}x{} ",
            PdfToolbox.getLogSignature(this), fileVariable.getName(), timeStep1LoadFileVariable - timeStep0Begin,
            // Load Image
            timeStep2BufferedImage - timeStep1LoadFileVariable, // Buffered Image
            timeStep3CreateImage - timeStep2BufferedImage, // Create image
            timeStep4AddPage - timeStep3CreateImage, // Add a new page
            timeStep5DrawImage - timeStep4AddPage, // Draw the image in the page
            image.getWidth(), image.getHeight());
      }

      String destinationFileName = pdfInput.getDestinationFileName();
      String destinationStorageDefinitionSt = pdfInput.getDestinationStorageDefinition();
      StorageDefinition destinationStorageDefinition=null;

      if (destinationStorageDefinitionSt != null && !destinationStorageDefinitionSt.trim().isEmpty()) {
        try {
          destinationStorageDefinition = StorageDefinition.getFromString(destinationStorageDefinitionSt);
        } catch (Exception e) {
          throw new ConnectorException(ERROR_DEFINITION_ERROR,
              PdfToolbox.getLogSignature(this) + "Can't decode StorageDefinition [" + destinationStorageDefinitionSt
                  + "]");
        }
      } else {
        if (defaultDocSource != null)
          destinationStorageDefinition = defaultDocSource.getStorageDefinition();
      }

      if (destinationStorageDefinition == null) {
        // no storage : this is a problem
        logger.error("{} no destination storage defintion define ", PdfToolbox.getLogSignature(this));
        throw new ConnectorException(ERROR_NO_DESTINATION_STORAGE_DEFINITION_DEFINE);
      }

      // produce the result, and save it in the pdfOutput
      // Exception PdfToolbox.ERROR_CREATE_FILEVARIABLE, PdfToolbox.ERROR_SAVE_ERROR
      PdfOutput pdfOutput = SavePdfDocument.savePdfFile(new PdfOutput(), destinationDocument, destinationFileName,
          destinationStorageDefinition, fileRepoFactory, this);

      logger.info("{}  {} image pages generated to document[{}]", PdfToolbox.getLogSignature(this), pageNumber,
          destinationFileName);
      return pdfOutput;
    } catch (Exception e) {
      logger.error("{} during ImageToPdf {}", PdfToolbox.getLogSignature(this), e);
      throw new ConnectorException(PdfToolbox.ERROR_DURING_OPERATION, "Error " + e);

    } finally {
      if (destinationDocument != null)
        try {
          destinationDocument.close();
        } catch (Exception e) {
          logger.error("{} during close document {}", PdfToolbox.getLogSignature(this), e.getMessage());
        }
    }
  }

  /**
   * Name of the sub-function
   *
   * @return the name
   */
  public String getName() {
    return "Images to PDF";
  }

  /**
   * Description of the sub-function
   *
   * @return the description
   */
  public String getDescription() {
    return "Generate a PDF from one or multiple images";
  }

  public List<PdfParameter> getSubFunctionParameters(PdfSubFunction.TypeParameter typeParameter) {
    switch (typeParameter) {
    case INPUT:
      return Arrays.asList(new PdfParameter(PdfInput.INPUT_LIST_SOURCE_FILE, // name
              "List source file", // label
              List.class, // class
              CherryInput.PARAMETER_MAP_LEVEL_OPTIONAL, // level
              "List of FileVariable for the file to convert", 1),

          PdfInput.pdfParameterDestinationFileName, PdfInput.pdfParameterDestinationStorageDefinition);

    case OUTPUT:
      return List.of(PdfOutput.PDF_PARAMETER_DESTINATION_FILE);

    }
    return Collections.emptyList();
  }

  private static final Map<String, String> listBpmnErrors = new HashMap<>();

  static {
    listBpmnErrors.putAll(LoadDocument.getBpmnErrors());
    listBpmnErrors.put(ERROR_ACCESS_SOURCE_IMAGE, "Impossible to access a source image");
    listBpmnErrors.put(ERROR_DRAW_IMAGE, "Error during draw the image");
    listBpmnErrors.put(ERROR_NO_DESTINATION_STORAGE_DEFINITION_DEFINE, "A storage definition must be set");
    listBpmnErrors.put(ERROR_DEFINITION_ERROR, "Definition error");
    listBpmnErrors.put(PdfToolbox.ERROR_DURING_OPERATION, PdfToolbox.ERROR_DURING_OPERATION_LABEL);
  }

  @Override
  public Map<String, String> getSubFunctionListBpmnErrors() {
    return listBpmnErrors;
  }

  @Override
  public String getSubFunctionName() {
    return "Image(s) to PDF";
  }

  @Override
  public String getSubFunctionDescription() {
    return "From one or a list of image, return a PDF";
  }

  @Override
  public String getSubFunctionType() {
    return "image-to-pdf";
  }
}
