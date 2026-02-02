package io.camunda.connector.pdf;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.camunda.connector.cherrytemplate.CherryOutput;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.pdf.toolbox.ParameterToolbox;
import io.camunda.connector.pdf.toolbox.PdfSubFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PdfOutput implements CherryOutput {

    public static final String OUTPUT_DESTINATION_FILE = "destinationFile";
    public static final String OUTPUT_LIST_DESTINATION_FILE = "listDestinationFile";
    public static final RunnerParameter PDF_PARAMETER_DESTINATION_FILE = new RunnerParameter(OUTPUT_DESTINATION_FILE, // name
            "Destination variable name", // label
            String.class, // class
            RunnerParameter.Level.REQUIRED, "Process variable where the file reference is saved");
    public static final RunnerParameter PDF_PARAMETER_LIST_DESTINATION_FILE = new RunnerParameter(OUTPUT_LIST_DESTINATION_FILE,
            // name
            "List of Destination variable name", // label
            List.class, // class
            RunnerParameter.Level.REQUIRED, "Process variable where the list of files reference is saved");
    public String destinationFile;
    public List<String> listDestinationsFile = new ArrayList<>();

    public void addDestinationFileInList(String destinationFile) {
        listDestinationsFile.add(destinationFile);
    }

    @JsonIgnore
    @Override
    public List<Map<String, Object>> getOutputParameters() {
        return ParameterToolbox.getOutputParameters();
    }

}
