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

#include "DllCallHandler.hpp"
#include <cstring>
#include "DateUtils.hpp"
#include "JNIHandler.hpp"

int DllCallHandler::BrokerLogin(const char *User,
                                const char *Pwd,
                                const char *Type,
                                char *Account)
{
    jniHandler.init();

    env = jniHandler.getJNIEnvironment();
    jstring jUser;
    jstring jPwd;
    jstring jType;
    if(User) jUser = env->NewStringUTF(User);
    else jUser = env->NewStringUTF("");

    if(Pwd) jPwd = env->NewStringUTF(Pwd);
    else jPwd = env->NewStringUTF("");

    if(Type) jType = env->NewStringUTF(Type);
    else jType = env->NewStringUTF("");

    jobjectArray jAccountArray = (jobjectArray)env->NewObjectArray(1, env->FindClass("java/lang/String"), env->NewStringUTF(""));
    jint res = jniHandler.callBrokerLogin(env, jUser, jPwd, jType, jAccountArray);

    if(Account)
    {
        jstring jAccount = (jstring)env->GetObjectArrayElement(jAccountArray, 0);
        char* accountID = const_cast<char*>(env->GetStringUTFChars(jAccount, NULL));
        std::size_t nPos = strlen(accountID);
        if(nPos>1023){
            BrokerError("Account number too big -> truncated");
            nPos = 1023;
        }
        std::strncpy(Account, accountID, nPos);
        Account[nPos] = 0;
        env->DeleteLocalRef(jAccount);
    }

    env->DeleteLocalRef(jUser);
    env->DeleteLocalRef(jPwd);
    env->DeleteLocalRef(jType);
    env->DeleteLocalRef(jAccountArray);

    return res;
}

int DllCallHandler::BrokerLogout()
{
    return jniHandler.callBrokerLogout(env);
}

int DllCallHandler::BrokerTime(DATE *pTimeUTC)
{
    jlongArray utcTimeArray = env->NewLongArray(1);
    jint res = jniHandler.callBrokerTime(env, utcTimeArray);

    if(pTimeUTC)
    {
        jlong *utcTime = env->GetLongArrayElements(utcTimeArray, 0);
        *pTimeUTC = DateUtils::fromDukaTime(utcTime[0]);
        env->ReleaseLongArrayElements(utcTimeArray, utcTime, 0);
    }
    env->DeleteLocalRef((jobject)utcTimeArray);

    return res;
}

int DllCallHandler::SubscribeAsset(const char* Asset)
{
    jstring jAsset = env->NewStringUTF(Asset);

    jint res = jniHandler.callSubscribeAsset(env, jAsset);
    env->DeleteLocalRef(jAsset);

    return res;
}

int DllCallHandler::BrokerAsset(const char *Asset,
                                double *pPrice,
                                double *pSpread,
                                double *pVolume,
                                double *pPip,
                                double *pPipCost,
                                double *pMinAmount,
                                double *pMargin,
                                double *pRollLong,
                                double *pRollShort)
{
    jstring jAsset = env->NewStringUTF(Asset);
    jdoubleArray jAssetParamsArray = env->NewDoubleArray(9);

    jint res = jniHandler.callBrokerAsset(env, jAsset, jAssetParamsArray);
    jdouble *assetParams = env->GetDoubleArrayElements(jAssetParamsArray, 0);

    if(pPrice) *pPrice = assetParams[0];
    if(pSpread) *pSpread = assetParams[1];
    if(pVolume) *pVolume = assetParams[2];
    if(pPip) *pPip = assetParams[3];
    if(pPipCost) *pPipCost = assetParams[4];
    if(pMinAmount) *pMinAmount = assetParams[5];
    if(pMargin) *pMargin = assetParams[6];
    if(pRollLong) *pRollLong = assetParams[7];
    if(pRollShort) *pRollShort = assetParams[8];

    env->DeleteLocalRef(jAsset);
    env->ReleaseDoubleArrayElements(jAssetParamsArray, assetParams, 0);
    env->DeleteLocalRef((jobject)jAssetParamsArray);

    return res;
}

