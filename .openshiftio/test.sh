#!/usr/bin/env bash
set -e

source .openshiftio/openshift.sh

if [ ! -d ".openshiftio" ]; then
  warning "The script expects the .openshiftio directory to exist"
  exit 1
fi

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
