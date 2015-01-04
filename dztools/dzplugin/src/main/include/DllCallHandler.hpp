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

#ifndef DLLCALLHANDLER_HPP
#define DLLCALLHANDLER_HPP

#include "dukazorrobridge.hpp"
#include "JNIHandler.hpp"

class DllCallHandler
{

public:
    int BrokerLogin(const char *User,
                    const char *Pwd,
                    const char *Type,
                    char *Account);

    int BrokerLogout();

    int BrokerTime(DATE *pTimeUTC);

    int SubscribeAsset(const char* Asset);

    int BrokerAsset(const char *Asset,
                    double *pPrice,
                    double *pSpread,
                    double *pVolume,
                    double *pPip,
                    double *pPipCost,
                    double *pMinAmount,
                    double *pMargin,
                    double *pRollLong,
                    double *pRollShort);

    int BrokerHistory(const char *Asset,
                      const DATE tStart,
                      const DATE tEnd,
                      const int nTickMinutes,
                      const int nTicks,
                      TICK *ticks);

    int BrokerAccount(const char *Account,
                      double *pBalance,
                      double *pTradeVal,
                      double *pMarginVal);

    int BrokerBuy(const char *Asset,
                  int nAmount,
                  double dStopDist,
                  double *pPrice);

    int BrokerTrade(const int nTradeID,
                    double *pOpen,
                    double *pClose,
                    double *pRoll,
                    double *pProfit);

    int BrokerStop(const int nTradeID,
                   const double dStop);

    int BrokerSell(const int nTradeID,
                   const int nAmount);

    int ProcessHistoryDownload();

private:
    JNIHandler jniHandler;
    JNIEnv *env;
};

#endif /* DLLCALLHANDLER_HPP */
