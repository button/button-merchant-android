#!/usr/bin/env bash

set -e
set -o pipefail

echo "coveralls start"

./gradlew coveralls

echo "coveralls done"
