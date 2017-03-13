node("maven") {
  checkout scm
  stage("Prepare") {
    sh "oc new-app openshift/postgresql-92-centos7 -e POSTGRESQL_USER=vertx -e POSTGRESQL_PASSWORD=secret  -e POSTGRESQL_DATABASE=my_data  --name=my-database"
    sh "oc create -f credentials-secret.yml"
  }
  stage("Build") {
    sh "mvn fabric8:deploy -Popenshift -DskipTests"
  }
  stage("Deploy")
}
