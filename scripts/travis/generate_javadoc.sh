#!/usr/bin/env bash
#
# This script builds the merchant library javadoc and copies the results to the docs folder. It
# should be run from the root of the project.
#
# Usage:
#     generate_javadoc.sh <release version>
#
# Example:
#     ./scripts/travis/generate_javadoc.sh 1.0.0

set -e
set -o pipefail

function usage() {
    echo "Usage: ${0} <release version>"
}

if [ -z "$1" ]; then
    usage
    exit 1
fi

echo "generating html docs version $1"
./gradlew clean :button-merchant:androidHtmlDocs
cp -r button-merchant/build/docs/html docs/history/$1
rm -f docs/latest
ln -s history/$1 docs/latest

echo "html docs done"
