package com.jforex.dzplugin;

/*
 * #%L
 * dzplugin $Id:$ $HeadURL:$
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

import com.jforex.dzplugin.config.DukascopyParams;
import com.jforex.dzplugin.config.ReturnCodes;
import com.jforex.dzplugin.handler.HistoryHandler;
import com.jforex.dzplugin.handler.LoginHandler;
import com.jforex.dzplugin.handler.OrderHandler;
import com.jforex.dzplugin.handler.SubscriptionHandler;
import com.jforex.dzplugin.provider.AccountInfo;
import com.jforex.dzplugin.provider.IPriceEngine;
import com.jforex.dzplugin.provider.ServerTimeProvider;
import com.jforex.dzplugin.utils.DateTimeUtils;
import com.jforex.dzplugin.utils.InstrumentUtils;

import com.dukascopy.api.IContext;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;

public class DukaZorroBridge {

    private IClient client;
    private IContext context;
    private DukaZorroStrategy strategy;
    private AccountInfo accountInfo;
    private HistoryHandler historyHandler;
    private OrderHandler orderHandler;
    private LoginHandler loginHandler;
    private SubscriptionHandler subscriptionHandler;
    private IPriceEngine priceEngine;
    private DateTimeUtils dateTimeUtils;
    private ServerTimeProvider serverTimeProvider;
    private boolean isStrategyStarted;

    private final static Logger logger = LogManager.getLogger(DukaZorroBridge.class);

    public DukaZorroBridge() {
        initClientInstance();
        isStrategyStarted = false;
        strategy = new DukaZorroStrategy();
        loginHandler = new LoginHandler(this);
    }

    private void initClientInstance() {
        try {
            client = ClientFactory.getDefaultInstance();
            client.setSystemListener(new DukaZorroSystemListener());
            logger.debug("IClient successfully initialized.");
            return;
        } catch (ClassNotFoundException e) {
            ZorroLogger.indicateError(logger, "IClient ClassNotFoundException occured!");
        } catch (IllegalAccessException e) {
            ZorroLogger.indicateError(logger, "IClient IllegalAccessException occured!");
        } catch (InstantiationException e) {
            ZorroLogger.indicateError(logger, "IClient InstantiationException occured!");
        }
    }

    public void initComponentsAfterLogin() {
        if (!isStrategyStarted)
            client.startStrategy(strategy);

        context = strategy.getContext();
        priceEngine = strategy.getPriceEngine();
        serverTimeProvider = new ServerTimeProvider(this);
        accountInfo = new AccountInfo(this);
        historyHandler = new HistoryHandler(this);
        orderHandler = new OrderHandler(this);
        subscriptionHandler = new SubscriptionHandler(this);
        dateTimeUtils = new DateTimeUtils(this);
    }

    public int doLogin(String User,
                       String Pwd,
                       String Type,
                       String accountInfos[]) {
        if (client.isConnected())
            return ReturnCodes.LOGIN_OK;

        return loginHandler.doLogin(User, Pwd, Type, accountInfos);
    }

    public int doLogout() {
        return loginHandler.logout();
    }

    public int doBrokerTime(double serverTime[]) {
        if (!client.isConnected()) {
            logger.warn("No connection to Dukascopy!");
            return ReturnCodes.CONNECTION_FAIL;
        }
        serverTime[0] = DateTimeUtils.getOLEDateFromMillis(serverTimeProvider.get());

        boolean isMarketOffline = dateTimeUtils.isMarketOffline();
        if (isMarketOffline)
            logger.debug("Market is offline");

        return isMarketOffline ? ReturnCodes.CONNECTION_OK_BUT_MARKET_CLOSED : ReturnCodes.CONNECTION_OK;
    }

    public int doSubscribeAsset(String instrumentName) {
        if (!client.isConnected())
            return ReturnCodes.ASSET_UNAVAILABLE;

        return subscriptionHandler.doSubscribeAsset(instrumentName);
    }

    public int doBrokerAsset(String instrumentName,
                             double assetParams[]) {
        if (!accountInfo.isConnected())
            return ReturnCodes.ASSET_UNAVAILABLE;

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
        if (!accountInfo.isConnected())
            return ReturnCodes.ACCOUNT_UNAVAILABLE;

        accountInfoParams[0] = accountInfo.getBalance();
        accountInfoParams[1] = accountInfo.getTradeValue();
        accountInfoParams[2] = accountInfo.getUsedMargin();

        return ReturnCodes.ACCOUNT_AVAILABLE;
    }

    public int doBrokerBuy(String instrumentName,
                           double tradeParams[]) {
        if (!accountInfo.isTradingPossible())
            return ReturnCodes.ORDER_SUBMIT_FAIL;

        return orderHandler.doBrokerBuy(instrumentName, tradeParams);
    }

    public int doBrokerTrade(int orderID,
                             double orderParams[]) {
        if (!accountInfo.isTradingPossible())
            return ReturnCodes.INVALID_ORDER_ID;

        return orderHandler.doBrokerTrade(orderID, orderParams);
    }

    public int doBrokerStop(int orderID,
                            double newSLPrice) {
        if (!accountInfo.isTradingPossible())
            return ReturnCodes.ADJUST_SL_FAIL;

        return orderHandler.doBrokerStop(orderID, newSLPrice);
    }

    public int doBrokerSell(int orderID,
                            int amount) {
        if (!accountInfo.isTradingPossible())
            return ReturnCodes.ORDER_CLOSE_FAIL;

        return orderHandler.doBrokerSell(orderID, amount);
    }

    public int doBrokerHistory(String instrumentName,
                               double startDate,
                               double endDate,
                               int tickMinutes,
                               int nTicks,
                               double tickParams[]) {
        if (!accountInfo.isConnected())
            return ReturnCodes.HISTORY_FAIL;

        return historyHandler.doBrokerHistory(instrumentName, startDate, endDate, tickMinutes, nTicks, tickParams);
    }

    public void doDLLlog(String msg) {
        logger.info(msg);
    }

    public IClient getClient() {
        return client;
    }

    public IContext getContext() {
        return context;
    }

    public AccountInfo getAccountInfo() {
        return accountInfo;
    }

    public IPriceEngine getPriceEngine() {
        return priceEngine;
    }

    public DateTimeUtils getDateTimeUtils() {
        return dateTimeUtils;
    }

    public ServerTimeProvider getServerTimeProvider() {
        return serverTimeProvider;
    }
}
