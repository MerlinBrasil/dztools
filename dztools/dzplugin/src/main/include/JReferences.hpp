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
static JMethodDesc doBrokerTime =     { nullptr, "doBrokerTime",     "([D)I" };
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

static const char* JVMClassPathOption =  "-Djava.class.path=Plugin\\dztools\\dzplugin\\dzplugin-" QUOTE(VERSION) ".jar";
static const char* DukaZorroBridgePath = "com/jforex/dzplugin/DukaZorroBridge";
static const char* ZorroLoggerPath =     "com/jforex/dzplugin/ZorroLogger";
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
