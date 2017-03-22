# Introduction

This project exposes a CRUD HTTP endpoint and a UI to use this service. The CRUD endpoint is implemented using a Vert
.x application and JDBC.

The application lets you:

* retrieve a list of fruits
* create new fruits
* edit existing fruits
* delete fruits

The CRUD endpoint follow the HTTP recommendations in term of HTTP verb and status.

# Prerequisites

To get started with this quickstart you'll need the following prerequisites:

Name | Description | Version
--- | --- | ---
[java][1] | Java JDK | 8
[maven][2] | Apache Maven | 3.3.x 
[oc][3] | OpenShift Client | v1.4.x
[git][4] | Git version management | 2.x 

[1]: http://www.oracle.com/technetwork/java/javase/downloads/
[2]: https://maven.apache.org/download.cgi?Preferred=ftp://mirror.reverse.net/pub/apache/
[3]: https://docs.openshift.com/enterprise/3.2/cli_reference/get_started_cli.html
[4]: https://git-scm.com/book/en/v2/Getting-Started-Installing-Git

In order to build and deploy this project on OpenShift, you need either:

* a local OpenShift instance such as Minishift,
* account on an OpenShift Online (OSO) instance, such as https://console.dev-preview-int.openshift.com/ instance.

# Deployment instructions

To build and deploy this quickstart you can:

1. deploy it to OpenShift using Apache Maven,
2. deploy it to OpenShift using a pipeline.
 
**If you are using Minishift**

1. Login to your OpenShift instance using:

```bash
oc login https://192.168.64.12:8443 -u developer -p developer
```

2. Open your browser to https://192.168.64.12:8443/console/, and log in using _developer/developer_.

3. Check that you have a project. If `oc project` returns an error, create a project with:

```bash
oc new-project myproject
```

**If your are using OpenShift Online**
  
1. Go to [OpenShift Online](https://console.dev-preview-int.openshift.com/console/command-line) to get the token used 
by the `oc` client for authentication and project access.
2. On the oc client, execute the following command to replace $MYTOKEN with the one from the Web Console:
     
```bash
oc login https://api.dev-preview-int.openshift.com --token=$MYTOKEN
```

3. Check that you have a project. If `oc project` returns an error, create a project with:
   
```bash
oc new-project myproject
```

## Deploy the application to OpenShift using Maven

To deploy the application using Maven, launch:

```bash
oc new-app \
 -p POSTGRESQL_USER=luke \
 -p POSTGRESQL_PASSWORD=secret \
 -p POSTGRESQL_DATABASE=my_data \
 -p DATABASE_SERVICE_NAME=my-database \
 --name=my-database \
 --template=postgresql-ephemeral 
mvn fabric8:deploy -Popenshift
```

The first command creates a PostgreSQL database. It's an ephemeral database, so the data is lost if you stop the 
database. The second command builds and deploys the application to the OpenShift instance on which you are logged in.
 The database credentials are store in an OpenShift secret deployed during the application deployment.

Once deployed, you can access the application using the _application URL_. Retrieve it using:

```bash
$ oc get route vertx-crud -o jsonpath={$.spec.host}
vertx-http-myproject.192.168.64.12.nip.io                                                                                                                              
```

Then, open your browser to the displayed url: http://vertx-crud-myproject.192.168.64.12.nip.io.                                                                         

Alternatively, you can invoke the _greeting_ service directly using curl or httpie:
    
```bash
# Get the fruits list
http http://$URL/api/fruits
# Add fruits
http POST http://$URL/api/fruits name=apple
# Edit fruits
http PUT http://$URL/api/fruits/1 name=pineapple
# Delete fruits
http DELETE http://$URL/api/fruits/1
```

If you get a `503` response, it means that the application is not ready yet.

## Deploy the application to OpenShift using a pipeline

When deployed with a _pipeline_ the application is built from the sources (from a git repository) by a continuous 
integration server (Jenkins) running in OpenShift.

To trigger this built:

1. Apply the OpenShift template:

```bash
oc new-app -f src/openshift/openshift-pipeline-template.yml
```

2. Trigger the pipeline build:

```bash
oc start-build vertx-http
```

With the sequence of command, you have deployed a Jenkins instance in your OpenShift project, define the build 
pipeline of the application and trigger a first build of the application.

Once the build is complete, you can access the application using the _application URL_. Retrieve this url using:

```bash
oc get route vertx-http -o jsonpath={$.spec.host}
```

Then, open your browser to the displayed url. For instance, http://vertx-http-myproject.192.168.64.12.nip.io.           
                                                              
Alternatively, you can invoke the _greeting_ service directly using curl or httpie:
    
```bash
curl http://vertx-http-myproject.192.168.64.12.nip.io/api/greeting
curl http://vertx-http-myproject.192.168.64.12.nip.io/api/greeting?name=Bruno
http http://vertx-http-myproject.192.168.64.12.nip.io/api/greeting
http http://vertx-http-myproject.192.168.64.12.nip.io/api/greeting name==Charles
```

If you get a `503` response, it means that the application is not ready yet.


# Running integration tests

The quickstart also contains a set of integration tests. You need to be connected to an OpenShift instance (Openshift 
Online or Minishift) to run them. You also need to select an existing project.

Then, run integration tests using:

```
mvn clean verify -Popenshift -Popenshift-it
```





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
