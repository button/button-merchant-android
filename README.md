[![Build Status](https://travis-ci.com/button/button-merchant-android.svg?token=csLDMWdyHoUrMqv9JzCZ&branch=master)](https://travis-ci.com/button/button-merchant-android)

# Button Merchant Android
An open source client library for Button merchants.

**Note:** The Button Merchant library is shared only with priveliged Merchants. Aspects of the documentation, and this repo are incomplete, pending public release. Please see the docs below for usage.

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
