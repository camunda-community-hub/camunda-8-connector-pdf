package io.camunda.connector.pdf.toolbox;

import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.pdf.PdfFunction;
import io.camunda.connector.pdf.PdfInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.*;

public class ParameterToolbox {
  private static final Logger logger = LoggerFactory.getLogger(ParameterToolbox.class.getName());

  /**
   * This is a toolbox, only static method
   */
  private ParameterToolbox() {
  }

  public static List<Map<String, Object>> getInputParameters() {
    return getParameters(true);
  }

  public static List<Map<String, Object>> getOutputParameters() {
    return getParameters(false);
  }


  /**
   * Return the list of parameters
   *
   * @param inputParameters true of false according the type of parameters
   * @return list of parameters on a list of MAP
   */
  private static List<Map<String, Object>> getParameters(boolean inputParameters) {

    List<RunnerParameter> runnerParametersCollectList = new ArrayList<>();
    logger.info("getParameters input? {}", inputParameters);

    // add the "choose the function" parameters
    RunnerParameter chooseFunction = new RunnerParameter(PdfInput.PDFFUNCTION,
            "FileStorage Function", String.class, RunnerParameter.Level.REQUIRED, "Choose the function to execute");
    chooseFunction.setAttribute("priority", -1);

    // add the input only at the INPUT parameters
    if (inputParameters) {
      runnerParametersCollectList.add(chooseFunction);
    }

    // We keep a list of parameters per type. Then, we will add a condition according to the type
    Map<String, List<String>> mapParameterSubType = new HashMap<>();

    //  now, we collect all functions, and for each function, we collect parameters
    for (Class<?> classFunction : PdfFunction.allFunctions) {
      try {
        Constructor<?> constructor = classFunction.getConstructor();
        PdfSubFunction inputSubFunction = (PdfSubFunction) constructor.newInstance();

        List<RunnerParameter> subFunctionsParametersList = inputParameters ?
                inputSubFunction.getInputsParameter() :
                inputSubFunction.getOutputsParameter();

        chooseFunction.addChoice(inputSubFunction.getSubFunctionType(), inputSubFunction.getSubFunctionName());
        logger.info("FileStorage SubFunctionName[{}] TypeChoice [{}] parameterList.size={}",
                inputSubFunction.getSubFunctionName(), inputSubFunction.getSubFunctionType(),
                subFunctionsParametersList.size());

        for (RunnerParameter parameter : subFunctionsParametersList) {

          // Record where the parameter is involved
          List<String> listSubFunctionForThisParameter = mapParameterSubType.getOrDefault(parameter.getName(), new ArrayList<>());
          listSubFunctionForThisParameter.add(inputSubFunction.getSubFunctionType());
          mapParameterSubType.put(parameter.getName(), listSubFunctionForThisParameter);

          // one parameter may be used by multiple functions, and we want to create only one, but play on condition to show it
          Optional<RunnerParameter> parameterInList = runnerParametersCollectList.stream()
                  .filter(t -> t.getName().equals(parameter.getName()))
                  .findFirst();
          if (parameterInList.isEmpty()) {
            // We search where to add this parameter. It is at the end of the group with the same priority
            int positionToAdd = 0;
            for (RunnerParameter indexParameter : runnerParametersCollectList) {
              if (indexParameter.getAttributeInteger("priority", 0) <= parameter.getAttributeInteger("priority", 0))
                positionToAdd++;
            }
            runnerParametersCollectList.add(positionToAdd, parameter);
            logger.info("  check parameter[{}.{}] : New Add at [{}] newSize[{}] - registered in[{}]",
                    inputSubFunction.getSubFunctionName(), parameter.getName(), positionToAdd,
                    runnerParametersCollectList.size(), parameter.getAttribute("ret"));
            // Already exist
          } else {
            // Register this function in that parameter
            logger.info("  check parameter[{}.{}] : Already exist - registered in[{}]",
                    inputSubFunction.getSubFunctionName(), parameter.getName(), inputSubFunction.getSubFunctionType());
          }
        }

      } catch (Exception e) {
        logger.error("Exception during the getInputParameters functions {}", e.toString());
      }
    }

    // Now, build the list from all parameters collected
    // We add a condition for each parameter
    for (RunnerParameter parameter : runnerParametersCollectList) {

      // Not apply a condition on the field used to choose the function
      if (parameter == chooseFunction)
        continue;
      // There is an explicit condition: do not override it
      if (parameter.getCondition() != null)
        continue;

      List<String> listFunctionForThisParameter = mapParameterSubType.get(parameter.getName());

      if (listFunctionForThisParameter == null || listFunctionForThisParameter.isEmpty()
              || listFunctionForThisParameter.size() == PdfFunction.allFunctions.size()) {
        logger.info("parameter [{}] Register in NO or ALL functions", parameter.getName());
      } else {
        logger.info("parameter [{}] Register in some functions [{}]", parameter.getName(),
                listFunctionForThisParameter);
        parameter.addCondition(chooseFunction.getName(), listFunctionForThisParameter);
      }
    }
    // first, the function selection
    logger.info(" FileStorageParameter => Map Size={}", runnerParametersCollectList.size());

    return runnerParametersCollectList.stream().map(t -> t.toMap(PdfInput.PDFFUNCTION)).toList();

  }

}
