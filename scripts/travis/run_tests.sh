#!/usr/bin/env bash

set -e
set -o pipefail

echo "running tests"
./gradlew clean
./gradlew check
./gradlew testDebugUnitTest

echo "tests done"