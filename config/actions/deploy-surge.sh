#!/bin/bash
# Split on "/", ref: http://stackoverflow.com/a/5257398/689223
REPO_SLUG_ARRAY=(${GITHUB_REPOSITORY//\// })
REPO_OWNER=${REPO_SLUG_ARRAY[0]}
REPO_NAME=${REPO_SLUG_ARRAY[1]}
DEPLOY_PATH=./reposense-report
DEPLOY_SUBDOMAIN="${GITHUB_SHA:0:6}-sha" # get first 6 characters of commit sha

# debugging purposes
echo "Owner: ${REPO_OWNER}"
echo "Repo: ${REPO_NAME}"
echo "Event type: ${GITHUB_EVENT_NAME}"
echo "Head ref: ${GITHUB_HEAD_REF}"
echo ${GITHUB_PR}

# replaces "/" or "." with "-"
# sed -r is only supported in linux, ref http://stackoverflow.com/a/2871217/689223
# Domain names follow the RFC1123 spec [a-Z] [0-9] [-]
# The length is limited to 253 characters
# https://en.wikipedia.org/wiki/Domain_Name_System#Domain_name_syntax
DEPLOY_DOMAIN=https://${DEPLOY_SUBDOMAIN}-${REPO_NAME}-${REPO_OWNER}.surge.sh
echo "Deploy domain: ${DEPLOY_DOMAIN}"
surge --project ${DEPLOY_PATH} --domain $DEPLOY_DOMAIN;
if [ "$GITHUB_EVENT_NAME" == "pull_request" ]
then
#  echo "https://api.github.com/repos/${REPO_OWNER}/${REPO_NAME}/statuses/${GITHUB_SHA}?access_token=${GITHUB_API_TOKEN}"
  curl "https://api.github.com/repos/${REPO_OWNER}/${REPO_NAME}/statuses/${GITHUB_PR_SHA}?access_token=${GITHUB_API_TOKEN}" \
  -H "Content-Type: application/json" \
  -X POST \
  -d "{\"state\": \"success\",\"context\": \"continuous-integration/travis\", \"description\": \"Deploy domain: ${DEPLOY_DOMAIN}\", \"target_url\": \"${DEPLOY_DOMAIN}\"}"
fi
