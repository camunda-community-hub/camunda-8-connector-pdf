package io.camunda.connector.pdf.extractpages;

import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.connector.pdf.toolbox.PdfToolbox;
import io.camunda.file.storage.FileRepoFactory;
import io.camunda.file.storage.FileVariable;
import io.camunda.file.storage.FileVariableReference;
import io.camunda.file.storage.StorageDefinition;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Expression accepted:
 * "8" only one page, page number 8 (the first page is 1)
 * "5-7" pages 5 to 7*
 * "2-n" pages 2 up to the end
 * "2-6,8,10-12,14-n" multiple expressions are accepted, separated by a comma
 */
@OutboundConnector(name = PdfExtractPagesFunction.TYPE_PDF_EXTRACTPAGES, inputVariables = {
    PdfExtractPagesInput.INPUT_SOURCE_FILE, PdfExtractPagesInput.INPUT_EXTRACT_EXPRESSION,
    PdfExtractPagesInput.INPUT_DESTINATION_FILE_NAME,
    PdfExtractPagesInput.INPUT_DESTINATION_STORAGEDEFINITION }, type = PdfExtractPagesFunction.TYPE_PDF_EXTRACTPAGES)

public class PdfExtractPagesFunction implements OutboundConnectorFunction {
  public static final String ERROR_INVALID_EXPRESSION = "INVALID_EXPRESSION";
  public static final String ERROR_EXTRACTION_ERROR = "EXTRACTION_ERROR";
  public static final String ERROR_DEFINITION_ERROR = "DEFINITION_ERROR";
  public static final String TYPE_PDF_EXTRACTPAGES = "c-pdf-extractpages";
  Logger logger = LoggerFactory.getLogger(PdfExtractPagesFunction.class.getName());

  @Override
  public PdfExtractPagesOutput execute(OutboundConnectorContext context) throws Exception {
    PdfExtractPagesInput pdfExtractPagesInput = context.getVariablesAsType(PdfExtractPagesInput.class);
    FileRepoFactory fileRepoFactory = FileRepoFactory.getInstance();
    FileVariableReference docSourceReference = null;
    PDDocument sourceDocument = null;
    PDDocument destinationDocument = null;
    String extractExpression = pdfExtractPagesInput.getExtractExpression();

    int nbPagesExtracted = 0;
    logger.info(
        getLogSignature() + "sourceDocument=|" + pdfExtractPagesInput.getSourceFile() + "] extract[" + extractExpression
            + "]");

    try {
      docSourceReference = FileVariableReference.fromJson(pdfExtractPagesInput.getSourceFile());
      FileVariable docSource = fileRepoFactory.loadFileVariable(docSourceReference);

      String destinationFileName = pdfExtractPagesInput.getDestinationFileName();
      String destinationStorageDefinitionSt = pdfExtractPagesInput.getDestinationStorageDefinition();
      // get the file
      if (docSource == null || docSource.getValue() == null) {
        throw new ConnectorException(PdfToolbox.ERROR_LOAD_ERROR,
            getLogSignature() + "Can't read file [" + pdfExtractPagesInput.getSourceFile() + "]");
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

      sourceDocument = PdfToolbox.loadPdfDocument(docSource, getName());

      if (sourceDocument.isEncrypted()) {
        throw new ConnectorException(PdfToolbox.ERROR_ENCRYPTED_PDF_NOT_SUPPORTED,
            getLogSignature() + "Document is encrypted");
      }
      destinationDocument = new PDDocument();
      // replace any "n" information in the expression by the number of page
      String extractExpressionResolved = extractExpression.replaceAll("n",
          String.valueOf(sourceDocument.getNumberOfPages()));
      String[] expressionsList = extractExpressionResolved.split(",", 0);
      for (String oneExpression : expressionsList) {
        int firstPage;
        int lastPage;
        // format must be <number1>-<number2> where number1<=number2
        try {
          String[] expressionDetail = oneExpression.split("-", 2);
          firstPage = expressionDetail.length >= 1 ? Integer.parseInt(expressionDetail[0]) : -1;
          lastPage = expressionDetail.length >= 2 ? Integer.parseInt(expressionDetail[1]) : firstPage;

          if (firstPage == -1 || lastPage == -1 || firstPage > lastPage)
            throw new ConnectorException(ERROR_INVALID_EXPRESSION,
                getLogSignature() + "Expression is <firstPage>-<lastPage> where firstPage<=lastPage :" + firstPage + ","
                    + lastPage);
        } catch (Exception e) {
          throw new ConnectorException(ERROR_INVALID_EXPRESSION,
              getLogSignature() + "Expression must be <firstPage>-<lastPage> : received[" + oneExpression + "] " + e);
        }
        for (int pageIndex = firstPage; pageIndex <= lastPage; pageIndex++) {
          if (pageIndex <= sourceDocument.getNumberOfPages()) {
            // getPage starts at 0, pageIndex start at 1
            destinationDocument.addPage(sourceDocument.getPage(pageIndex - 1));
            nbPagesExtracted++;
          }
        }
      }

      FileVariable outputFileVariable = PdfToolbox.saveOutputPdfDocument(destinationDocument, destinationFileName,
          destinationStorageDefinition, getName());
      FileVariableReference outputFileReference = fileRepoFactory.saveFileVariable(outputFileVariable);

      PdfExtractPagesOutput pdfExtractPagesOutput = new PdfExtractPagesOutput();
      pdfExtractPagesOutput.destinationFile = outputFileReference.toJson();

      logger.info(
          getLogSignature() + "Extract " + nbPagesExtracted + " pages from document[" + docSource.getName() + "] to ["
              + destinationFileName + "]");
      return pdfExtractPagesOutput;
    } catch (Exception e) {
      logger.error(getLogSignature() + "During extraction " + e);
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

  public String getName() {
    return "PDF Extract pages";
  }

  private String getLogSignature() {
    return "Connector [" + getName() + "]:";
  }
}
