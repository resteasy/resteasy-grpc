#!/bin/bash

fail() {
    printf "%s\n\n" "${1}"
    printHelp
    exit 1
}

printArgHelp() {
    if [ -z "${1}" ]; then
        printf "    %-20s%s\n" "${2}" "${3}"
    else
        printf "%s, %-20s%s\n" "${1}" "${2}" "${3}"
    fi
}

printHelp() {
    echo "Performs a release of the project. Two arguments are required, the releaseVersion and the development."
    printArgHelp "-d" "--development" "The next version for the development cycle."
    printArgHelp "-f" "--force" "Forces to allow a SNAPSHOT suffix in release version and not require one for the development version."
    printArgHelp "-r" "--release" "The expected exit status. Defaults to 0."
    printArgHelp "" "--dry-run" "Executes the release in as a dry-run. Nothing will be updated or pushed."
    echo "Usage: ${0##*/} --release 1.0.0 --development 1.0.1"
}

DRY_RUN=false
FORCE=false
DEVEL_VERSION=""
RELEASE_VERSION=""
LOCAL_REPO="/tmp/resteasy-grpc/m2/repository"

while [ "$#" -gt 0 ]
do
    case "${1}" in
        -d|--development)
            DEVEL_VERSION="${2}"
            shift
            ;;
        --dry-run)
            DRY_RUN=true
            ;;
        -f|--force)
            FORCE=true;
            ;;
        -r|--release)
            RELEASE_VERSION="${2}"
            shift
            ;;
        *)
            fail "Invalid argument ${1}"
            ;;
    esac
    shift
done

if [ -z "${DEVEL_VERSION}" ]; then
    fail "The development version is required."
fi

if [ -z "${RELEASE_VERSION}" ]; then
    fail "The release version is required."
fi

if [ ${FORCE} == false ]; then
    if  echo "${RELEASE_VERSION}" | grep -q "SNAPSHOT" ; then
        fail "The release version appears to be a SNAPSHOT (${RELEASE_VERSION}). This is likely no valid and -f should be used if it is."
    fi
    if  echo "${DEVEL_VERSION}" | grep -q -v "SNAPSHOT" ; then
        fail "The development version does not appear to be a SNAPSHOT (${DEVEL_VERSION}). This is likely no valid and -f should be used if it is."
    fi
fi

printf "Performing release for version %s with the next version of %s\n" "${RELEASE_VERSION}" "${DEVEL_VERSION}"

MAVEN_ARGS=()
TAG_NAME="v${RELEASE_VERSION}"

if ${DRY_RUN}; then
    echo "This will be a dry run and nothing will be updated or pushed."
    MAVEN_ARGS=("-DdryRun")
else
    MAVEN_ARGS=("-DpushChanges=true")
fi

if [ -d "${LOCAL_REPO}" ]; then
    rm -rf "${LOCAL_REPO}"
fi

mvn clean release:clean release:prepare release:perform -Dmaven.local.repo="${LOCAL_REPO}" -DdevelopmentVersion="${DEVEL_VERSION}" -DreleaseVersion="${RELEASE_VERSION}" -Dtag="${TAG_NAME}" "${MAVEN_ARGS[@]}"


