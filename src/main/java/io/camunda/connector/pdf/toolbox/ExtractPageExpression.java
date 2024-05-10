package io.camunda.connector.pdf.toolbox;

import io.camunda.connector.api.error.ConnectorException;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.Map;

public class ExtractPageExpression {

  public static final String ERROR_INVALID_EXPRESSION_LABEL = "Invalid expression to pilot the extraction. Format must be <number1>-<number2> where number1<=number2. n means 'end of document' : example, 10-n";
  private static final String ERROR_INVALID_EXPRESSION = "INVALID_EXPRESSION";
  private final String[] expressionsList;
  private final PdfSubFunction subFunction;

  /**
   * @param expression  expression to manage
   * @param pdfDocument PDFDocument
   * @param subFunction caller
   */
  public ExtractPageExpression(String expression, PDDocument pdfDocument, PdfSubFunction subFunction) {
    this.subFunction = subFunction;
    String extractExpressionResolved = expression.replace("n", String.valueOf(pdfDocument.getNumberOfPages()));
    expressionsList = extractExpressionResolved.split(",", 0);

  }

  /**
   * Return the list of BPMN Error the loadPdfDocument can return
   *
   * @return list of BPMN Error (list of Map(Code, Explanation))
   */
  public static Map<String, String> getBpmnErrorExtractExpression() {
    return Map.of(ERROR_INVALID_EXPRESSION, ERROR_INVALID_EXPRESSION_LABEL);
  }

  /**
   * Check if a page number is inside the expression, or not
   *
   * @param pageNumberToCheck page number to check, start at 1
   * @return true if the page is inside the expression, false else
   * @throws ConnectorException if the expression is incorrect
   */
  public boolean isPageInRange(int pageNumberToCheck) throws ConnectorException {
    for (String oneExpression : this.expressionsList) {
      int firstPage;
      int lastPage;
      // format must be <number1>-<number2> where number1<=number2
      try {
        String[] expressionDetail = oneExpression.split("-", 2);
        firstPage = expressionDetail.length >= 1 ? Integer.parseInt(expressionDetail[0]) : -1;
        lastPage = expressionDetail.length >= 2 ? Integer.parseInt(expressionDetail[1]) : firstPage;

        if (firstPage == -1 || lastPage == -1 || firstPage > lastPage)
          throw new ConnectorException(ERROR_INVALID_EXPRESSION, PdfToolbox.getLogSignature(subFunction)
              + "Expression is <firstPage>-<lastPage> where firstPage<=lastPage :" + firstPage + "," + lastPage);
      } catch (Exception e) {
        throw new ConnectorException(ERROR_INVALID_EXPRESSION,
            PdfToolbox.getLogSignature(subFunction) + "Expression must be <firstPage>-<lastPage> : received["
                + oneExpression + "] " + e);
      }
      for (int pageIndex = firstPage; pageIndex <= lastPage; pageIndex++) {
        if (pageIndex == pageNumberToCheck)
          return true;
      }
    }
    return false;
  }
}
