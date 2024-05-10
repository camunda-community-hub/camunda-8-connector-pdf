package io.camunda.connector.pdf.sharedfunctions;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.pdf.toolbox.PdfSubFunction;
import io.camunda.connector.pdf.toolbox.PdfToolbox;
import io.camunda.filestorage.FileRepoFactory;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.FileVariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class LoadDocument {
  private static final Logger logger = LoggerFactory.getLogger(LoadDocument.class.getName());

  private static final String ERROR_LOAD_DOCSOURCE = "LOAD_DOCSOURCE";
  private static final String ERROR_LOAD_DOCSOURCE_LABEL = "The reference can't be decoded";

  private static final String ERROR_LOAD_ERROR = "LOAD_ERROR";
  private static final String ERROR_LOAD_ERROR_LABEL = "An error occurs during the load";

  /**
   * Toolbox, only static method
   */
  private LoadDocument() {}
  /**
   * From a source file reference, load the FileVariable Doc Source
   *
   * @param sourceFile      the reference (JSON containing the reference)
   * @param fileRepoFactory fileReport to load the fileVariable
   * @param subFunction     caller of the function
   * @return the FileVariable, else an error
   */
  public static FileVariable loadDocSource(String sourceFile,
                                           FileRepoFactory fileRepoFactory,
                                           PdfSubFunction subFunction) {
    try {
      FileVariableReference docSourceReference = FileVariableReference.fromJson(sourceFile);
      return loadDocSourceFromReference(docSourceReference, fileRepoFactory, subFunction);
    } catch (ConnectorException ce) {
      throw ce;
    } catch (Exception e) {
      logger.error("{} Exception during extraction on sourceFile[{}] : {} ", PdfToolbox.getLogSignature(subFunction), sourceFile, e);
      throw new ConnectorException(ERROR_LOAD_DOCSOURCE, "Document["+sourceFile+"] Error " + e);
    }
  }

  public static FileVariable loadDocSourceFromReference(FileVariableReference docReference,
                                                        FileRepoFactory fileRepoFactory,
                                                        PdfSubFunction subFunction) {
    try {
      FileVariable docSource = fileRepoFactory.loadFileVariable(docReference);

      // get the file
      if (docSource == null || docSource.getValue() == null) {
        throw new ConnectorException(ERROR_LOAD_ERROR,
            PdfToolbox.getLogSignature(subFunction) + "Can't read file [" + docReference.toJson() + "]");
      }
      return docSource;
    } catch (Exception e) {
      logger.error("{} Exception load [{}] : {} ", PdfToolbox.getLogSignature(subFunction), docReference, e);
      throw new ConnectorException(ERROR_LOAD_DOCSOURCE, "DocReference["+docReference+"] Error : " + e);
    }
  }

  /**
   * Return the list of BPMN Errors the loadPdfDocument can return
   *
   * @return list of BPMN Error under a Map(Code,Label)
   */
  public static Map<String, String> getBpmnErrors() {
    return Map.of(ERROR_LOAD_ERROR, ERROR_LOAD_ERROR_LABEL, ERROR_LOAD_DOCSOURCE, ERROR_LOAD_DOCSOURCE_LABEL);
  }
}
