
#!/usr/bin/env bash

set -euo pipefail

# Phase-1 deploy script for hello-war -> Tomcat 10.1
# Usage (from CD):
#   ARTIFACTORY_URL=... ARTIFACTORY_REPO=... ARTIFACTORY_USER=... ARTIFACTORY_TOKEN=... \
#   ARTIFACT_GROUP_PATH=... ARTIFACT_ID=... ARTIFACT_BASE_VERSION=... \
#   sudo /opt/deploy/deploy.sh [VERSION]

TOMCAT_SERVICE_NAME="tomcat"
TOMCAT_WEBAPPS_DIR="/opt/tomcat/webapps"
DEPLOY_WAR_PATH="${TOMCAT_WEBAPPS_DIR}/hello.war"
EXPLODED_DIR="${TOMCAT_WEBAPPS_DIR}/hello"
HEALTH_URL="http://localhost:8080/hello/"

log() {
  echo "[deploy.sh] $(date '+%Y-%m-%d %H:%M:%S') $*"
}

fail() {
  log "ERROR: $*"
  exit 1
}

# REQUIRED ENV VARS (from GitHub Actions)
: "${ARTIFACTORY_URL:?ARTIFACTORY_URL env var is required}"
: "${ARTIFACTORY_REPO:?ARTIFACTORY_REPO env var is required}"
: "${ARTIFACT_GROUP_PATH:?ARTIFACT_GROUP_PATH env var is required}"
: "${ARTIFACT_ID:?ARTIFACT_ID env var is required}"
: "${ARTIFACT_BASE_VERSION:?ARTIFACT_BASE_VERSION env var is required}"
: "${ARTIFACTORY_USER:?ARTIFACTORY_USER env var is required}"
: "${ARTIFACTORY_TOKEN:?ARTIFACTORY_TOKEN env var is required}"

# VERSION = arg or base
VERSION="${1:-${ARTIFACT_BASE_VERSION}}"
log "Deploying version: ${VERSION}"

WAR_NAME="${ARTIFACT_ID}-${VERSION}.war"
SNAPSHOT_URL="${ARTIFACTORY_URL}/${ARTIFACTORY_REPO}/${ARTIFACT_GROUP_PATH}/${ARTIFACT_ID}/${VERSION}/${WAR_NAME}"
TMP_WAR="/tmp/${ARTIFACT_ID}-${VERSION}-$$.war"

log "Artifact URL: ${SNAPSHOT_URL}"
log "Downloading WAR to temporary file: ${TMP_WAR}"

curl -u "${ARTIFACTORY_USER}:${ARTIFACTORY_TOKEN}" \
     -fSL \
     -o "${TMP_WAR}" \
     "${SNAPSHOT_URL}" || fail "Download failed from Artifactory."

if [[ ! -s "${TMP_WAR}" ]]; then
  fail "Downloaded WAR is empty or missing: ${TMP_WAR}"
fi
log "Download complete. Size: $(du -h "${TMP_WAR}" | awk '{print $1}')"

if [[ ! -d "${TOMCAT_WEBAPPS_DIR}" ]]; then
  fail "Tomcat webapps directory does not exist: ${TOMCAT_WEBAPPS_DIR}"
fi

log "Deploying WAR to ${DEPLOY_WAR_PATH}"
cp "${TMP_WAR}" "${DEPLOY_WAR_PATH}" || fail "Failed to copy WAR."
chmod 640 "${DEPLOY_WAR_PATH}"
chown tomcat:tomcat "${DEPLOY_WAR_PATH}" || log "Warning: could not chown tomcat:tomcat"

if [[ -d "${EXPLODED_DIR}" ]]; then
  log "Removing exploded directory: ${EXPLODED_DIR}"
  rm -rf "${EXPLODED_DIR}" || fail "Failed to remove exploded directory."
else
  log "Exploded directory not present (ok): ${EXPLODED_DIR}"
fi

log "Re-Starting Tomcat service: ${TOMCAT_SERVICE_NAME}"
systemctl restart "${TOMCAT_SERVICE_NAME}" || fail "Failed to re-start Tomcat service."

log "Checking application health at: ${HEALTH_URL}"
MAX_ATTEMPTS=20
SLEEP_SECONDS=3
attempt=1

while (( attempt <= MAX_ATTEMPTS )); do
  if curl -fsS "${HEALTH_URL}" > /dev/null 2>&1; then
    log "Health check succeeded on attempt ${attempt}. Deployment OK."
    rm -f "${TMP_WAR}" || true
    exit 0
  fi
  log "Health check attempt ${attempt}/${MAX_ATTEMPTS} failed. Retrying in ${SLEEP_SECONDS}s..."
  sleep "${SLEEP_SECONDS}"
  ((attempt++))
done

log "Health check failed after ${MAX_ATTEMPTS} attempts."
log "Leaving WAR at ${DEPLOY_WAR_PATH}. Check Tomcat logs under /opt/tomcat/logs."
rm -f "${TMP_WAR}" || true
exit 1
