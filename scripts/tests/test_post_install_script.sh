#!/usr/bin/env bash
#
# test_post_install_script.sh
#
# Copyright (c) 2018 Button, Inc. (https://usebutton.com)
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
#
#

clear
echo "############## (Android) Test Post Install Script ##############"

############## Before Running

# 1 - Assure Device is connected tot the network
# 2 - Assure all web browsers are closed
# 3 - Assure the Google Play store is closed
# 4 - Assure a build of you latest APK is located where the ${appLocation} variable defined

echo 
echo "############## Variables"

# Package Name
package=com.usebutton.merchant.sample
echo "Package Name: ${package}"

# App Location (Relative)
appLocation=sample/build/outputs/apk/debug/sample-debug.apk
echo "APK Location: ${appLocation}"

# bttn.io domain
domain=btnmerchant
echo "Domain: ${domain}"

# Token to be passed as btn_ref
token="src-"
token=${token}$(cat /dev/urandom | env LC_CTYPE=C tr -cd 'a-f0-9' | head -c 8)
echo "Generated Token: ${token}"

# Sleep time to allow device to link into Google Play Store (In Seconds)
# Note: May need to increase depending on Device Speed and Network reliability
sleepTime=5

echo
echo "############## Run"

# Check if the package is installed on the current device
echo "Checking if ${package} exists..."
doesPackageExist=$(adb shell pm list packages ${package}) > /dev/null;

# If the package exists, we want to uninstall it
if [ -n "${doesPackageExist}" ] 
    then
    echo "Uninstalling ${package}..."
    adb uninstall ${package} > /dev/null;
fi

# Start intent. Should launch browser to you bttn.io link
echo "Launching intent to web browser... https://${domain}.bttn.io/test?btn_ref=${token}\&btn_direct=true"
adb shell am start -W -a android.intent.action.VIEW -d "https://${domain}.bttn.io/test?btn_ref=${token}\&btn_direct=true" > /dev/null;

# Sleep to allow device to link into Google Play Store
# Note: May need to increase depending on Device Speed
echo "Sleeping for ${sleepTime} seconds..."
sleep ${sleepTime} > /dev/null;

# Install the button enabled merchant app
echo "Installing App @ ${appLocation}"
./gradlew assembleDebug
adb install ${appLocation} > /dev/null;

# Launching intent that should receive onPostInstallIntent
echo "Launching intent to ${package}"
adb shell monkey -p ${package} -c android.intent.category.LAUNCHER 1 > /dev/null;

echo
echo "############## Verify"

# Sleep to allow logcat to populate
echo "Sleeping for ${sleepTime} seconds..."
sleep ${sleepTime} > /dev/null;

# Check adb if we logged onPostInstallIntent
echo "Checking Log for onPostInstallIntent with btn_ref as ${token}"
isVerified=$(adb logcat -d | grep "btn_ref: ${token}")
echo "${isVerified}"

# If the package exists, we want to uninstall it
if [ -n "${isVerified}" ]
    then
    echo "Verified onPostInstallIntent was successful"
    echo
    exit 0
else
    echo "Error: No onPostInstallIntent call was received..."
    echo
    exit 1
fi
