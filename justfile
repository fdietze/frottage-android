# https://github.com/casey/just

# List available recipes in the order in which they appear in this file
_default:
  @just --list --unsorted

apk:
  gradle assembleDebug

devices:
  adb devices

run:
  gradle assembleDebug
  adb install app/build/outputs/apk/debug/app-debug.apk
  adb shell monkey -p com.frottage -c android.intent.category.LAUNCHER 1
  scripts/logcat com.frottage


