package io.camunda.connector.pdf.sharedfunctions;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.pdf.toolbox.PdfSubFunction;
import io.camunda.connector.pdf.toolbox.PdfToolbox;
import io.camunda.filestorage.FileVariable;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class LoadPdfDocument {
  private static final String ERROR_LOAD_PDF_ERROR = "LOAD_PDF_ERROR";
  private static final String ERROR_LOAD_PDF_ERROR_LABEL = "Error reading the document - is that a PDF?";

  private static final String ERROR_ENCRYPTED_PDF_NOT_SUPPORTED = "ENCRYPTED_PDF_NOT_SUPPORTED";
  private static final String ERROR_ENCRYPTED_PDF_NOT_SUPPORTED_LABEL = "Encrypted PDF is not supported";

  private static final Logger logger = LoggerFactory.getLogger(LoadPdfDocument.class.getName());

  /**
   * Toolbox, only static method
   */
  private LoadPdfDocument() {
  }

  /**
   * Load a PDDocument from a file variable
   *
   * @param sourceFileVariable the source document to load
   * @return a PDocument
   */
  public static PDDocument loadPdfDocument(FileVariable sourceFileVariable, PdfSubFunction subFunction)
      throws ConnectorException {
    PDDocument sourceDocument;
    try {
      sourceDocument = PDDocument.load(sourceFileVariable.getValue());

    } catch (Exception e) {
      logger.error("{} Load PDF document : {} ", PdfToolbox.getLogSignature(subFunction), e);

      throw new ConnectorException(ERROR_LOAD_PDF_ERROR,
          "Connector[" + subFunction.getSubFunctionName() + "] Can't load document [" + sourceFileVariable.getName()
              + "]");
    }
    if (sourceDocument.isEncrypted()) {
      logger.error("{} PDF document is encrypted, this is not supported : {} ", PdfToolbox.getLogSignature(subFunction),
          sourceFileVariable.getName());
      throw new ConnectorException(ERROR_ENCRYPTED_PDF_NOT_SUPPORTED,
          "Connector[" + subFunction.getSubFunctionName() + "] Document is encrypted");
    }
    return sourceDocument;
  }

  /**
   * Return the list of BPMN Errors the loadPdfDocument can return
   *
   * @return list of BPMN Error under a Map(Code,Label)
   */
  public static Map<String, String> getBpmnErrors() {
    return Map.of(ERROR_LOAD_PDF_ERROR, ERROR_LOAD_PDF_ERROR_LABEL, ERROR_ENCRYPTED_PDF_NOT_SUPPORTED,
        ERROR_ENCRYPTED_PDF_NOT_SUPPORTED_LABEL);
  }
}
