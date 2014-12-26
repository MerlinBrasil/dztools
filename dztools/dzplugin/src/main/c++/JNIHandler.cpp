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
    for(auto *desc : JData::DukaZorroBridgeMethods)
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

jint JNIHandler::callBrokerLogin(JNIEnv* env,
                                 jstring jUser,
                                 jstring jPwd,
                                 jstring jType,
                                 jobjectArray jAccountArray)
{
    return (jint)env->CallObjectMethod(JData::JDukaZorroBridgeObject, JData::doLogin.methodID, jUser, jPwd, jType, jAccountArray);
}

jint JNIHandler::callBrokerLogout(JNIEnv* env)
{
    return (jint)env->CallObjectMethod(JData::JDukaZorroBridgeObject, JData::doLogout.methodID);
}


jint JNIHandler::callBrokerTime(JNIEnv* env,
                                jlongArray utcTimeArray)
{
    return (jint)env->CallObjectMethod(JData::JDukaZorroBridgeObject, JData::doBrokerTime.methodID, utcTimeArray);
}

jint JNIHandler::callSubscribeAsset(JNIEnv* env,
                                    jstring jAsset)
{
    return (jint)env->CallObjectMethod(JData::JDukaZorroBridgeObject, JData::doSubscribeAsset.methodID, jAsset);
}

jint JNIHandler::callBrokerAsset(JNIEnv* env,
                                 jstring jAsset,
                                 jdoubleArray jAssetParamsArray)
{
    return (jint)env->CallObjectMethod(JData::JDukaZorroBridgeObject, JData::doBrokerAsset.methodID, jAsset, jAssetParamsArray);
}

jint JNIHandler::callBrokerHistory(JNIEnv* env,
                                   jstring jAsset,
                                   jlong jtStart,
                                   jlong jtEnd,
                                   jint jnTickMinutes,
                                   jint jnTicks,
                                   jdoubleArray jTicksArray)
{
    return (jint)env->CallObjectMethod(JData::JDukaZorroBridgeObject, JData::doBrokerHistory.methodID, jAsset, jtStart, jtEnd, jnTickMinutes, jnTicks, jTicksArray);
}

jint JNIHandler::callBrokerAccount(JNIEnv* env,
                                   jdoubleArray jAccountParamsArray)
{
    return (jint)env->CallObjectMethod(JData::JDukaZorroBridgeObject, JData::doBrokerAccount.methodID, jAccountParamsArray);
}


jint JNIHandler::callBrokerBuy(JNIEnv* env,
                               jstring jAsset,
                               jdoubleArray jTradeParamsArray)
{
    return (jint)env->CallObjectMethod(JData::JDukaZorroBridgeObject, JData::doBrokerBuy.methodID, jAsset, jTradeParamsArray);
}


jint JNIHandler::callBrokerTrade(JNIEnv* env,
                                 jint jAsset,
                                 jdoubleArray jOrderParamsArray)
{
    return (jint)env->CallObjectMethod(JData::JDukaZorroBridgeObject, JData::doBrokerTrade.methodID, jAsset, jOrderParamsArray);
}

jint JNIHandler::callBrokerStop(JNIEnv* env,
                                jint jnTradeID,
                                jdouble jdStop)
{
    return (jint)env->CallObjectMethod(JData::JDukaZorroBridgeObject, JData::doBrokerStop.methodID, jnTradeID, jdStop);
}

jint JNIHandler::callBrokerSell(JNIEnv* env,
                                jint jnTradeID,
                                jint jnAmount)
{
    return (jint)env->CallObjectMethod(JData::JDukaZorroBridgeObject, JData::doBrokerSell.methodID, jnTradeID, jnAmount);
}
