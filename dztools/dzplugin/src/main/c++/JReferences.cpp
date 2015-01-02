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

#define DO_QUOTE(X) #X
#define QUOTE(X) DO_QUOTE(X)

#include "JReferences.hpp"

namespace JData
{

jobject JDukaZorroBridgeObject;

jclass JDukaZorroBridgeClass;
jclass JZorroLoggerClass;
jclass ExceptionClass;

JMethodDesc constructor =      { nullptr, "<init>",           "()V" };
JMethodDesc doLogin =          { nullptr, "doLogin",          "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;)I" };
JMethodDesc doLogout =         { nullptr, "doLogout",         "()I" };
JMethodDesc doBrokerTime =     { nullptr, "doBrokerTime",     "([D)I" };
JMethodDesc doSubscribeAsset = { nullptr, "doSubscribeAsset", "(Ljava/lang/String;)I" };
JMethodDesc doBrokerAsset =    { nullptr, "doBrokerAsset",    "(Ljava/lang/String;[D)I" };
JMethodDesc doBrokerAccount =  { nullptr, "doBrokerAccount",  "([D)I" };
JMethodDesc doBrokerBuy =      { nullptr, "doBrokerBuy",      "(Ljava/lang/String;[D)I" };
JMethodDesc doBrokerTrade =    { nullptr, "doBrokerTrade",    "(I[D)I" };
JMethodDesc doBrokerStop =     { nullptr, "doBrokerStop",     "(ID)I" };
JMethodDesc doBrokerSell =     { nullptr, "doBrokerSell",     "(II)I" };
JMethodDesc doBrokerHistory =  { nullptr, "doBrokerHistory",  "(Ljava/lang/String;DDII[D)I" };
JMethodDesc doDLLlog      =    { nullptr, "doDLLlog",         "(Ljava/lang/String;)V" };
JMethodDesc doHistoryDownload= { nullptr, "doHistoryDownload","()I" };
JMethodDesc excGetMessage=     { nullptr, "getMessage",       "()Ljava/lang/String;" };
JMethodDesc excGetName=        { nullptr, "getName",          "()Ljava/lang/String;" };

const JNINativeMethod nativesTable[2] { { (char*)"jcallback_BrokerError",    (char*)"(Ljava/lang/String;)V", (void *)&jcallback_BrokerError },
                                        { (char*)"jcallback_BrokerProgress", (char*)"(I)V",                  (void *)&jcallback_BrokerProgress } };

const char* JVMClassPathOption =  "-Djava.class.path=Plugin\\dukascopy\\dzplugin-" QUOTE(VERSION) ".jar";
const char* DukaZorroBridgePath = "com/jforex/dzplugin/DukaZorroBridge";
const char* ZorroLoggerPath =     "com/jforex/dzplugin/ZorroLogger";
const char* ExcPath =             "java/lang/Class";

const std::vector<JMethodDesc*> dukaZorroBridgeMethods = { &constructor,
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
                                                           &doBrokerHistory,
                                                           &doHistoryDownload,
                                                           &doDLLlog};


 const int nativesTableSize = sizeof(nativesTable) / sizeof(nativesTable[0]);

 const int JNI_VERSION = JNI_VERSION_1_8;

} /* namespace JData */

