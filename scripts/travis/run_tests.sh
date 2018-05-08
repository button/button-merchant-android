#!/usr/bin/env bash

set -e
set -o pipefail

echo "running tests"
./gradlew clean
./gradlew checkstyle
./gradlew check
./gradlew testDebugUnitTest

echo "tests done"
