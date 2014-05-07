LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_PACKAGE_NAME := PPListViewDemo
LOCAL_MODULE_TAGS := tests
LOCAL_JAVA_LIBRARIES += android-support-v4
include $(BUILD_PACKAGE)
