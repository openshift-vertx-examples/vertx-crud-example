# Introduction

This project exposes a simple REST to JDBC endpoint where the products and stocks can be managed.

1. Build and launch using vert.x or the vert.x maven plug-in.
1. Build, deploy, and authenticate using OpenShift Online.

## Prerequisites

To get started with these quickstarts you'll need the following prerequisites:

Name | Description | Version
--- | --- | ---
[java][1] | Java JDK | 8
[maven][2] | Apache Maven | 3.3.x 
[oc][3] | OpenShift Client | v3.4.x
[git][4] | Git version management | 2.x 

[1]: http://www.oracle.com/technetwork/java/javase/downloads/
[2]: https://maven.apache.org/download.cgi?Preferred=ftp://mirror.reverse.net/pub/apache/
[3]: https://docs.openshift.com/enterprise/3.2/cli_reference/get_started_cli.html
[4]: https://git-scm.com/book/en/v2/Getting-Started-Installing-Git

In order to build and deploy this project, you must have an account on an OpenShift Online (OSO): https://console.dev-preview-int.openshift.com/ instance.

## Build the Project

1. Execute the following apache maven command:

```bash
mvn clean package
```

## Launch and Test on your local machine

To test the application locally, you need a PosgreSQL database running on _localhost_ with:

* a database called _my_data_
* credentials: `postgres` / `postgres`
  

1. Execute the following apache maven command:

```
java -jar target/${artifactId}-${version}.jar
```

1. Execute the following HTTP Get requests to get a response from the Rest endpoint:

```
# list all products
curl http http://localhost:8080/products
# list single product
curl http http://localhost:8080/products/1
# create new product
curl -H "Content-Type: application/json" -X POST -d '{"name":"Pineapple","stock":"1"}' http://localhost:8080/products
# update a product
curl -H "Content-Type: application/json" -X PUT -d '{"name":"Apple","stock":"100"}' http://localhost:8080/products/1
# delete a product
curl -H "Content-Type: application/json" -X DELETE http://localhost:8080/products/1
```

## Launch using Vert.x maven plugin

1. Execute the following command:

```bash
mvn compile vertx:run
```

## Deploy and run on OpenShift

Login to your OpenShift instance. For Minishift use:

```
oc login https://192.168.64.12:8443 -u developer -p developer
```

If you want to use a specific project for this quickstart, create one with:

```
export NAME=PUT_YOUR_PROJECT_NAME
oc new-project ${NAME}
oc project ${NAME}
USERNAME=$(oc whoami)
oc policy add-role-to-user admin ${USERNAME} -n ${NAME}
oc policy add-role-to-user view -n ${NAME} -z default
```

Deploying the application requires:

1. a postgres database
2. a _secret_ containing the credential
3. the application

Create the postgres database with:

```
oc new-app openshift/postgresql-92-centos7 \
 -e POSTGRESQL_USER=vertx \
 -e POSTGRESQL_PASSWORD=secret \
 -e POSTGRESQL_DATABASE=my_data \
 --name=my-database  
```

It creates a database service named `my-database`.

Then, create the _Secret_ object containing the credentials. The application access this secret to retrieve the login and password:

```
oc create -f credentials-secret.yml
```

Finally, deploy the application with:

```
mvn fabric8:deploy -Popenshift
```

# Integration tests

You can run the integration tests using:

```
mvn clean verify -Popenshift -Popenshift-it
```
