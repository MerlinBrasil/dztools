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

#include "dukazorrobridge.hpp"
#include "DllCallHandler.hpp"
#include <sstream>

#define DLLFUNC extern "C" __declspec(dllexport)

int (__cdecl *BrokerError)(const char *txt) = nullptr;
int (__cdecl *BrokerProgress)(const int percent) = nullptr;
const int HISTORY_DOWNLOAD = 666;
static DllCallHandler dllCallHandler;

BOOL APIENTRY DllMain(HMODULE hModule,
                              DWORD  ul_reason_for_call,
                              LPVOID lpReserved)
{
    switch (ul_reason_for_call)
    {
        case DLL_PROCESS_ATTACH:
            break;
        case DLL_PROCESS_DETACH:
            break;
        case DLL_THREAD_ATTACH:
            break;
        case DLL_THREAD_DETACH:
            break;
    }
    return TRUE;
}

int jcallback_BrokerError(JNIEnv *env,
                          jclass clazz,
                          jstring msg)
{
    int result = 0;
    const char *ctxt = env->GetStringUTFChars(msg, nullptr);
    if (ctxt != nullptr) {
        result = BrokerError(ctxt);
        env->ReleaseStringUTFChars(msg, ctxt);
    }
    return result;
}

int jcallback_BrokerProgress(JNIEnv *env,
                             jclass clazz,
                             jint progress)
{
    return BrokerProgress(progress);
}

DLLFUNC int BrokerOpen(char *Name,
                       FARPROC fpError,
                       FARPROC fpProgress)
{
    strcpy(Name, "Dukascopy");
    (FARPROC&)BrokerError = fpError;
    (FARPROC&)BrokerProgress = fpProgress;

    return PLUGIN_VERSION;
}

DLLFUNC int BrokerLogin(const char *User,
                        const char *Pwd,
                        const char *Type,
                        char *Account)
{
    if(User)
        return dllCallHandler.BrokerLogin(User, Pwd, Type, Account);
    else
        return dllCallHandler.BrokerLogout();
}

DLLFUNC int BrokerTime(DATE *pTimeUTC)
{
    return dllCallHandler.BrokerTime(pTimeUTC);
}

DLLFUNC int BrokerAsset(const char *Asset,
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
    if(!pPrice)
        return dllCallHandler.SubscribeAsset(Asset);
    else
        return dllCallHandler.BrokerAsset(Asset, pPrice, pSpread, pVolume, pPip, pPipCost, pMinAmount, pMargin, pRollLong, pRollShort);
}


DLLFUNC int BrokerHistory(const char *Asset,
                          const DATE tStart,
                          const DATE tEnd,
                          const int nTickMinutes,
                          const int nTicks,
                          TICK *ticks)
{
    return dllCallHandler.BrokerHistory(Asset, tStart, tEnd, nTickMinutes, nTicks, ticks);
}

DLLFUNC int BrokerAccount(const char *Account,
                          double *pBalance,
                          double *pTradeVal,
                          double *pMarginVal)
{
    return dllCallHandler.BrokerAccount(Account, pBalance, pTradeVal, pMarginVal);
}

DLLFUNC int BrokerBuy(const char *Asset,
                      int nAmount,
                      double dStopDist,
                      double *pPrice)
{
    return dllCallHandler.BrokerBuy(Asset, nAmount, dStopDist, pPrice);
}

DLLFUNC int BrokerTrade(const int nTradeID,
                        double *pOpen,
                        double *pClose,
                        double *pRoll,
                        double *pProfit)
{
    return dllCallHandler.BrokerTrade(nTradeID, pOpen, pClose, pRoll,pProfit);
}

DLLFUNC int BrokerStop(const int nTradeID,
                       const double dStop)
{
    return dllCallHandler.BrokerStop(nTradeID, dStop);
}

DLLFUNC int BrokerSell(const int nTradeID,
                       const int nAmount)
{
    return dllCallHandler.BrokerSell(nTradeID, nAmount);
}

DLLFUNC double BrokerCommand(const int nCommand,
                             const DWORD dwParameter)
{
    switch(nCommand)
    {
        case GET_MINLOT:
            return 1000l;
        case GET_TYPE:
            return 1l;
        case HISTORY_DOWNLOAD:
            return dllCallHandler.ProcessHistoryDownload();
        default:
        {
            std::stringstream buffer;
            buffer << "Command " << nCommand << "not yet supported.";
            BrokerError(buffer.str().c_str());
        }
    }
    return 0l;
}
