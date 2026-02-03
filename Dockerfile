# docker build -t pierre-yves-monnet/C8-con-pdf .
FROM camunda/connectors-bundle:8.8.0

# Copy your fat JAR (with dependencies) into the runtime classpath
# Adjust the file name to match your built artifact
COPY target/pdf-*.jar /opt/app/pdf-function.jar
