[![Build Status](https://travis-ci.com/button/button-merchant-android.svg?token=csLDMWdyHoUrMqv9JzCZ&branch=master)](https://travis-ci.com/button/button-merchant-android)

[![Coverage Status](https://coveralls.io/repos/github/button/button-merchant-android-private/badge.svg?branch=master&t=VbxDcA)](https://coveralls.io/github/button/button-merchant-android-private?branch=master)

# Button Merchant Android
An open source client library for Button merchants.

## Documentation
Documentation for the Merchant Library can be found on the [Button Developer site](https://developer.usebutton.com/guides/merchants/android/open-source-merchant-library).


## Build and run the sample app
```bash
git clone https://github.com/button/button-merchant-android.git
cd button-merchant-android
./gradlew clean installDebug
```

In order to get the sample app to compile and run, you will need to define your Button App ID in your global or local `gradle.properties` file.

```groovy
buttonMerchantAppId="__YOUR_APP_ID__"
```

# Contributing
We are looking forward to accepting your contributions to this project very soon!

Until then, if you have something you would like to contribute, please [get in touch](opensource@usebutton.com).

## Running tests locally
To succesfully run tests that relate to public-key pinning, you will need to add the provided `localhost.keystore` to your Java Runtime's trusted roots (`cacerts`).
Android Studio comes bundled with its own installation of OpenJDK. The following snippet will automatically import the keystore and restart the java process. The keystore password is `localhost`.
### MacOS
```
keytool -importkeystore -destkeystore /Applications/Android\ Studio.app/Contents/jre/jdk/Contents/Home/jre/lib/security/cacerts -storepass changeit -srckeystore button-merchant/src/test/res/raw/localhost.keystore -noprompt
kill -9 $(ps -A | grep java | grep "Android Studio" | grep -v grep | awk '{print $1}')
```
