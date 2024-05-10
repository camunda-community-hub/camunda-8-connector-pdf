package io.camunda.connector.pdf.toolbox;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.pdf.PdfInput;
import io.camunda.connector.pdf.PdfOutput;

import java.util.List;
import java.util.Map;

public interface PdfSubFunction {
  PdfOutput executeSubFunction(PdfInput pdfInput, OutboundConnectorContext context) throws ConnectorException;

  List<PdfParameter> getSubFunctionParameters(TypeParameter typeParameter);

  Map<String, String> getSubFunctionListBpmnErrors();

  String getSubFunctionName();

  String getSubFunctionDescription();

  String getSubFunctionType();

  enum TypeParameter {INPUT, OUTPUT}

}
