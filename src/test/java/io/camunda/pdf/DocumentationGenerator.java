package io.camunda.pdf;

import io.camunda.cherry.definition.connector.SdkRunnerCherryConnector;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.pdf.PdfFunction;
import io.camunda.connector.pdf.PdfInput;
import io.camunda.connector.pdf.toolbox.PdfSubFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Generate the documentation in md
 **/
public class DocumentationGenerator {

    private static final Logger logger = LoggerFactory.getLogger(DocumentationGenerator.class.getName());

    public static void main(String[] args) {
        DocumentationGenerator documentationGenerator = new DocumentationGenerator();
        documentationGenerator.generate("./doc/", "Functions.md", new PdfFunction());

    }

    /**
     * Generate the documentation
     * @param folder folder to write the documentation
     * @param fileName name of the documentation
     * @param pdfFunction CmisObject
     */
    public void generate(String folder, String fileName, PdfFunction pdfFunction) {
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(folder + fileName))) {
            logger.info("Generating {}/{}", folder, fileName);

            SdkRunnerCherryConnector cherryConnector = new SdkRunnerCherryConnector(pdfFunction);
            writeLine(writer, "# " + cherryConnector.getDisplayLabel());
            if (cherryConnector.getLogo() != null) {
                writeLine(writer, "[Description](data:image/png;base64," + cherryConnector.getLogo() + ")");
            }
            writeLine(writer, "");
            writeLine(writer, cherryConnector.getDescription());
            // for each function
            for (PdfSubFunction function : pdfFunction.getListSubFunctions()) {
                writeTitle(writer, "##", addSpace(function.getSubFunctionName()));

                writeLine(writer, function.getSubFunctionDescription());

                writeTitle(writer, "###", "Inputs");
                writeParameters(writer, function.getInputsParameter());

                writeTitle(writer, "###", "Outputs");
                writeParameters(writer, function.getOutputsParameter());

                writeTitle(writer, "###", "Errors");
                writeErrors(writer, function.getBpmnErrors());
            }
            writer.flush();
        } catch (Exception e) {
            logger.error("Exception during generation ", e);
        }


    }



    /**
     * write a line
     * @param writer writer to write to
     * @param line line to write
     * @throws IOException
     */
    private void writeLine(BufferedWriter writer, String line) throws IOException {
        writer.write(line);
        writer.newLine();
        logger.info(line);
    }


    /**
     * write a Markdown title
     * @param writer writer to write to
     * @param level level
     * @param title title to write
     * @throws IOException
     */
    private void writeTitle(BufferedWriter writer, String level, String title) throws IOException {
        writer.newLine();
        writer.newLine();
        writer.write(level + " " + title);
        writer.newLine();
        logger.info(level + " " + title);
    }

    /**
     * Write Runner parameters
     * @param writer writer to write to
     * @param parameters list of all Parameters to write
     * @throws IOException
     */
    private void writeParameters(BufferedWriter writer, List<RunnerParameter> parameters) throws IOException {
        logger.info("parameters...");
        List<Map<String, String>> records = parameters.stream()
                .map(t -> Map.of("Name", t.getName(),
                        "Description", t.label,
                        "Class", t.clazz.getName(),
                        "Level", t.level.name()))
                .toList();
        writeTable(writer,
                List.of("Name", "Description", "Class", "Level"),
                records);
        writer.newLine();

    }

    /**
     * Write the BPMN Error structure
     * @param writer writer to write to
     * @param bpmnErrors BpmnError structure
     * @throws IOException
     */
    public void writeErrors(BufferedWriter writer, Map<String, String> bpmnErrors) throws IOException {
        logger.info("errors...");
        List<Map<String, String>> records = bpmnErrors.entrySet().stream()
                .map(t -> Map.of("Name", t.getKey(),
                        "Explanation", t.getValue()))
                .collect(Collectors.toList());
        writeTable(writer,
                List.of("Name", "Explanation"),
                records);
        writer.newLine();
    }


    /**
     * Write a Markdown table, ensure each column has the same length
     * @param writer writer to write to
     * @param columns list of Columns and keys in the records. Example "Name", "Explanation"
     * @param records list of Records, containing key. Example [{"Name":"USA", "Explanation": "Country in North America"}, {"Name": "France", "Explanation":"Country in europe"}]
     * @throws IOException
     */
    private void writeTable(BufferedWriter writer, List<String> columns, List<Map<String, String>> records) throws IOException {
        Map<String, Integer> sizeColumns = new HashMap<>();
        for (String header : columns) {
            int size = header.length();
            int maxLength = records.stream()
                    .map(m -> m.get(header))                // extract the value for the given key
                    .filter(Objects::nonNull)            // skip null values
                    .mapToInt(String::length)            // get each string length
                    .max()                               // find the max
                    .orElse(0);
            sizeColumns.put(header, Math.max(size, maxLength) + 1);
        }
        // Now write it
        StringBuffer lineHeader = new StringBuffer();
        StringBuffer lineSeparator = new StringBuffer();
        for (String header : columns) {
            lineHeader.append("| " + complete(header, sizeColumns.get(header), " "));
            lineSeparator.append("|-" + complete("-", sizeColumns.get(header), "-"));
        }
        lineHeader.append("|");
        lineSeparator.append("|");
        writeLine(writer, lineHeader.toString());
        writeLine(writer, lineSeparator.toString());

        // Write content now
        for (Map<String, String> record : records) {
            StringBuffer lineRecord = new StringBuffer();

            for (String header : columns) {
                lineRecord.append("| " + complete(record.get(header), sizeColumns.get(header), " "));
            }
            lineRecord.append("|");
            writeLine(writer, lineRecord.toString());
        }

    }

    /**
     * Complete a string, to have the same lengh
     * @param originalValue original value to complete
     * @param size size expected
     * @param complement string (one character expected) to complete the string
     * @return
     */
    private String complete(String originalValue, int size, String complement) {
        int remaining = size - originalValue.length();
        if (remaining <= 0) return originalValue;

        return originalValue + complement.repeat(remaining);
    }

    /**
     * Transform a CamelString to a String with space.
     * @param text input String, like "CamelTextToTestIt"
     * @return the text transformed, like "Camel text to test it"
     */
    private String addSpace(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.replaceAll("(?<=[a-z])([A-Z])", " $1").toLowerCase().replaceFirst("^.", text.substring(0, 1));
    }
}
