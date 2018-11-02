#!/usr/bin/env bash
set -e

source .openshiftio/openshift.sh

if [ ! -d ".openshiftio" ]; then
  warning "The script expects the .openshiftio directory to exist"
  exit 1
fi

 # cleanup
oc delete build --all
oc delete bc --all
oc delete dc --all
oc delete deploy --all
oc delete is --all
oc delete istag --all
oc delete isimage --all
oc delete job --all
oc delete po --all
oc delete rc --all
oc delete rs --all
oc delete statefulsets --all
oc delete secrets --all
oc delete configmap --all
oc delete services --all
oc delete routes --all
oc delete template --all

# Deploy the templates and required resources
oc apply -f .openshiftio/service.yaml
oc apply -f .openshiftio/application.yaml

# Create the application
oc new-app --template=vertx-crud-booster -p SOURCE_REPOSITORY_URL=https://github.com/openshiftio-vertx-boosters/vertx-crud-booster

# wait for pod to be ready
waitForPodState "my-database" "Running"
waitForPodReadiness "my-database" 1
waitForPodState "crud-vertx" "Running"
waitForPodReadiness "crud-vertx" 1

mvn verify -Popenshift-it -Denv.init.enabled=false
