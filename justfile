# https://github.com/casey/just

# List available recipes in the order in which they appear in this file
_default:
  @just --list --unsorted

test:
  gradle testDebug

test-watch:
  rg --files build.gradle.kts app | entr -crn gradle testDebug

dev:
  rg --files build.gradle.kts app | entr -crn just install

fix:
  ktlint --format

# run ci checks locally
ci:
  (git ls-files && git ls-files --others --exclude-standard) | entr -cnr earthly +ci-test

apk:
  ./gradlew assembleDebug

devices:
  adb devices

install: apk
  adb devices
  adb install app/build/outputs/apk/debug/frottage-debug.apk || (adb uninstall com.frottage && adb install app/build/outputs/apk/debug/frottage-debug.apk)
  adb shell monkey -p com.frottage -c android.intent.category.LAUNCHER 1

run: install
  scripts/logcat com.frottage

deploy:
  mkdir -p keys
  echo "$KEYSTORE_BASE64" | base64 --decode > keys/keystore.jks
  echo "$PLAY_SERVICE_ACCOUNT_JSON" > keys/play-service-account.json
  fastlane playstore
  rm -rf keys
