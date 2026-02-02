# Kubernetes image

# Deploy
The connector can be used a simple pod


# Build the image

(Do not forget to update banner.txt with the current version number)

Rebuilt the image via

```
mvn clean install
```
Push the docker image
The docker image is build using the Dockerfile present on the root level.

Push the image to
```
docker build -t pierre-yves-monnet/c8-con-pdf:3.1.0 .
```
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

Check on https://github.com/camunda-community-hub/process-execution-automator/pkgs/container/process-execution-automator

