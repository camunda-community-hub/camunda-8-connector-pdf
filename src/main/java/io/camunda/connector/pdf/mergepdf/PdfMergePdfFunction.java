package io.camunda.connector.pdf.mergepdf;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.pdf.PdfInput;
import io.camunda.connector.pdf.PdfOutput;
import io.camunda.connector.pdf.sharedfunctions.LoadDocument;
import io.camunda.connector.pdf.sharedfunctions.LoadPdfDocument;
import io.camunda.connector.pdf.sharedfunctions.RetrieveStorageDefinition;
import io.camunda.connector.pdf.sharedfunctions.SavePdfDocument;
import io.camunda.connector.pdf.toolbox.PdfSubFunction;
import io.camunda.connector.pdf.toolbox.PdfToolbox;
import io.camunda.filestorage.FileRepoFactory;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.FileVariableReference;
import io.camunda.filestorage.storage.StorageDefinition;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PdfMergePdfFunction implements PdfSubFunction {
  public static final String ERROR_NO_DESTINATION_STORAGE_DEFINITION_DEFINE = "ERROR_NO_DESTINATION_STORAGE_DEFINITION_DEFINE";
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

  Logger logger = LoggerFactory.getLogger(PdfMergePdfFunction.class.getName());

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
    // PDF Library obliges to keep all documents open until we write the destinationDocument. So, keep it here, and close all at the end
    List<PDDocument> sourceDocumentsList = new ArrayList<>();

    try {
      List<FileVariableReference> fileVariableReferenceList = new ArrayList<>();

      List<Object> referenceDocSource = pdfInput.getListSourceFile();
      logger.info("{} Start MergeDocument {} documents", PdfToolbox.getLogSignature(this), referenceDocSource.size());

      for (Object reference : referenceDocSource) {
        fileVariableReferenceList.add(FileVariableReference.fromObject(reference));
      }

      String destinationFileName = pdfInput.getDestinationFileName();
      StorageDefinition destinationStorageDefinition = RetrieveStorageDefinition.getStorageDefinition(pdfInput, null,
          false, this);

      // Merge
      int nbPagesMerged = 0;
      destinationDocument = new PDDocument();

      for (FileVariableReference fileVariableReference : fileVariableReferenceList) {
        FileVariable docFileToAdd = LoadDocument.loadDocSourceFromReference(fileVariableReference, fileRepoFactory,
            this,outboundConnectorContext);

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
          destinationStorageDefinition, fileRepoFactory, this,outboundConnectorContext);

      logger.info("{} merge {} pages from {} documents to [{}]", PdfToolbox.getLogSignature(this), nbPagesMerged,
          fileVariableReferenceList.size(), destinationFileName);
      return pdfOutput;
    } catch (ConnectorException ce) {
      // already logged
      throw ce;
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


  public String getSubFunctionName() {
    return "Merge documents";
  }

  @Override
  public String getSubFunctionType() {
    return "merge-pdfs";
  }

  public String getSubFunctionDescription() {
    return "Merge two PDFs document in one PDF";
  }


  public Map<String, String> getSubFunctionListBpmnErrors() {
    return listBpmnErrors;
  }

  @Override
  public List<RunnerParameter> getInputsParameter() {
    return Arrays.asList(new RunnerParameter(PdfInput.LIST_SOURCE_FILE, // name
                    "List source file", // label
                    List.class, // class
                    RunnerParameter.Level.OPTIONAL, // level
                    "List of FileVariable for the file to convert"),

            PdfInput.pdfParameterDestinationFileName,
            PdfInput.pdfParameterDestinationJsonStorageDefinition,
            PdfInput.pdfParameterDestinationStorageDefinition,
            PdfInput.pdfParameterDestinationStorageDefinitionComplement,
            PdfInput.pdfParameterDestinationStorageDefinitionCmis);
  }

  @Override
  public List<RunnerParameter> getOutputsParameter() {
    return List.of(PdfOutput.PDF_PARAMETER_DESTINATION_FILE);  }

  @Override
  public Map<String, String> getBpmnErrors() {
    return Map.of();
  }
}
