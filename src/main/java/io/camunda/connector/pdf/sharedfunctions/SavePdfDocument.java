package io.camunda.connector.pdf.sharedfunctions;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.pdf.PdfOutput;
import io.camunda.connector.pdf.toolbox.PdfSubFunction;
import io.camunda.connector.pdf.toolbox.PdfToolbox;
import io.camunda.filestorage.FileRepoFactory;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.FileVariableReference;
import io.camunda.filestorage.storage.StorageDefinition;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Map;

public class SavePdfDocument {

  private static final Logger logger = LoggerFactory.getLogger(SavePdfDocument.class.getName());

  private static final String ERROR_CREATE_FILEVARIABLE = "ERROR_CREATE_FILEVARIABLE";
  private static final String ERROR_CREATE_FILEVARIABLE_LABEL = "Error when reading the PDF to create a fileVariable to save";
  private static final String ERROR_SAVE_ERROR = "SAVE_ERROR";
  private static final String ERROR_SAVE_ERROR_LABEL = "An error occurs during the save";

  /**
   * Toolbox, only static method
   */
  private SavePdfDocument() {
  }

  /**
   * Save the pdf produce in a FileRepoFactory using the storageDefinition
   *
   * @param pdfOutput           pdfOutput, will be returned by the function
   * @param destinationDocument psf document
   * @param fileName            file name of the document
   * @param storageDefinition   storage definition to save the document
   * @param fileRepoFactory     file repo factory to send the document
   * @param subFunction         caller
   * @return the pdfOutput, completed
   * @throws ConnectorException different exception can be produced
   */
  public static PdfOutput savePdfFile(PdfOutput pdfOutput,
                                      PDDocument destinationDocument,
                                      String fileName,
                                      StorageDefinition storageDefinition,
                                      FileRepoFactory fileRepoFactory,
                                      PdfSubFunction subFunction,
                                      OutboundConnectorContext outboundConnectorContext) throws ConnectorException {
    FileVariable fileVariableOut = new FileVariable();
    try {

      // First, save the document to a FileVariable
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      try {
        destinationDocument.save(byteArrayOutputStream);

        fileVariableOut.setValue(byteArrayOutputStream.toByteArray());
        fileVariableOut.setName(fileName);
        fileVariableOut.setStorageDefinition(storageDefinition);
      } catch (Exception e) {
        logger.error("{} Error during save to name[{}] StorageDefinition[{}] : {}",
            PdfToolbox.getLogSignature(subFunction), fileName, storageDefinition, e.getMessage());

        throw new ConnectorException(ERROR_CREATE_FILEVARIABLE,
            "Name [" + fileName + "] StorageDefinition [" + storageDefinition + "] Error " + e);
      }

      // Second, write it to the fileRepo
      FileVariableReference outputFileReference = fileRepoFactory.saveFileVariable(fileVariableOut, outboundConnectorContext);

      // je triche : on renome le fichier

      String fileNameToRename= outputFileReference.content.toString();
      File oldFile = new File("c:/temp/unzip/result/" + fileNameToRename);
      int index = fileNameToRename.indexOf(" - [");
      String newFileName = fileNameToRename;
      if (index != -1) {
        newFileName = fileNameToRename.substring(0, index)+".pdf";
      } else {
        index = fileNameToRename.indexOf(".pdf");
        newFileName = fileNameToRename.substring(0, index)+".pdf";
      }
      // New file name in the same directory
      File newFile = new File("c:/temp/unzip/result/" +newFileName);
      // Rename the file
      boolean success = oldFile.renameTo(newFile);

      pdfOutput.destinationFile = outputFileReference.toJson();
      return pdfOutput;
    } catch (Exception e) {
      logger.error("{} Error during save to name[{}] StorageDefinition[{}] : {}",
          PdfToolbox.getLogSignature(subFunction), fileVariableOut.getName(), fileVariableOut.getStorageDefinition(),
          e.getMessage());

      throw new ConnectorException(ERROR_SAVE_ERROR,
          "Name [" + fileVariableOut.getName() + "] StorageDefinition [" + fileVariableOut.getStorageDefinition()
              + "] Error " + e);
    }
  }

  /**
   * Save file
   * @param pdfOutput pdfOutput to save
   * @param fileVariable file variable to save the value
   * @param fileRepoFactory repository
   * @param subFunction subfunction to log
   * @param outboundConnectorContext Outbound connector
   * @throws ConnectorException
   */
  public static void saveFile(PdfOutput pdfOutput,
                              FileVariable fileVariable,
                              FileRepoFactory fileRepoFactory,
                              PdfSubFunction subFunction,
                              OutboundConnectorContext outboundConnectorContext) throws ConnectorException {

    try {
      FileVariableReference outputFileReference = fileRepoFactory.saveFileVariable(fileVariable,outboundConnectorContext);
      pdfOutput.addDestinationFileInList(outputFileReference.toJson());
    } catch (Exception e) {
      logger.error("{} Error during save FileVariable[{}] StorageDefinition[{}] : {}",
          PdfToolbox.getLogSignature(subFunction), fileVariable.getName(), fileVariable.getStorageDefinition(),
          e.getMessage());

      throw new ConnectorException(ERROR_SAVE_ERROR, "Error " + e);
    }

  }

  /**
   * Return the list of BPMN Error the loadPdfDocument can return
   *
   * @return list of BPMN Error under a Map(Code,Label)
   */
  public static Map<String, String> getBpmnErrors() {
    return Map.of(ERROR_CREATE_FILEVARIABLE, ERROR_CREATE_FILEVARIABLE_LABEL, ERROR_SAVE_ERROR, ERROR_SAVE_ERROR_LABEL);
  }


}