int DllCallHandler::BrokerHistory(const char *Asset,
                                  const DATE tStart,
                                  const DATE tEnd,
                                  const int nTickMinutes,
                                  const int nTicks,
                                  TICK *ticks)
{
    jstring jAsset = env->NewStringUTF(Asset);
    int tickArrayLength = nTicks*5;
    jdoubleArray jTicksArray = env->NewDoubleArray(tickArrayLength);
    jlong startDate = DateUtils::fromDATE(tStart);
    jlong endDate =  DateUtils::fromDATE(tEnd);

    jint res = jniHandler.callBrokerHistory(env, jAsset, startDate, endDate, nTickMinutes, nTicks, jTicksArray);
    jdouble *ticksParams = env->GetDoubleArrayElements(jTicksArray, 0u);

    for(int i=0; i<nTicks;++i){
        int paramsIndex = i*5;
        ticks[i].fOpen = ticksParams[paramsIndex];
        ticks[i].fClose = ticksParams[paramsIndex+1];
        ticks[i].fHigh = ticksParams[paramsIndex+2];
        ticks[i].fLow = ticksParams[paramsIndex+3];
        ticks[i].time = DateUtils::fromDukaTime(ticksParams[paramsIndex+4]);
    }
    env->DeleteLocalRef(jAsset);
    env->ReleaseDoubleArrayElements(jTicksArray, ticksParams, 0u);
    env->DeleteLocalRef((jobject)jTicksArray);

    return res;
}

int DllCallHandler::BrokerAccount(const char *Account,
                                  double *pBalance,
                                  double *pTradeVal,
                                  double *pMarginVal)
{
    if(Account)
    {
        BrokerError("Multiple accounts are not yet supported!");
        return 0;
    }
    jdoubleArray jAccountParamsArray = env->NewDoubleArray(3);

    jint res = jniHandler.callBrokerAccount(env, jAccountParamsArray);
    jdouble *accountParams = env->GetDoubleArrayElements(jAccountParamsArray, 0);

    if(pBalance) *pBalance = accountParams[0];
    if(pTradeVal) *pTradeVal = accountParams[1];
    if(pMarginVal) *pMarginVal = accountParams[2];

    env->ReleaseDoubleArrayElements(jAccountParamsArray, accountParams, 0);
    env->DeleteLocalRef((jobject)jAccountParamsArray);

    return res;
}

int DllCallHandler::BrokerBuy(const char *Asset,
                              int nAmount,
                              double dStopDist,
                              double *pPrice)
{
    jstring jAsset = env->NewStringUTF(Asset);
    jdoubleArray jTradeParamsArray = env->NewDoubleArray(3);

    jdouble fill[2];
    fill[0] = nAmount;
    fill[1] = dStopDist;
    env->SetDoubleArrayRegion(jTradeParamsArray, 0, 2, fill);

    jint res = jniHandler.callBrokerBuy(env, jAsset, jTradeParamsArray);
    jdouble *tradeParams = env->GetDoubleArrayElements(jTradeParamsArray, 0);

    if(pPrice) *pPrice = tradeParams[2];

    env->DeleteLocalRef(jAsset);
    env->ReleaseDoubleArrayElements(jTradeParamsArray, tradeParams, 0);
    env->DeleteLocalRef((jobject)jTradeParamsArray);

    return res;
}

int DllCallHandler::BrokerTrade(const int nTradeID,
                                double *pOpen,
                                double *pClose,
                                double *pRoll,
                                double *pProfit)
{
    jdoubleArray jOrderParamsArray = env->NewDoubleArray(4);

    jint res = jniHandler.callBrokerTrade(env, nTradeID, jOrderParamsArray);
    jdouble *orderParams = env->GetDoubleArrayElements(jOrderParamsArray, 0);

    if(res > 0)
    {
        if(pOpen) *pOpen = orderParams[0];
        if(pClose) *pClose = orderParams[1];
        if(pRoll) *pRoll = orderParams[2];
        if(pProfit) *pProfit = orderParams[3];
    }
    env->ReleaseDoubleArrayElements(jOrderParamsArray, orderParams, 0);
    env->DeleteLocalRef((jobject)jOrderParamsArray);

    return res;
}

int DllCallHandler::BrokerStop(const int nTradeID,
                               const double dStop)
{
    return jniHandler.callBrokerStop(env, nTradeID, dStop);;
}

int DllCallHandler::BrokerSell(const int nTradeID,
                               const int nAmount)
{
    return jniHandler.callBrokerSell(env, nTradeID, nAmount);;
}

