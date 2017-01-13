
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE    :=  tutorial01
LOCAL_SRC_FILES := tutorial01.c
LOCAL_LDLIBS := -llog
LOCAL_SHARED_LIBRARIES := libavformat libavcodec libswscale libavutil libswresample libavfilter
include $(BUILD_SHARED_LIBRARY)
$(call import-module, ffmpeg-3.2/android/arm)