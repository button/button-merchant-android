#!/usr/bin/env bash

set -e
set -o pipefail

echo "starting deploy to bintray"
echo "PR " + ${TRAVIS_PULL_REQUEST} + " BRANCH" + ${TRAVIS_BRANCH}

if [ "${TRAVIS_PULL_REQUEST}" = "false" ] && [ "${TRAVIS_BRANCH}" = "master" ]; then
    ./gradlew build bintrayUpload
    echo "deploying to bintray"
fi

echo "finished deploying to bintray"