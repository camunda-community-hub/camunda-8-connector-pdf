package io.camunda.connector.pdf.mergedocument;

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
import io.camunda.filestorage.FileVariableReference;
import io.camunda.filestorage.StorageDefinition;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PdfMergeDocumentFunction implements PdfSubFunction {
  public static final String ERROR_NO_DESTINATION_STORAGE_DEFINITION_DEFINE = "ERROR_NO_DESTINATION_STORAGE_DEFINITION_DEFINE";

  Logger logger = LoggerFactory.getLogger(PdfMergeDocumentFunction.class.getName());

  /**
   * Execute the subfonction
   *
   * @param pdfInput                 the input of connector
   * @param outboundConnectorContext outbound connector execution
   * @return an Output object
   * @throws ConnectorException in case of any error
   */
  public PdfOutput executeSubFunction(PdfInput pdfInput, OutboundConnectorContext outboundConnectorContext)
      throws ConnectorException {
    logger.debug("{} Start MergeDocument", PdfToolbox.getLogSignature(this));

    FileRepoFactory fileRepoFactory = FileRepoFactory.getInstance();

    PDDocument destinationDocument = null;
    // PDF Library oblige to keep all documents open until we write the destinationDocument. So, keep it here, and close all at the end
    List<PDDocument> sourceDocumentsList = new ArrayList<>();

    try {
      List<FileVariableReference> fileVariableReferenceList = new ArrayList<>();

      List<String> referenceDocSource = pdfInput.getListSourceFile();
      logger.info("{} Start MergeDocument {} documents", PdfToolbox.getLogSignature(this), referenceDocSource.size());

      for (String reference : referenceDocSource) {
        fileVariableReferenceList.add(FileVariableReference.fromJson(reference));
      }

      String destinationFileName = pdfInput.getDestinationFileName();
      StorageDefinition destinationStorageDefinition = RetrieveStorageDefinition.getStorageDefinition(pdfInput, null,
          false, this);

      // Merge
      int nbPagesMerged = 0;
      destinationDocument = new PDDocument();

      for (FileVariableReference fileVariableReference : fileVariableReferenceList) {
        FileVariable docFileToAdd = LoadDocument.loadDocSourceFromReference(fileVariableReference, fileRepoFactory,
            this);

        if (destinationStorageDefinition == null)
          destinationStorageDefinition = docFileToAdd.getStorageDefinition();

        PDDocument docSourcePDF = LoadPdfDocument.loadPdfDocument(docFileToAdd, this);
        sourceDocumentsList.add(docSourcePDF);

        // add all pages from sources
        for (int pageIndex = 0; pageIndex < docSourcePDF.getNumberOfPages(); pageIndex++) {
          // getPage starts at 0, pageIndex start at 1
          destinationDocument.addPage(docSourcePDF.getPage(pageIndex));
          nbPagesMerged++;
        }
      } // end merge

      // if the destination is null here, there is a issue
      if (destinationStorageDefinition == null) {
        // no storage : this is a problem
        logger.error("{} no destination storage definition defined ", PdfToolbox.getLogSignature(this));
        throw new ConnectorException(ERROR_NO_DESTINATION_STORAGE_DEFINITION_DEFINE);

      }

      // produce the result, and save it in the pdfOutput
      // Exception PdfToolbox.ERROR_CREATE_FILEVARIABLE, PdfToolbox.ERROR_SAVE_ERROR
      PdfOutput pdfOutput = SavePdfDocument.savePdfFile(new PdfOutput(), destinationDocument, destinationFileName,
          destinationStorageDefinition, fileRepoFactory, this);

      logger.info("{} merge {} pages from {} documents to [{}]", PdfToolbox.getLogSignature(this), nbPagesMerged,
          fileVariableReferenceList.size(), destinationFileName);
      return pdfOutput;
    } catch (Exception e) {
      logger.error("{} Exception during merge {}", PdfToolbox.getLogSignature(this), e);
      throw new ConnectorException(PdfToolbox.ERROR_DURING_OPERATION, "Error " + e);
    } finally {
      if (destinationDocument != null) {
        try {
          destinationDocument.close();
        } catch (Exception e) {
          // don't care
        }
        for (PDDocument doc : sourceDocumentsList) {
          try {
            doc.close();
          } catch (Exception e) {
            // don't care
          }
        }
      }
    } // end finally

  }

  public List<PdfParameter> getSubFunctionParameters(TypeParameter typeParameter) {
    if (typeParameter.equals(TypeParameter.INPUT)) {

      return Arrays.asList(new PdfParameter(PdfInput.INPUT_LIST_SOURCE_FILE, // name
              "List source file", // label
              List.class, // class
              CherryInput.PARAMETER_MAP_LEVEL_OPTIONAL, // level
              "List of FileVariable for the file to convert", 1),

          PdfInput.pdfParameterDestinationFileName, PdfInput.pdfParameterDestinationStorageDefinition);
    } else {
      return List.of(PdfOutput.PDF_PARAMETER_DESTINATION_FILE);
    }
  }

  public String getSubFunctionName() {
    return "Merge documents";
  }

  @Override
  public String getSubFunctionType() {
    return "merge-documents";
  }

  public String getSubFunctionDescription() {
    return "Merge two PDF documents in one PDF";
  }

  private static final Map<String, String> listBpmnErrors = new HashMap<>();

  static {
    listBpmnErrors.putAll(LoadDocument.getBpmnErrors());
    listBpmnErrors.putAll(RetrieveStorageDefinition.getBpmnErrors());
    listBpmnErrors.putAll(LoadPdfDocument.getBpmnErrors());
    listBpmnErrors.putAll(SavePdfDocument.getBpmnErrors());
    listBpmnErrors.put(PdfToolbox.ERROR_DURING_OPERATION, PdfToolbox.ERROR_DURING_OPERATION);
    listBpmnErrors.put(ERROR_NO_DESTINATION_STORAGE_DEFINITION_DEFINE,
        "A storage definition must be set to store the result document");
  }

  public Map<String, String> getSubFunctionListBpmnErrors() {
    return listBpmnErrors;
  }

}
