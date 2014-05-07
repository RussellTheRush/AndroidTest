#! /bin/bash

mm

P=$ANDROID_PRODUCT_OUT
F=$P/data/app/PPListViewDemo.apk

adb install -r $F

adb shell logcat -c
adb shell logcat -s RRR
