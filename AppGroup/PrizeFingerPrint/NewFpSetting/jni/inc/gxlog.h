#ifndef _GOODIX_LOG_H_
#define _GOODIX_LOG_H_

//#define GOODIX_LOG_ON
#ifdef GOODIX_LOG_ON
#include <android/log.h>
#define TAG       "FingerGoodix"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__) // ����LOGD����
#else
//#define LOGD(...)
//for test log if work
#include <android/log.h>
#define TAG       "FingerGoodix"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,TAG,"Not define GOODIX_LOG_ON") // ����LOGD����

#endif

#endif
