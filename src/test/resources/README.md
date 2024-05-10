# Run the test

## Test it

To test it:

### Specify a Zeebe server
Check the file src/test/resources/application.yaml and set up the configuration to connect a Zeebe server

## Deploy the process
Deploy the officeToPdf.bpmn process

![OfficeToPdf.bpmn](officeToPdf.png)

## Start the LocalConnectorRuntime
Source is located io.camunda.officetopdf.LocalConnectorRuntime. Start it.


## Create a process instance
Two tasks will be executed, and the process instance will show up in the Review Document. 
PDF are saved in the temporary folder of the machine.


## Information
The connector load a WORD and a OPENOFFICE file from Internet

The Source File is :
`````json
{
  "storageDefinition": "URL", 
  "content": "https://github.com/pierre-yves-monnet/camunda-8-connector-officetopdf/raw/a51fc1b29add729087936eb0460b028ba8b5e977/src/test/resources/OfficeToPdfExample.docx"
}
`````

A PDF is created and saved in the TEMP Folder on the runtime (so not very easy to access), 
it depend where you run the LocalConnectorRuntime

Check if you see any errors.
