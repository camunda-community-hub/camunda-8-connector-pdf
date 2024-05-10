package io.camunda.connector.pdf.sharedfunctions;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.pdf.PdfInput;
import io.camunda.connector.pdf.toolbox.PdfSubFunction;
import io.camunda.connector.pdf.toolbox.PdfToolbox;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.StorageDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class RetrieveStorageDefinition {
  private static final Logger logger = LoggerFactory.getLogger(RetrieveStorageDefinition.class.getName());

  private static final String ERROR_BAD_STORAGE_DEFINITION = "BAD_STORAGE_DEFINITION";
  private static final String ERROR_BAD_STORAGE_DEFINITION_LABEL = "The storage definition is not correctly describe";
  private static final String ERROR_NO_DESTINATION_STORAGE_DEFINITION_DEFINE = "ERROR_NO_DESTINATION_STORAGE_DEFINITION_DEFINE";
  private static final String ERROR_NO_DESTINATION_STORAGE_DEFINITION_DEFINE_LABEL = "No Storage definition is found, please define one";

  /**
   * Toolbox, only static method
   */
private RetrieveStorageDefinition() {}
  /**
   * From the Input or a default Doc source, return the StorageDefinition
   *
   * @param pdfInput             odfInput
   * @param defaultDocSource     default doc source, to get the storage from that doc source if there is no definition in the input
   * @param throwExceptionIfNull if storage definition is null, thrown an exception
   * @param subFunction          source sub function to log it
   * @return the storage definition, or an exception. May be null if nothing is declared at input or no default doc
   */
  public static StorageDefinition getStorageDefinition(PdfInput pdfInput,
                                                       FileVariable defaultDocSource,
                                                       boolean throwExceptionIfNull,
                                                       PdfSubFunction subFunction) {
    String destinationStorageDefinitionSt = pdfInput.getDestinationStorageDefinition();

    StorageDefinition destinationStorageDefinition;
    if (destinationStorageDefinitionSt != null && !destinationStorageDefinitionSt.trim().isEmpty()) {
      try {
        destinationStorageDefinition = StorageDefinition.getFromString(destinationStorageDefinitionSt);
      } catch (Exception e) {
        logger.error("{} Bad storage definition [{}]", PdfToolbox.getLogSignature(subFunction),
            destinationStorageDefinitionSt);
        throw new ConnectorException(ERROR_BAD_STORAGE_DEFINITION,
            PdfToolbox.getLogSignature(subFunction) + "Can't decode StorageDefinition ["
                + destinationStorageDefinitionSt + "]");
      }
    } else {
      destinationStorageDefinition = defaultDocSource == null ? null : defaultDocSource.getStorageDefinition();
    }
    if (throwExceptionIfNull && destinationStorageDefinition == null) {
      // no storage : this is a problem
      logger.error("{} no destination storage definition define ", PdfToolbox.getLogSignature(subFunction));
      throw new ConnectorException(ERROR_NO_DESTINATION_STORAGE_DEFINITION_DEFINE);
    }

    return destinationStorageDefinition;
  }

  /**
   * Return the list of BPMN Error the RetrieveStorageDefinition can return
   *
   * @return list of BPMN Error under a Map(Code,Label)
   */
  public static Map<String, String> getBpmnErrors() {
    return Map.of(ERROR_NO_DESTINATION_STORAGE_DEFINITION_DEFINE, ERROR_NO_DESTINATION_STORAGE_DEFINITION_DEFINE_LABEL,
        ERROR_BAD_STORAGE_DEFINITION, ERROR_BAD_STORAGE_DEFINITION_LABEL);
  }
}
