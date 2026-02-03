# Kubernetes image

# Deploy

The connector can be used a simple pod

```shell
kubectl create -f c8-con-pdf.yaml
```

# Connect to a different cluster

The simple way to connect to a different cluster is to set variables in the pod, as an environment variables.
To change the gatewayUrl, use in the YAML file

> Image currently use 8.7.x API. The configuration change between 8.7 and 8.8
 

```yaml
          env:
            - name: JAVA_TOOL_OPTIONS
              value: >-
                -Dcamunda.client.zeebe.gatewayUrl=http://myserver:26500

```
The default configuration connect to a local self-manage cluster, assuming the connector is deployed in the same cluster.

Visit
https://docs.camunda.io/docs/8.7/apis-tools/spring-zeebe-sdk/configuration/#modes



To deploy using a basic authentication, add

```yaml
          env:
            - name: JAVA_TOOL_OPTIONS
              value: >-
                -Dcamunda.client.auth.method=basic
                -Dcamunda.client.auth.username=<your username>
                -Dcamunda.client.auth.password=<your password>
```


To access all parameters: https://docs.camunda.io/docs/apis-tools/camunda-spring-boot-starter/properties-reference/

# Build the image

(Do not forget to update banner.txt with the current version number)

## Rebuilt the image via

```
mvn clean install
```

## Check the connector runtime version 

The version is referenced in `DockerFile`

## Push the docker image

The docker image is build using the Dockerfile present on the root level.

Push the image to
```
docker build -t pierre-yves-monnet/c8-con-pdf:3.1.0 .
```

## Push in Camunda hub

Push the image to the Camunda hub (you must be login first to the docker registry)

```
docker tag pierre-yves-monnet/c8-con-pdf:3.1.0 ghcr.io/camunda-community-hub/c8-con-pdf:3.1.0
docker push ghcr.io/camunda-community-hub/c8-con-pdf:3.1.0
```
Tag as the latest:

```
docker tag pierre-yves-monnet/c8-con-pdf:3.1.0 ghcr.io/camunda-community-hub/c8-con-pdf:latest
docker push ghcr.io/camunda-community-hub/c8-con-pdf:latest
```

Check on https://github.com/camunda-community-hub/package/c8-con-pdf

