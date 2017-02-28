# Another readme


## Login

On minishift / local

```
oc login -u developer -p developer 
```

On Openshift Online

```
oc login URL -token ....
```

## Deploy posgtres


```
oc new-app openshift/postgresql-92-centos7 \
 -e POSTGRESQL_USER=vertx \
 -e POSTGRESQL_PASSWORD=secret \
 -e POSTGRESQL_DATABASE=my_data \
 -l name=my-database  
```

## Create secrets

```
oc create -f credentials-secrets.yml
```