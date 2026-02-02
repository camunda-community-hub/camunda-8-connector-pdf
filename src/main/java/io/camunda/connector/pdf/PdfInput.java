package io.camunda.connector.pdf;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.cherrytemplate.CherryInput;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.pdf.toolbox.ParameterToolbox;
import io.camunda.connector.pdf.toolbox.PdfError;
import io.camunda.filestorage.storage.StorageDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * the JsonIgnoreProperties is mandatory: the template may contain additional widget to help the designer, especially on the OPTIONAL parameters
 * This avoids the MAPPING Exception
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PdfInput implements CherryInput {
    /**
     * Attention, each Input here must be added in the PdfFunction, list of InputVariables
     */
    public static final String PDFFUNCTION = "pdfFunction";
    /**
     * Input need for ExtractPages
     */
    public static final String SOURCE_FILE = "sourceFile";
    public static final String LIST_SOURCE_FILE = "listSourceFile";
    public static final String EXTRACT_EXPRESSION = "extractExpression";
    public static final String DESTINATION_FILE_NAME = "destinationFileName";
    public static final String DESTINATION_JSONSTORAGEDEFINITION = "destinationJsonStorageDefinition";
    public static final String DESTINATION_STORAGEDEFINITION = "destinationStorageDefinition";
    public static final String DESTINATION_STORAGEDEFINITION_COMPLEMENT = "destinationStorageDefinitionComplement";
    public static final String DESTINATION_STORAGEDEFINITION_CMIS = "destinationStorageDefinitionCmis";

    public static final String FILE_TO_ADD = "fileToAdd";
    /**
     * Watermark
     */
    public static final String WATERMARK = "waterMark";
    // default PdfToolbox.TEXT_POSITION.CENTER
    public static final String WATERMARK_POSITION = "watermarkPosition";
    public static final String WATERMARK_POSITION_TOP = "TOP";
    public static final String WATERMARK_POSITION_CENTER = "CENTER";
    public static final String WATERMARK_POSITION_BOTTOM = "BOTTOM";
    // default 0
    public static final String WATERMARK_ROTATION = "watermarkRotation";
    public static final String WATERMARK_FONTHEIGHT = "watermarkFontHeight";
    public static final String WATERMARK_COLOR = "watermarkColor";
    public static final String COLOR_RED = "red";
    public static final String COLOR_GREEN = "green";
    public static final String COLOR_BLACK = "black";
    public static final String COLOR_BLUE = "blue";
    public static final String COLOR_CYAN = "cyan";
    public static final String COLOR_GRAY = "gray";
    public static final String COLOR_DARKGRAY = "darkGray";
    public static final String COLOR_LIGHTGRAY = "lightGray";
    public static final String COLOR_MAGENTA = "magenta";
    public static final String COLOR_ORANGE = "orange";
    public static final String COLOR_PINK = "pink";
    public static final String COLOR_WHITE = "white";
    public static final String COLOR_YELLOW = "yellow";
    /**
     * PdfToImage
     */
    public static final String PDFTOIMAGE_DPI = "dpi";
    public static final RunnerParameter pdfParameterDestinationFileName = new RunnerParameter(
            PdfInput.DESTINATION_FILE_NAME,
            // name
            "Destination file name", // label
            String.class, // class
            RunnerParameter.Level.REQUIRED, // level
            "Name of the new file created");
    public static final RunnerParameter pdfParameterDestinationJsonStorageDefinition = new RunnerParameter(
            PdfInput.DESTINATION_JSONSTORAGEDEFINITION, // name
            "JSon Storage Destination", // label
            Map.class, // class
            RunnerParameter.Level.OPTIONAL, // level
            "Storage Definition in Json.");
    public static final RunnerParameter pdfParameterDestinationStorageDefinition = new RunnerParameter(
            PdfInput.DESTINATION_STORAGEDEFINITION, // name
            "Storage Destination", // label
            String.class, // class
            RunnerParameter.Level.OPTIONAL, // level
            "Storage Definition use to describe how to save the file. If not set, the storage used to read the source file is used.");
    public static final RunnerParameter pdfParameterDestinationStorageDefinitionComplement = new RunnerParameter(
            PdfInput.DESTINATION_STORAGEDEFINITION_COMPLEMENT, // name
            "Storage Destination Complement", // label
            String.class, // class
            RunnerParameter.Level.OPTIONAL, // level
            "Complement for some storage definition.");
    public static final RunnerParameter pdfParameterDestinationStorageDefinitionCmis = new RunnerParameter(
            PdfInput.DESTINATION_STORAGEDEFINITION_CMIS, // name
            "Storage Destination CMIS information", // label
            String.class, // class
            RunnerParameter.Level.OPTIONAL, // level
            "CMIS information when the storage is a CMIS repository.");
    private final Logger logger = LoggerFactory.getLogger(PdfInput.class.getName());
    public Long dpi = 300L;
    private String pdfFunction;
    private Object sourceFile;
    private List<Object> listSourceFile;
    private String extractExpression;
    private String destinationFileName;

    private Object destinationJsonStorageDefinition;
    private String destinationStorageDefinition;
    private String destinationStorageDefinitionComplement;
    private String destinationStorageDefinitionCmis;



    private String fileToAdd;
    private String waterMark;
    private String watermarkPosition;
    private String watermarkColor;
    private Long watermarkRotation;
    private Long watermarkFontHeight;

    public String getPdfFunction() {
        return pdfFunction;
    }

    public Object getSourceFile() {
        return sourceFile;
    }

    public List<Object> getListSourceFile() {
        return listSourceFile;
    }

    public String getExtractExpression() {
        return extractExpression == null || extractExpression.trim().isEmpty() ? "1-n" : extractExpression;
    }

    public String getDestinationFileName() {
        return destinationFileName;
    }

    public Object getDestinationJsonStorageDefinition() {
        return destinationJsonStorageDefinition;
    }

    public String getDestinationStorageDefinition() {
        return destinationStorageDefinition;
    }

    public String getDestinationStorageDefinitionComplement() {
        return destinationStorageDefinitionComplement;
    }

    public String getDestinationStorageDefinitionCmis() {
        return destinationStorageDefinitionCmis;
    }

    public String getWatermarkColor() {
        return watermarkColor;
    }

    public String getFileToAdd() {
        return fileToAdd;
    }

    public String getWaterMark() {
        return waterMark;
    }

    public String getWatermarkPosition() {
        return watermarkPosition;
    }

    public String getWatermarkcolor() {
        return watermarkColor;
    }

    public Long getWatermarkRotation() {
        return watermarkRotation;
    }

    public Long getWatermarkFontHeight() {
        return watermarkFontHeight;
    }

    public Long getDpi() {
        return dpi;
    }

    @JsonIgnore
    @Override
    public List<Map<String, Object>> getInputParameters() {
        return ParameterToolbox.getInputParameters();
    }

    /**
     * Return a Storage definition
     *
     * @return the storage definition
     * @throws ConnectorException if the connection
     */
    @JsonIgnore
    public StorageDefinition getDestinationStorageDefinitionObject() throws ConnectorException {
        try {
            StorageDefinition storageDefinitionObj = null;
            // Attention, it may be an empty string due to the modeler which not like null value
            if (getDestinationJsonStorageDefinition() != null && ! getDestinationJsonStorageDefinition().toString().trim().isEmpty()) {
                storageDefinitionObj = StorageDefinition.getFromObject(getDestinationJsonStorageDefinition());
                return storageDefinitionObj;
            }

            // It's acceptable to have a null value here
            if (destinationStorageDefinition==null || destinationStorageDefinition.toString().trim().isEmpty()) {
                return null;
            }
            storageDefinitionObj = StorageDefinition.getFromStorageDefinition(getDestinationStorageDefinition());
            storageDefinitionObj.complement = getDestinationStorageDefinitionComplement();
            if (storageDefinitionObj.complement != null && storageDefinitionObj.complement.isEmpty())
                storageDefinitionObj.complement = null;

            storageDefinitionObj.complementInObject = getDestinationStorageDefinitionCmis();
            return storageDefinitionObj;
        } catch (Exception e) {
            logger.error("Can't get the FileStorage - bad Gson value :" + destinationStorageDefinition);
            throw new ConnectorException(PdfError.INCORRECT_STORAGEDEFINITION,
                    "FileStorage information" + destinationStorageDefinition);
        }
    }
}
