#ifndef DUKAZORROBRIDGE_HPP
#define DUKAZORROBRIDGE_HPP

#include <windows.h>
#include <jni.h>

#include "functions.h"
#include "trading.h"

typedef double DATE;
typedef jlong DukaTime;

static const int PLUGIN_VERSION = 2u;
extern int (__cdecl *BrokerError)(const char *txt);
extern int (__cdecl *BrokerProgress)(const int percent);

extern void jcallback_BrokerError(JNIEnv *env,
                                  jclass clazz,
                                  jstring msg);
extern void jcallback_BrokerProgress(JNIEnv *env,
                                     jclass clazz,
                                     jint progress);

#endif /* DUKAZORROBRIDGE_HPP */

