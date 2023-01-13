package io.camunda.connector.pdf.mergedocument;

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

@OutboundConnector(name = PdfMergeDocumentFunction.TYPE_PDF_EXTRACTPAGES, inputVariables = {
    PdfMergeDocumentInput.INPUT_SOURCE_FILE, // reference to the source file, first page to add
    PdfMergeDocumentInput.INPUT_FILE_TO_ADD, // reference to the second source file
    PdfMergeDocumentInput.INPUT_DESTINATION_FILE_NAME,
    PdfMergeDocumentInput.INPUT_DESTINATION_STORAGEDEFINITION }, type = PdfMergeDocumentFunction.TYPE_PDF_EXTRACTPAGES)

public class PdfMergeDocumentFunction implements OutboundConnectorFunction {
  public static final String ERROR_MERGE_ERROR = "MERGE_ERROR";
  public static final String ERROR_DEFINITION_ERROR = "DEFINITION_ERROR";
  public static final String TYPE_PDF_EXTRACTPAGES = "c-pdf-mergepages";
  Logger logger = LoggerFactory.getLogger(PdfMergeDocumentFunction.class.getName());

  @Override
  public PdfMergeDocumentOutput execute(OutboundConnectorContext context) throws Exception {
    PdfMergeDocumentInput pdfExtractPagesInput = context.getVariablesAsType(PdfMergeDocumentInput.class);
    FileRepoFactory fileRepoFactory = FileRepoFactory.getInstance();

    FileVariableReference docSourceReference = null;
    FileVariableReference docFileToAddReference = null;
    PDDocument docSourcePDF = null;
    PDDocument docFileToAddPDF = null;
    PDDocument destinationDocument = null;

    int nbPagesExtracted = 0;
    logger.info(getLogSignature() + "SourceDocument=|" + pdfExtractPagesInput.getSourceFile() + "] AddDocument["
        + pdfExtractPagesInput.getFileToAdd() + "]");

    try {
      docSourceReference = FileVariableReference.fromJson(pdfExtractPagesInput.getSourceFile());
      FileVariable docSource = fileRepoFactory.loadFileVariable(docSourceReference);

      docFileToAddReference = FileVariableReference.fromJson(pdfExtractPagesInput.getFileToAdd());
      FileVariable docFileToAdd = fileRepoFactory.loadFileVariable(docFileToAddReference);

      String destinationFileName = pdfExtractPagesInput.getDestinationFileName();
      String destinationStorageDefinitionSt = pdfExtractPagesInput.getDestinationStorageDefinition();
      // get the file

      if (docSource == null || docSource.getValue() == null) {
        throw new ConnectorException(PdfToolbox.ERROR_LOAD_ERROR,
            getLogSignature() + "Can't read file [" + pdfExtractPagesInput.getSourceFile() + "]");
      }
      if (docFileToAdd == null || docFileToAdd.getValue() == null) {
        throw new ConnectorException(PdfToolbox.ERROR_LOAD_ERROR,
            getLogSignature() + "Can't read file [" + pdfExtractPagesInput.getFileToAdd() + "]");
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

      docSourcePDF = PdfToolbox.loadPdfDocument(docSource, getName());

      if (docSourcePDF.isEncrypted()) {
        throw new ConnectorException(PdfToolbox.ERROR_ENCRYPTED_PDF_NOT_SUPPORTED,
            getLogSignature() + "Document [" + pdfExtractPagesInput.getSourceFile() + "] is encrypted");
      }
      docFileToAddPDF = PdfToolbox.loadPdfDocument(docFileToAdd, getName());
      if (docFileToAddPDF.isEncrypted()) {
        throw new ConnectorException(PdfToolbox.ERROR_ENCRYPTED_PDF_NOT_SUPPORTED,
            getLogSignature() + "Document [" + pdfExtractPagesInput.getFileToAdd() + "] is encrypted");
      }
      destinationDocument = new PDDocument();

      // add all pages from sources
      for (int pageIndex = 0; pageIndex < docSourcePDF.getNumberOfPages(); pageIndex++) {
        // getPage starts at 0, pageIndex start at 1
        destinationDocument.addPage(docSourcePDF.getPage(pageIndex));
      }

      // add all pages from addPages
      for (int pageIndex = 0; pageIndex < docFileToAddPDF.getNumberOfPages(); pageIndex++) {
        // getPage starts at 0, pageIndex start at 1
        destinationDocument.addPage(docFileToAddPDF.getPage(pageIndex));
      }

      FileVariable outputFileVariable = PdfToolbox.saveOutputPdfDocument(destinationDocument, destinationFileName,
          destinationStorageDefinition, getName());
      PdfMergeDocumentOutput pdfExtractPagesOutput = new PdfMergeDocumentOutput();
      try {
        FileVariableReference outputFileReference = fileRepoFactory.saveFileVariable(outputFileVariable);
        pdfExtractPagesOutput.destinationFile = outputFileReference.toJson();
      } catch( Exception e) {
        throw new ConnectorException(PdfToolbox.ERROR_SAVE_ERROR, "Error "+e);
      }

      logger.info(getLogSignature() + "Merge " + docSourcePDF.getNumberOfPages() + nbPagesExtracted
          + " pages extracted from document[" + docSource.getName() + "] " + " and "
          + docFileToAddPDF.getNumberOfPages() + " pages document[" + docFileToAdd.getName() + "] " + "to ["
          + destinationFileName + "]");
      return pdfExtractPagesOutput;
    } catch (Exception e) {
      logger.error("During merge " + e);
      throw new ConnectorException(ERROR_MERGE_ERROR, "Error " + e);
    } finally {
      if (docSourcePDF != null)
        try {
          docSourcePDF.close();
        } catch (Exception e) {
          // don't care
        }
      if (docFileToAddPDF != null)
        try {
          docFileToAddPDF.close();
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
    return "PDF Merge document";
  }

  private String getLogSignature() {
    return "Connector [" + getName() + "]:";
  }
}
