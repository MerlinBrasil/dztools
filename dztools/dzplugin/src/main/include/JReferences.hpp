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

extern jobject JDukaZorroBridgeObject;

extern jclass JDukaZorroBridgeClass;
extern jclass JZorroLoggerClass;
extern jclass ExceptionClass;

extern JMethodDesc constructor;
extern JMethodDesc doLogin;
extern JMethodDesc doLogout;
extern JMethodDesc doBrokerTime;
extern JMethodDesc doSubscribeAsset;
extern JMethodDesc doBrokerAsset;
extern JMethodDesc doBrokerAccount;
extern JMethodDesc doBrokerBuy;
extern JMethodDesc doBrokerTrade;
extern JMethodDesc doBrokerStop;
extern JMethodDesc doBrokerSell;
extern JMethodDesc doBrokerHistory;
extern JMethodDesc doDLLlog;
extern JMethodDesc excGetMessage;
extern JMethodDesc excGetName;

extern const JNINativeMethod nativesTable[2];
extern const int nativesTableSize;

extern const char* JVMClassPathOption;
extern const char* DukaZorroBridgePath;
extern const char* ZorroLoggerPath;
extern const char* ExcPath;

extern const std::vector<JMethodDesc*> dukaZorroBridgeMethods;

extern const int JNI_VERSION;

} /* namespace JData */

#endif /* JREFERENCES_HPP */
