/*
 * #%L
 * dzplugin
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 juxeii
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

#ifndef JNIHANDLER_HPP
#define JNIHANDLER_HPP

#include "dukazorrobridge.hpp"

class JNIHandler
{
public:
    void init();

    JNIEnv* getJNIEnvironment();

    JNIEnv* getEnvForCurrentThread();

    void checkJNIExcpetion(JNIEnv* env);

    jint callBrokerLogin(JNIEnv* env,
                         jstring jUser,
                         jstring jPwd,
                         jstring jType,
                         jobjectArray jAccountArray);

    jint callBrokerLogout(JNIEnv* env);

    jint callBrokerTime(JNIEnv* env,
                        jlongArray utcTimeArray);

    jint callSubscribeAsset(JNIEnv* env,
                            jstring jAsset);

    jint callBrokerAsset(JNIEnv* env,
                         jstring jAsset,
                         jdoubleArray jAssetParamsArray);

    jint callBrokerHistory(JNIEnv* env,
                           jstring jAsset,
                           jlong jtStart,
                           jlong jtEnd,
                           jint jnTickMinutes,
                           jint jnTicks,
                           jdoubleArray jTicksArray);

    jint callBrokerAccount(JNIEnv* env,
                           jdoubleArray jAccountParamsArray);

    jint callBrokerBuy(JNIEnv* env,
                      jstring jAsset,
                      jdoubleArray jTradeParamsArray);

    jint callBrokerTrade(JNIEnv* env,
                         jint jAsset,
                         jdoubleArray jOrderParamsArray);

    jint callBrokerStop(JNIEnv* env,
                        jint jnTradeID,
                        jdouble jdStop);

    jint callBrokerSell(JNIEnv* env,
                        jint jnTradeID,
                        jint jnAmount);

    JavaVM* getJVM() { return jvm; }

private:
    void initializeJVM();

    void initializeJavaReferences();

    void initBridgeObject();

    void initExceptionHandling();

    void registerNatives();

    void registerClassMethods();

    JavaVM *jvm = nullptr;
    JNIEnv * env = nullptr;

    bool isJVMLoaded = false;
    bool areJavaReferencesInitialized = false;
};

#endif /* JNIHANDLER_HPP */
