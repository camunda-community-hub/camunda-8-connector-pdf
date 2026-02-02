package io.camunda.connector.pdf.extractpages;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.pdf.PdfInput;
import io.camunda.connector.pdf.PdfOutput;
import io.camunda.connector.pdf.sharedfunctions.LoadDocument;
import io.camunda.connector.pdf.sharedfunctions.LoadPdfDocument;
import io.camunda.connector.pdf.sharedfunctions.RetrieveStorageDefinition;
import io.camunda.connector.pdf.sharedfunctions.SavePdfDocument;
import io.camunda.connector.pdf.toolbox.ExtractPageExpression;
import io.camunda.connector.pdf.toolbox.PdfSubFunction;
import io.camunda.connector.pdf.toolbox.PdfToolbox;
import io.camunda.filestorage.FileRepoFactory;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.storage.StorageDefinition;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Expression accepted:
 * "8" only one page, page number 8 (the first page is 1)
 * "5-7" pages 5 to 7*
 * "2-n" pages 2 up to the end
 * "2-6,8,10-12,14-n" multiple expressions are accepted, separated by a comma
 */

public class PdfExtractPagesFunction implements PdfSubFunction {
  public static final String ERROR_EXTRACTION_ERROR = "EXTRACTION_ERROR";
  private static final Map<String, String> listBpmnErrors = new HashMap<>();

  static {
    listBpmnErrors.putAll(LoadDocument.getBpmnErrors());
    listBpmnErrors.putAll(RetrieveStorageDefinition.getBpmnErrors());
    listBpmnErrors.putAll(LoadPdfDocument.getBpmnErrors());
    listBpmnErrors.putAll(SavePdfDocument.getBpmnErrors());
    listBpmnErrors.putAll(ExtractPageExpression.getBpmnErrorExtractExpression());
    listBpmnErrors.put(ERROR_EXTRACTION_ERROR, "Extraction error");
  }

  Logger logger = LoggerFactory.getLogger(PdfExtractPagesFunction.class.getName());

  public static String getDescription() {
    return "Extract pages from a PDF. A String pilot the extraction, to get the list of page to extract";
  }

  /**
   * @param pdfInput                 input of connector
   * @param outboundConnectorContext context of onnector
   * @return the output
   * @throws Exception for any error
   */
  public PdfOutput executeSubFunction(PdfInput pdfInput, OutboundConnectorContext outboundConnectorContext)
      throws ConnectorException {
    logger.debug("{} Start ExtractPages", PdfToolbox.getLogSignature(this));

    FileRepoFactory fileRepoFactory = FileRepoFactory.getInstance();
    PDDocument sourceDocument = null;
    PDDocument destinationDocument = null;
    String extractExpression = pdfInput.getExtractExpression();

    int nbPagesExtracted = 0;
    logger.info("{} Start ExtractPages sourceDocument=[{}] extract[{}] ", PdfToolbox.getLogSignature(this),
        pdfInput.getSourceFile(), extractExpression);

    try {
      FileVariable docSource = LoadDocument.loadDocSource(pdfInput.getSourceFile(), fileRepoFactory, this,outboundConnectorContext);

      String destinationFileName = pdfInput.getDestinationFileName();

      StorageDefinition destinationStorageDefinition = RetrieveStorageDefinition.getStorageDefinition(pdfInput,
          docSource, true, this);

      sourceDocument = LoadPdfDocument.loadPdfDocument(docSource, this);

      destinationDocument = new PDDocument();
      ExtractPageExpression extractPageExpression = new ExtractPageExpression(extractExpression, sourceDocument, this);

      // Loop on each page
      for (int pageIndex = 1; pageIndex <= sourceDocument.getNumberOfPages(); pageIndex++)
        if (extractPageExpression.isPageInRange(pageIndex)) {
          // getPage starts at 0, pageIndex start at 1
          destinationDocument.addPage(sourceDocument.getPage(pageIndex - 1));
          nbPagesExtracted++;
        }

      // produce the result, and save it in the pdfOutput
      // Exception PdfToolbox.ERROR_CREATE_FILEVARIABLE, PdfToolbox.ERROR_SAVE_ERROR
      PdfOutput pdfOutput = SavePdfDocument.savePdfFile(new PdfOutput(), destinationDocument, destinationFileName,
          destinationStorageDefinition, fileRepoFactory, this,outboundConnectorContext);

      logger.info("{} Extract {} pages from document[{}] to [{}]", PdfToolbox.getLogSignature(this), nbPagesExtracted,
          docSource.getName(), destinationFileName);
      return pdfOutput;
    } catch (ConnectorException ce) {
      // already logged
      throw ce;
    } catch (Exception e) {
      logger.error("{} Exception during extraction {} ", PdfToolbox.getLogSignature(this), e);
      throw new ConnectorException(ERROR_EXTRACTION_ERROR, "Error " + e);
    } finally {
      if (sourceDocument != null)
        try {
          sourceDocument.close();
        } catch (Exception e) {
          // don't care
        }
      if (destinationDocument != null) {
        try {
          destinationDocument.close();
        } catch (Exception e) {
          // don't care
        }

      }
    } // end finally

  }

  public String getSubFunctionName() {
    return "Extract pages";
  }

  @Override
  public String getSubFunctionDescription() {
    return "Extract pages in a PDF, and produce a PDF";
  }

  @Override
  public String getSubFunctionType() {
    return "extract-pages";
  }

  public Map<String, String> getSubFunctionListBpmnErrors() {
    return listBpmnErrors;
  }



  @Override
  public List<RunnerParameter> getInputsParameter() {
    return List.of(
    new RunnerParameter(PdfInput.SOURCE_FILE, // name
                    "Source file", // label
                    Object.class, // class
                    RunnerParameter.Level.REQUIRED, // level
                    "FileVariable for the file to convert"),

            new RunnerParameter(PdfInput.EXTRACT_EXPRESSION, // name
                    "Extract Expression", // label
                    String.class, // class
                    RunnerParameter.Level.REQUIRED, // level
                    "Extract pilot: example, 2-4 mean extract pages 2 to 4 (document page start at 1). Use \u0027n\u0027 to specify the end of the document (2-n) extract from page 2 to the end. Simple number is accepted to extract a page. Example: 4-5, 10, 15-n or 2-n, 1 (first page to the end)"),

            PdfInput.pdfParameterDestinationFileName,
            PdfInput.pdfParameterDestinationJsonStorageDefinition,
            PdfInput.pdfParameterDestinationStorageDefinition,
            PdfInput.pdfParameterDestinationStorageDefinitionComplement,
            PdfInput.pdfParameterDestinationStorageDefinitionCmis);
  }

  @Override
  public List<RunnerParameter> getOutputsParameter() {
    return List.of(PdfOutput.PDF_PARAMETER_DESTINATION_FILE);
  }

  @Override
  public Map<String, String> getBpmnErrors() {
    return listBpmnErrors;  }
}
