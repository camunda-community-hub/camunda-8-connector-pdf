package io.camunda.connector.pdf.toolbox;

import io.camunda.connector.cherrytemplate.CherryInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PdfParameter {
  private static final Logger logger = LoggerFactory.getLogger(PdfParameter.class.getName());

  private final String name;

  private final String label;
  private final Class<?> classParameter;
  private final String level;
  private final String explanation;
  private final int priority;
  // we want to keep the order
  private final List<Map<String, String>> listOfChoices = new ArrayList<>();
  private final List<String> listRegisteredType = new ArrayList<>();

  /**
   * @param name           name
   * @param label          label
   * @param classParameter class
   * @param level          level:  CherryInput.PARAMETER_MAP_LEVEL_REQUIRED or  CherryInput.PARAMETER_MAP_LEVEL_OPTIONAL
   * @param explanation    explanation
   * @param priority       to order the parameters BETWEEN all functions, we use the priority field. Then, all priority
   */
  public PdfParameter(String name,
                      String label,
                      Class<?> classParameter,
                      String level,
                      String explanation,
                      int priority) {
    this.name = name;
    this.label = label;
    this.classParameter = classParameter;
    this.level = level;
    this.explanation = explanation;
    this.priority = priority;
  }

  public String getName() {
    return name;
  }

  public String getLabel() {
    return label;
  }

  public Class<?> getClassParameter() {
    return classParameter;
  }

  public String getLevel() {
    return level;
  }

  public String getExplanation() {
    return explanation;
  }

  public List<String> getListRegisteredType() {
    return listRegisteredType;
  }

  public void addRegisteredType(String type) {
    listRegisteredType.add(type);
  }

  public PdfParameter addChoice(String code, String displayName) {
    Map<String, String> oneChoice = new HashMap<>();
    oneChoice.put(CherryInput.PARAMETER_MAP_CHOICE_LIST_CODE, code);
    oneChoice.put(CherryInput.PARAMETER_MAP_CHOICE_LIST_DISPLAY_NAME, displayName);

    listOfChoices.add(oneChoice);
    return this;
  }

  public int getPriority() {
    return priority;
  }

  public Map<String, Object> getMap(String parameterNameForCondition) {
    Map<String, Object> oneParameter = new HashMap<>();
    oneParameter.put(CherryInput.PARAMETER_MAP_NAME, name);
    oneParameter.put(CherryInput.PARAMETER_MAP_LABEL, label);
    oneParameter.put(CherryInput.PARAMETER_MAP_CLASS, classParameter);
    oneParameter.put(CherryInput.PARAMETER_MAP_LEVEL, level);
    oneParameter.put(CherryInput.PARAMETER_MAP_EXPLANATION, explanation);

    oneParameter.put(CherryInput.PARAMETER_MAP_CONDITION, listOfChoices);
    oneParameter.put(CherryInput.PARAMETER_MAP_CHOICE_LIST, listOfChoices);

    if (!listRegisteredType.isEmpty()) {
      oneParameter.put(CherryInput.PARAMETER_MAP_CONDITION, parameterNameForCondition);
      oneParameter.put(CherryInput.PARAMETER_MAP_CONDITION_ONE_OF, listRegisteredType);
    }
    logger.info("PdfParameters getMap:{}", oneParameter);

    return oneParameter;
  }
}
