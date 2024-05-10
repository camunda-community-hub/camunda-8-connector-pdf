package io.camunda.connector.pdf.toolbox;

import io.camunda.connector.cherrytemplate.CherryInput;
import io.camunda.connector.pdf.PdfFunction;
import io.camunda.connector.pdf.PdfInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ParameterToolbox {
  private static final Logger logger = LoggerFactory.getLogger(ParameterToolbox.class.getName());

  /**
   * This is a toolbox, only static method
   */
  private ParameterToolbox() {
  }

  /**
   * Return the list of parameters
   *
   * @param typeParameter type of parameters (INPUT, OUTPUT)
   * @return list of parameters on a list of MAP
   */
  public static List<Map<String, Object>> getParameters(PdfSubFunction.TypeParameter typeParameter) {

    List<PdfParameter> pdfParametersCollectList = new ArrayList<>();
    logger.info("getParameters{}", typeParameter);

    // add the "choose the function" parameters
    PdfParameter chooseFunction = new PdfParameter(PdfInput.INPUT_PDFFUNCTION, "Pdf Function", String.class,
        CherryInput.PARAMETER_MAP_LEVEL_REQUIRED, "Choose the function to execute", 0);

    // add the input only at the INPUT parameters
    if (typeParameter == PdfSubFunction.TypeParameter.INPUT) {
      pdfParametersCollectList.add(chooseFunction);
    }

    //  now, we collect all functions, and for each function, we collect parameters
    for (Class<?> classFunction : PdfFunction.allFunctions) {
      try {
        Constructor<?> constructor = classFunction.getConstructor();
        PdfSubFunction inputSubFunction = (PdfSubFunction) constructor.newInstance();

        List<PdfParameter> subFunctionsParametersList = inputSubFunction.getSubFunctionParameters(typeParameter);
        chooseFunction.addChoice(inputSubFunction.getSubFunctionType(), inputSubFunction.getSubFunctionName());
        logger.info("PDFFunction detected {} add type choice {} parameterList.size={}",
            inputSubFunction.getSubFunctionName(), inputSubFunction.getSubFunctionType(),
            subFunctionsParametersList.size());

        for (PdfParameter parameter : subFunctionsParametersList) {

          // one parameter may be used by multiple functions, and we want to create only one, but play on condition to show it
          Optional<PdfParameter> parameterInList = pdfParametersCollectList.stream()
              .filter(t -> t.getName().equals(parameter.getName()))
              .findFirst();
          logger.info(" check param{} : Already present? {} ", parameter.getName(), parameterInList.isPresent());
          if (parameterInList.isEmpty()) {
            parameter.addRegisteredType(inputSubFunction.getSubFunctionType());
            // We search where to add this parameter. It is at the end of the group with the same priority
            int positionToAdd = 0;
            for (PdfParameter indexParameter : pdfParametersCollectList) {
              if (indexParameter.getPriority() <= parameter.getPriority())
                positionToAdd++;
            }
            pdfParametersCollectList.add(positionToAdd, parameter);
            logger.info(" Add it at position {} new Size={}", positionToAdd, pdfParametersCollectList.size());
            // Already exist
          } else {
            // Register this function in that parameter
            parameterInList.get().addRegisteredType(inputSubFunction.getSubFunctionType());
          }
        }
      } catch (Exception e) {
        logger.error("Exception during the getInputParameters functions {}", e.toString());
      }
    }

    // Now, build the list from all parameters collected
    // first, the function selection
    logger.info(" PdfParameter => Map Size={}", pdfParametersCollectList.size());

    return pdfParametersCollectList.stream().map(t -> t.getMap(PdfInput.INPUT_PDFFUNCTION)).toList();

  }

}
