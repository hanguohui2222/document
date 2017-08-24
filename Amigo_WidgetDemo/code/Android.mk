#
# Copyright (C) 2008 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

ifeq ($(GN_APK_AUTO_APK_TEST), yes)
LOCAL_PROGUARD_ENABLED := disabled
LOCAL_STATIC_JAVA_LIBRARIES := emma
LOCAL_AAPT_FLAGS += --debug-mode
LOCAL_JACK_ENABLED := disabled
endif

LOCAL_STATIC_JAVA_LIBRARIES := \
    AmigoLib\
    android-support-v4\
    android-support-v13

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_PACKAGE_NAME := AmigoDemo

LOCAL_PROGUARD_ENABLED := disabled
LOCAL_CERTIFICATE := platform
LOCAL_JAVA_LIBRARIES += telephony-common

LOCAL_EMMA_COVERAGE_FILTER := *,-emma,-emmarun,-com.vladium.*,-android.support.*

include $(BUILD_PACKAGE)
include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := AmigoLib:./libs/AmigoLib.jar

include $(BUILD_MULTI_PREBUILT)

include $(call all-makefiles-under,$(LOCAL_PATH))
