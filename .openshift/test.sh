#!/usr/bin/env bash
set -e

if [ ! -d ".openshift" ]; then
  warning "The script expects the .openshift directory to exist"
  exit 1
fi

source .openshift/openshift.sh

if [ -z "$1" ]; then
  ORG="openshift-vertx-examples"
else
  ORG=$1
fi

REPO="https://github.com/$ORG/vertx-crud-example"
echo -e "\n${YELLOW}Using source repository: $REPO ...\n${NC}"

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
oc apply -f .openshift/service.yaml
oc apply -f .openshift/application.yaml

# Create the application
oc new-app --template=vertx-crud-example -p SOURCE_REPOSITORY_URL="$REPO"

# wait for pod to be ready
waitForPodState "my-database" "Running"
waitForPodReadiness "my-database" 1
waitForPodState "crud-vertx" "Running"
waitForPodReadiness "crud-vertx" 1

mvn verify -Popenshift-it -Denv.init.enabled=false
