[![Community badge: Incubating](https://img.shields.io/badge/Lifecycle-Incubating-blue)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#incubating-)
[![Community extension badge](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)

# camunda-8-connector-pdf
Multiple Connectors on PDF documents.

These connectors used the File Storage library to access and save the document.
Use the Load connector to upload the document in the process. See Cherry FileStorage connector (https://github.com/camunda-community-hub/zeebe-cherry-framework or https://github.com/camunda-community-hub/camunda-8-connector-filestorage)

# c-pdf-mergepages

## Principle

## Inputs
| Name                           | Description                                                                                                         | Class             | Default | Level     |
|--------------------------------|---------------------------------------------------------------------------------------------------------------------|-------------------|---------|-----------|
| sourceFileVariableFileVariable | For the file to convert                                                                                             | java.lang.Object  |         | REQUIRED  |

## Output
| Name             | Description                                         | Class             | Level    |
|------------------|-----------------------------------------------------|-------------------|----------|
| pdfFileVariable  | FileVariable converted (a File Variable Reference)  | java.lang.Object  | REQUIRED |


## BPMN Errors

| Name             | Explanation       |
|------------------|-------------------|
| LOAD_FILE_ERROR  | Load File error   |




# extract pages

## Principle

## Inputs
| Name                           | Description                                                                                                         | Class             | Default | Level     |
|--------------------------------|---------------------------------------------------------------------------------------------------------------------|-------------------|---------|-----------|
| sourceFileVariableFileVariable | For the file to convert                                                                                             | java.lang.Object  |         | REQUIRED  |

## Output
| Name             | Description                                         | Class             | Level    |
|------------------|-----------------------------------------------------|-------------------|----------|
| pdfFileVariable  | FileVariable converted (a File Variable Reference)  | java.lang.Object  | REQUIRED |


## BPMN Errors

| Name             | Explanation       |
|------------------|-------------------|
| LOAD_FILE_ERROR  | Load File error   |



# Watermark

## Principle

## Inputs
| Name                           | Description                                                                                                         | Class             | Default | Level     |
|--------------------------------|---------------------------------------------------------------------------------------------------------------------|-------------------|---------|-----------|
| sourceFileVariableFileVariable | For the file to convert                                                                                             | java.lang.Object  |         | REQUIRED  |

## Output
| Name             | Description                                         | Class             | Level    |
|------------------|-----------------------------------------------------|-------------------|----------|
| pdfFileVariable  | FileVariable converted (a File Variable Reference)  | java.lang.Object  | REQUIRED |


## BPMN Errors

| Name             | Explanation       |
|------------------|-------------------|
| LOAD_FILE_ERROR  | Load File error   |











This connector merges two PDF files into one PDF file.

Two process variable provides documents. Process variables contain a reference to the document itself.
([File Storage](https://github.com/camunda-community-hub/zebee-cherry-filestorage) library).

The result is a new document saved with the same library. The result process variable contains the reference to the document.



## Manipulating file

Via the **File Storage** library, The process variable contains only a reference.
The core document, which may be saved in a Folder, Temporary Folder, or CMIS or any repository available via the library.

The result is saved in the File Storage: this is why the connetor ask for a StorageDefinition.
The result process variable will contains only the reference to the file. If no "destinationStorageDefinition"
is provided, the PDF is saved in the same storage than the source document.

To get the result of the file on a file system, use any connector or application using the File Storage API.

Find the user documentation in our [Camunda Platform 8 Docs](https://docs.camunda.io/docs/components/integration-framework/connectors/out-of-the-box-connectors/slack/).

# Build

```bash
mvn clean package
```

Two jars are produced. The jar with all dependency can be upload in the [Cherry Framework](https://github.com/camunda-community-hub/zeebe-cherry-framework)

## Element Template

The element templates can be found in the [element-templates](element-template) directory.






### Input

```JSON
{
  "sourceFile": ".....",
  "fileToAdd": "...",
  "destinationFileName": "documentMerged.pdf",
  "destinationStorageDefinition": "JSON"
}
```

SourceFile and fileToAdd are accessible via a reference. Visit [File Storage](https://github.com/camunda-community-hub/zebee-cherry-filestorage) library.
The file can be stored as a process variable in JSON or in an external Folder, a CMIS repository, etc...

Connectors to load and save files are available in the repository [Cherry Framework](https://github.com/camunda-community-hub/zeebe-cherry-framework)
Component [LoadFileFromDisk](https://github.com/camunda-community-hub/zeebe-cherry-framework/tree/main/src/main/java/io/camunda/cherry/files/LoadFileFromDiskWorker.java)
load a file from a disk, and [SaveFileToDisk](https://github.com/camunda-community-hub/zeebe-cherry-framework/tree/main/src/main/java/io/camunda/cherry/files/SaveFileToDiskWorker.java) save the file on a disk.

destinationFileName is the file name.

The destinationStorageDefinition indicates where the PDF file is produced. According to the File Storage library,
it can be on a disk, an external Folder, a CMIS repository, etc...

### Output

```JSON
{
  "destinationFile": "..."
}
```
The response will contain the reference where the file is saved, according to the storage definition.

### Errors

The connector can produce these BPMN errors:

**DEFINITION_ERROR**: one input file (source or filetoadd) can't be uploaded.
Process variables contain a reference to the file itself. These references were sent to the FileStorage library, but it wasn't acceptable.

**EXTRACTION_ERROR**: one input file (source or filetoadd) can't be read by the PDF Parser.

**MERGE_ERROR**: Error during the merge.

**ENCRYPTED_PDF_NOT_SUPPORTED**: the PDF library does not support encrypted document

**LOAD_ERROR**: An error arrived during the load of files in the filestorage library

**SAVE_ERROR**: an error arrived during the save of the file in the filestorage library



## Element Template

The element templates can be found in the [element-templates/PDF Merge document.json](element-templates/PDF Merge document.json) file.


# c-pdf-extractpages

Extract the Pages document from a source document, and create a new PDF document.
Expression to extract pages is:

```
"8" only one page, page number 8 (the first page is 1)
"5-7" pages 5 to 7* 
"2-n" pages 2 up to the end
"2-6,8,10-12,14-n" multiple expressions are accepted, separated by a comma.
```


# Build

```bash
mvn clean package
```

## API

### Input

```JSON
{
  "sourceFile": ".....",
  "extractExpression": "2-n",
  "destinationFileName": "documentExtracted.pdf",
  "destinationStorageDefinition": "JSON"
}
```

SourceFileis accessible via a reference. Visit [File Storage](https://github.com/camunda-community-hub/zebee-cherry-filestorage) library.
The file can be stored as a process variable in JSON or in an external Folder, a CMIS repository, etc...

Connectors to load and save files are available in the repository [Cherry Framework](https://github.com/camunda-community-hub/zeebe-cherry-framework)
Component [LoadFileFromDisk](https://github.com/camunda-community-hub/zeebe-cherry-framework/tree/main/src/main/java/io/camunda/cherry/files/LoadFileFromDiskWorker.java)
load a file from a disk, and [SaveFileToDisk](https://github.com/camunda-community-hub/zeebe-cherry-framework/tree/main/src/main/java/io/camunda/cherry/files/SaveFileToDiskWorker.java) save the file on a disk.

destinationFileName is the file name.

The destinationStorageDefinition indicates where the PDF file is produced. According to the File Storage library,
it can be on a disk, an external Folder, a CMIS repository, etc...


### Output

```JSON
{
  "destinationFile" : "..."
}
```
The response will contain the reference where the file is saved, according to the storage definition.

### Errors

The connector can produce these BPMN Errors:

**DEFINITION_ERROR**: one input file (source or filetoadd) can't be uploaded.
Process variables contain a reference to the file itself. These references were sent to the FileStorage library, but it wasn't acceptable.

**EXTRACTION_ERROR**: one input file (source or filetoadd) can't be read by the PDF Parser.

**ENCRYPTED_PDF_NOT_SUPPORTED**: the PDF library does not support encrypted document

**LOAD_ERROR**: An error arrived during the load of files in the filestorage library

**SAVE_ERROR**: an error arrived during the save of the file in the filestorage library

**INVALID_EXPRESSION**: the expression contains an error.


## Element Template

The element templates can be found in the [element-templates/PDF Extract pages.json](element-templates/PDF Extract pages.json) file.




# c-pdf-watermark

Add a watermark on a PDF document. Watermark position (TOP, CENTER, BOTTOM), rotation, and color can be defined.


# Build

```bash
mvn clean package
```

## API

### Input

```JSON
{
  "sourceFile": ".....",
  "watermark": "Draft version",
  "watermarkPosition": "CENTER",
  "watermarkColor": "red",
  "watermarkRotation": 45,
  "destinationFileName": "taggedDocument.pdf",
  "destinationStorageDefinition": "JSON"
}
```

SourceFileis accessible via a reference. Visit [File Storage](https://github.com/camunda-community-hub/zebee-cherry-filestorage) library.
The file can be stored as a process variable in JSON or in an external Folder, a CMIS repository, etc...

Connectors to load and save files are available in the repository [Cherry Framework](https://github.com/camunda-community-hub/zeebe-cherry-framework)
Component [LoadFileFromDisk](https://github.com/camunda-community-hub/zeebe-cherry-framework/tree/main/src/main/java/io/camunda/cherry/files/LoadFileFromDiskWorker.java)
load a file from a disk, and [SaveFileToDisk](https://github.com/camunda-community-hub/zeebe-cherry-framework/tree/main/src/main/java/io/camunda/cherry/files/SaveFileToDiskWorker.java) save the file on a disk.

The position is a string between "TOP", "CENTER", and "BOTTOM".

The color has to be chosen between "red", "green", "black", "blue", "cyan", "gray", "darkGray", "lightGray", "magenta", "orange", "pink", "white", "yellow".

The rotation is acceptable only when the watermark position is CENTER. The value is in degree, metric system.
0° is horizontal, 45° is diagonal, text start on the bottom left and moved up to the top right.

destinationFileName is the file name.

The destinationStorageDefinition indicates where the PDF file is produced. According to the File Storage library,
it can be on a disk, an external Folder, a CMIS repository, etc...


### Output

```JSON
{
  "destinationFile": "..."
}
```
The response will contain the reference where the file is saved, according to the storage definition.

### Errors

The connector can produce these BPMN Errors:

**DEFINITION_ERROR**: one input file (source or filetoadd) can't be uploaded.
Process variables contain a reference to the file itself. These references were sent to the FileStorage library, but it wasn't acceptable.

**EXTRACTION_ERROR**: one input file (source or filetoadd) can't be read by the PDF Parser.

**ENCRYPTED_PDF_NOT_SUPPORTED**: the PDF library does not support encrypted document

**LOAD_ERROR**: An error arrived during the load of files in the filestorage library

**SAVE_ERROR**: an error arrived during the save of the file in the filestorage library

**INVALID_COLOR**: the color is not in the list.


## Element Template

The element templates can be found in the [element-templates/PDF Add watermark.json](element-templates/PDF Add watermark.json) file.
