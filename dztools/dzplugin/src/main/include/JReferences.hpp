#ifndef JREFERENCES_HPP
#define JREFERENCES_HPP

#define DO_QUOTE(X) #X
#define QUOTE(X) DO_QUOTE(X)

#include <vector>
#include "dukazorrobridge.hpp"

typedef struct JMethodDesc
{
    jmethodID methodID;
    const char *name;
    const char *signature;
} JMethodDesc;

namespace JData
{

static jobject JDukaZorroBridgeObject;

static jclass JDukaZorroBridgeClass;
static jclass JZorroLoggerClass;
static jclass ExceptionClass;

static JMethodDesc constructor =      { nullptr, "<init>",           "()V" };
static JMethodDesc doLogin =          { nullptr, "doLogin",          "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;)I" };
static JMethodDesc doLogout =         { nullptr, "doLogout",         "()I" };
static JMethodDesc doBrokerTime =     { nullptr, "doBrokerTime",     "([J)I" };
static JMethodDesc doSubscribeAsset = { nullptr, "doSubscribeAsset", "(Ljava/lang/String;)I" };
static JMethodDesc doBrokerAsset =    { nullptr, "doBrokerAsset",    "(Ljava/lang/String;[D)I" };
static JMethodDesc doBrokerAccount =  { nullptr, "doBrokerAccount",  "([D)I" };
static JMethodDesc doBrokerBuy =      { nullptr, "doBrokerBuy",      "(Ljava/lang/String;[D)I" };
static JMethodDesc doBrokerTrade =    { nullptr, "doBrokerTrade",    "(I[D)I" };
static JMethodDesc doBrokerStop =     { nullptr, "doBrokerStop",     "(ID)I" };
static JMethodDesc doBrokerSell =     { nullptr, "doBrokerSell",     "(II)I" };
static JMethodDesc doBrokerHistory =  { nullptr, "doBrokerHistory",  "(Ljava/lang/String;JJII[D)I" };
static JMethodDesc excGetMessage=     { nullptr, "getMessage",       "()Ljava/lang/String;" };
static JMethodDesc excGetName=        { nullptr, "getName",          "()Ljava/lang/String;" };

static const JNINativeMethod nativesTable[2] { { (char*)"jcallback_BrokerError",    (char*)"(Ljava/lang/String;)V", (void *)&jcallback_BrokerError },
                                               { (char*)"jcallback_BrokerProgress", (char*)"(I)V",                  (void *)&jcallback_BrokerProgress } };

static const char* JVMClassPathOption =  "-Djava.class.path=Plugin\\Dukascopy" QUOTE(VERSION) ".jar";
static const char* DukaZorroBridgePath = "com/jforex/dukazorrobridge/DukaZorroBridge";
static const char* ZorroLoggerPath =     "com/jforex/dukazorrobridge/ZorroLogger";
static const char* ExcPath =             "java/lang/Class";

static std::vector<JMethodDesc*> DukaZorroBridgeMethods = { &constructor,
                                                            &doLogin,
                                                            &doLogout,
                                                            &doBrokerTime,
                                                            &doSubscribeAsset,
                                                            &doBrokerAsset,
                                                            &doBrokerAccount,
                                                            &doBrokerBuy,
                                                            &doBrokerTrade,
                                                            &doBrokerStop,
                                                            &doBrokerSell,
                                                            &doBrokerHistory };


static const int nativesTableSize = sizeof(nativesTable) / sizeof(nativesTable[0]);

static const int JNI_VERSION = JNI_VERSION_1_8;

} /* namespace JData */

#endif /* JREFERENCES_HPP */
