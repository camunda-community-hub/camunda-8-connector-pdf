package io.camunda.connector.pdf.toolbox;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.pdf.PdfInput;
import io.camunda.connector.pdf.PdfOutput;

import java.util.List;
import java.util.Map;

public interface PdfSubFunction {
  PdfOutput executeSubFunction(PdfInput pdfInput, OutboundConnectorContext context) throws ConnectorException;
  String getSubFunctionName();

  String getSubFunctionDescription();

  String getSubFunctionType();

  List<RunnerParameter> getInputsParameter();

  List<RunnerParameter> getOutputsParameter();

  Map<String, String> getBpmnErrors();

  enum TypeParameter {INPUT, OUTPUT}

}
