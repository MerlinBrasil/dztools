package com.jforex.dzplugin.handler;

/*
 * #%L
 * dzplugin
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.dzplugin.ZorroLogger;
import com.jforex.dzplugin.config.DukascopyParams;
import com.jforex.dzplugin.config.ReturnCodes;
import com.jforex.dzplugin.provider.AccountInfo;
import com.jforex.dzplugin.provider.IPriceEngine;
import com.jforex.dzplugin.provider.ServerTimeProvider;
import com.jforex.dzplugin.utils.DateTimeUtils;
import com.jforex.dzplugin.utils.InstrumentUtils;

import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;

public class AccountHandler {

    private final IPriceEngine priceEngine;
    private final AccountInfo accountInfo;
    private final ServerTimeProvider serverTimeProvider;
    private final DateTimeUtils dateTimeUtils;

    private final static Logger logger = LogManager.getLogger(AccountHandler.class);

    public AccountHandler(IPriceEngine priceEngine,
                          AccountInfo accountInfo,
                          ServerTimeProvider serverTimeProvider,
                          DateTimeUtils dateTimeUtils) {
        this.priceEngine = priceEngine;
        this.accountInfo = accountInfo;
        this.serverTimeProvider = serverTimeProvider;
        this.dateTimeUtils = dateTimeUtils;
    }

    public int doBrokerTime(double serverTime[]) {
        serverTime[0] = DateTimeUtils.getOLEDateFromMillis(serverTimeProvider.get());

        boolean isMarketOffline = dateTimeUtils.isMarketOffline();
        if (isMarketOffline)
            logger.debug("Market is offline");

        return isMarketOffline ? ReturnCodes.CONNECTION_OK_BUT_MARKET_CLOSED : ReturnCodes.CONNECTION_OK;
    }

    public int doBrokerAsset(String instrumentName,
                             double assetParams[]) {
        Instrument instrument = InstrumentUtils.getByName(instrumentName);
        if (instrument == null)
            return ReturnCodes.ASSET_UNAVAILABLE;

        ITick tick = priceEngine.getLastTick(instrument);
        if (tick == null) {
            logger.warn("No data for " + instrument + " available!");
            ZorroLogger.log("No data for " + instrument + " available!");
            return ReturnCodes.ASSET_UNAVAILABLE;
        }
        assetParams[0] = tick.getAsk();
        assetParams[1] = priceEngine.getSpread(instrument);
        // Volume: not supported for Forex
        assetParams[2] = 0f;
        assetParams[3] = instrument.getPipValue();
        double pipCost = accountInfo.getPipCost(instrument, OfferSide.ASK);
        if (pipCost == 0f)
            return ReturnCodes.ASSET_UNAVAILABLE;
        assetParams[4] = pipCost;
        assetParams[5] = DukascopyParams.LOT_SIZE;
        double marginForLot = accountInfo.getMarginForLot(instrument);
        if (marginForLot == 0f)
            return ReturnCodes.ASSET_UNAVAILABLE;
        assetParams[6] = marginForLot;
        // RollLong : currently not available by Dukascopy
        assetParams[7] = 0f;
        // RollShort: currently not available by Dukascopy
        assetParams[8] = 0f;

        return ReturnCodes.ASSET_AVAILABLE;
    }

    public int doBrokerAccount(double accountInfoParams[]) {
        accountInfoParams[0] = accountInfo.getBalance();
        accountInfoParams[1] = accountInfo.getTradeValue();
        accountInfoParams[2] = accountInfo.getUsedMargin();

        return ReturnCodes.ACCOUNT_AVAILABLE;
    }
}
