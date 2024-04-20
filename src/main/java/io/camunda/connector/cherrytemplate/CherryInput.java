package io.camunda.connector.cherrytemplate;

import java.util.List;
import java.util.Map;

public interface CherryInput {
  /**
   * get the list of Input Parameters
   *
   * @return list of Map. Map contains key "name", "label", "defaultValue", "class", "level", "explanation"
   */
  List<Map<String, Object>> getInputParameters();

}
