# camunda-8-connector-pdf
Connectors on PDF documents

These connectors used the File Storage library to access and save the document.
Use the Load connector to upload the document in the process. See Cherry FileStorage connector (https://github.com/camunda-community-hub/zeebe-cherry-framework or https://github.com/camunda-community-hub/camunda-8-connector-filestorage)

# Merge PDF document
The connector takes two PDF documents and merges them into one document.

# Extract Pages document to a new PDF document
The connector tale one PDF document and an expression like
```
"8" only one page, page number 8 (the first page is 1)
"5-7" pages 5 to 7* 
"2-n" pages 2 up to the end
"2-6,8,10-12,14-n" multiple expressions are accepted, separated by a comma.
```

# Watermark
Add a watermark on a PDF document. Watermark position can be defined (TOP, CENTER, BOTTOM), rotation, and color.