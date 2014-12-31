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

#include <assert.h>
#include "JNIHandler.hpp"
#include "JReferences.hpp"

void JNIHandler::init()
{
    initializeJVM();
    initializeJavaReferences();
}

void JNIHandler::initializeJVM()
{
    if(isJVMLoaded) return;

    JavaVMInitArgs args;
    JavaVMOption options[1];

    args.version = JData::JNI_VERSION;
    args.nOptions = 1;
    args.options = options;
    options[0].optionString = (char*)JData::JVMClassPathOption;
    args.ignoreUnrecognized = JNI_FALSE;

    jint res = JNI_CreateJavaVM(&jvm, (void **)&env, &args);
    assert(res ==  JNI_OK);
    isJVMLoaded = true;
}

JNIEnv* JNIHandler::getJNIEnvironment(){
    return env;
}

JNIEnv* JNIHandler::getEnvForCurrentThread()
{
    int envStat = jvm->GetEnv((void **)&env, JData::JNI_VERSION);
    if (envStat == JNI_EDETACHED) {
        jint res = jvm->AttachCurrentThread((void**)&env, NULL);
        assert (res == JNI_OK);
    }
    return env;
}

void JNIHandler::initializeJavaReferences()
{
    if(areJavaReferencesInitialized) return;

    initExceptionHandling();
    initBridgeObject();
    registerNatives();

    areJavaReferencesInitialized = true;
}

void JNIHandler::initBridgeObject()
{
    JData::JDukaZorroBridgeClass = env->FindClass(JData::DukaZorroBridgePath);
    registerClassMethods();
    JData::JDukaZorroBridgeObject = env->NewObject(JData::JDukaZorroBridgeClass, JData::constructor.methodID);
    checkJNIExcpetion(env);
}

void JNIHandler::initExceptionHandling()
{
    JData::ExceptionClass = env->FindClass(JData::ExcPath);
    JData::excGetName.methodID = env->GetMethodID(JData::ExceptionClass, JData::excGetName.name, JData::excGetName.signature);
}

void JNIHandler::registerNatives()
{
    JData::JZorroLoggerClass = env->FindClass(JData::ZorroLoggerPath);
    env->RegisterNatives(JData::JZorroLoggerClass, JData::nativesTable, JData::nativesTableSize);
    checkJNIExcpetion(env);
}

void JNIHandler::registerClassMethods()
{
    for(auto *desc : JData::dukaZorroBridgeMethods)
    {
        desc->methodID = env->GetMethodID(JData::JDukaZorroBridgeClass, desc->name, desc->signature);
        checkJNIExcpetion(env);
    }
}

void JNIHandler::checkJNIExcpetion(JNIEnv* env)
{
    jthrowable exc = env->ExceptionOccurred();
    if (!exc) return;

    jclass exccls(env->GetObjectClass(exc));
    jstring name = static_cast<jstring>(env->CallObjectMethod(exccls, JData::excGetName.methodID));
    char const* utfName(env->GetStringUTFChars(name, 0));

    JData::excGetMessage.methodID = env->GetMethodID(exccls, JData::excGetMessage.name, JData::excGetMessage.signature);
    jstring message = static_cast<jstring>(env->CallObjectMethod(exc, JData::excGetMessage.methodID));
    char const* utfMessage(env->GetStringUTFChars(message, 0));

    BrokerError(utfName);
    BrokerError(utfMessage);

    env->ReleaseStringUTFChars(message, utfMessage);
    env->ReleaseStringUTFChars(name, utfName);
    env->ExceptionClear();
}
