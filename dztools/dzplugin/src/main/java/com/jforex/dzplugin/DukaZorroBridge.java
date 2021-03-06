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

import java.io.File;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IContext;
import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;
import com.jforex.dzplugin.config.DZPluginConfig;
import com.jforex.dzplugin.config.ReturnCodes;
import com.jforex.dzplugin.handler.AccountHandler;
import com.jforex.dzplugin.handler.HistoryHandler;
import com.jforex.dzplugin.handler.LoginHandler;
import com.jforex.dzplugin.handler.OrderHandler;
import com.jforex.dzplugin.handler.SubscriptionHandler;
import com.jforex.dzplugin.provider.AccountInfo;
import com.jforex.dzplugin.provider.IPriceEngine;
import com.jforex.dzplugin.provider.ServerTimeProvider;
import com.jforex.dzplugin.utils.DateTimeUtils;

public class DukaZorroBridge {

    private IClient client;
    private IContext context;
    private DukaZorroStrategy strategy;
    private AccountInfo accountInfo;
    private AccountHandler accountHandler;
    private HistoryHandler historyHandler;
    private OrderHandler orderHandler;
    private LoginHandler loginHandler;
    private SubscriptionHandler subscriptionHandler;
    private IPriceEngine priceEngine;
    private DateTimeUtils dateTimeUtils;
    private ServerTimeProvider serverTimeProvider;
    private boolean isStrategyStarted;
    private long strategyID;
    private final DZPluginConfig pluginConfig = ConfigFactory.create(DZPluginConfig.class);

    private final static Logger logger = LogManager.getLogger(DukaZorroBridge.class);

    public DukaZorroBridge() {
        initClientInstance();
        isStrategyStarted = false;
        strategy = new DukaZorroStrategy();
        loginHandler = new LoginHandler(client);
    }

    private void initClientInstance() {
        try {
            client = ClientFactory.getDefaultInstance();
            client.setSystemListener(new DukaZorroSystemListener());
            client.setCacheDirectory(new File(pluginConfig.CACHE_DIR()));
            logger.debug("IClient successfully initialized.");
            return;
        } catch (ClassNotFoundException e) {
            logger.error("IClient ClassNotFoundException occured! " + e.getMessage());
        } catch (IllegalAccessException e) {
            logger.error("IClient IllegalAccessException occured!" + e.getMessage());
        } catch (InstantiationException e) {
            logger.error("IClient InstantiationException occured!" + e.getMessage());
        }
        ZorroLogger.indicateError();
    }

    public void initComponentsAfterLogin() {
        if (!isStrategyStarted)
            strategyID = client.startStrategy(strategy);

        context = strategy.getContext();
        priceEngine = strategy.getPriceEngine();
        serverTimeProvider = new ServerTimeProvider(priceEngine);
        accountInfo = new AccountInfo(context.getAccount(), context.getUtils(), priceEngine);
        dateTimeUtils = new DateTimeUtils(context.getDataService(), serverTimeProvider);
        accountHandler = new AccountHandler(priceEngine, accountInfo, serverTimeProvider, dateTimeUtils);
        historyHandler = new HistoryHandler(context.getHistory());
        orderHandler = new OrderHandler(context, priceEngine, accountInfo);
        subscriptionHandler = new SubscriptionHandler(client, priceEngine, accountInfo);
    }

    public int doLogin(String User,
                       String Pwd,
                       String Type,
                       String accountInfos[]) {
        if (client.isConnected())
            return ReturnCodes.LOGIN_OK;

        int loginResult = loginHandler.doLogin(User, Pwd, Type, accountInfos);
        if (loginResult == ReturnCodes.LOGIN_OK) {
            initComponentsAfterLogin();
            String accountID = accountInfo.getID();
            accountInfos[0] = accountID;
            logger.info("Login successful for account ID " + accountID);
        }
        return loginResult;
    }

    public int doLogout() {
        client.stopStrategy(strategyID);
        return loginHandler.logout();
    }

    public int doBrokerTime(double serverTime[]) {
        if (!client.isConnected())
            return ReturnCodes.CONNECTION_FAIL;

        return accountHandler.doBrokerTime(serverTime);
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

        return accountHandler.doBrokerAsset(instrumentName, assetParams);
    }

    public int doBrokerAccount(double accountInfoParams[]) {
        if (!accountInfo.isConnected())
            return ReturnCodes.ACCOUNT_UNAVAILABLE;

        return accountHandler.doBrokerAccount(accountInfoParams);
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

    public int doHistoryDownload() {
        if (!client.isConnected())
            return ReturnCodes.HISTORY_DOWNLOAD_FAIL;

        return historyHandler.doHistoryDownload();
    }
}
