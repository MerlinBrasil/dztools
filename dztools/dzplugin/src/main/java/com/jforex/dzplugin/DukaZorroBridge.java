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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.Filter;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;
import com.jforex.dzplugin.config.DZPluginConfig;
import com.jforex.dzplugin.config.HistoryConfig;
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
import com.jforex.dzplugin.utils.InstrumentUtils;

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
    private final DZPluginConfig pluginConfig = ConfigFactory.create(DZPluginConfig.class);
    private final HistoryConfig historyConfig = ConfigFactory.create(HistoryConfig.class);

    private FileOutputStream outStream;
    private ByteBuffer bbf;

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
            client.setCacheDirectory(new File(pluginConfig.CACHE_DIR()));
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

        return loginHandler.doLogin(User, Pwd, Type, accountInfos);
    }

    public int doLogout() {
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
        String instrumentName = historyConfig.Asset();
        int startYear = historyConfig.StartYear();
        int endYear = historyConfig.EndYear();
        int numYears = endYear - startYear + 1;

        Instrument instrument = InstrumentUtils.getByName(instrumentName);
        if (instrument == null) {
            ZorroLogger.log("Asset " + instrumentName + " is invalid!");
            return ReturnCodes.HISTORY_DOWNLOAD_FAIL;
        }
        for (int i = 0; i < numYears; ++i) {
            int currentYear = startYear + i;

            GregorianCalendar calendarStart = new GregorianCalendar(currentYear, 0, 1, 0, 0, 0);
            calendarStart.setTimeZone(TimeZone.getTimeZone("UTC"));
            long startTime = calendarStart.getTimeInMillis();

            GregorianCalendar calendarEnd = new GregorianCalendar(currentYear, 11, 31, 23, 59, 0);
            calendarEnd.setTimeZone(TimeZone.getTimeZone("UTC"));
            long endTime = calendarEnd.getTimeInMillis();

            String startTimeString = DateTimeUtils.formatDateTime(startTime);
            String endTimeString = DateTimeUtils.formatDateTime(endTime);
            ZorroLogger.log("Fetching bars from " + startTimeString + " to " + endTimeString + " for " + currentYear);

            IHistory history = context.getHistory();
            List<IBar> bars = new ArrayList<IBar>();
            try {
                bars = history.getBars(instrument, Period.ONE_MIN, OfferSide.ASK, Filter.WEEKENDS, startTime, endTime);
            } catch (JFException e) {
                ZorroLogger.log("History exception: " + e.getMessage());
            }
            Collections.reverse(bars);
            ZorroLogger.log("Successfully fetched " + bars.size() + " " + Period.ONE_MIN + " bars" + " for " + currentYear);
            try {
                writeBarsToTICKsFile(bars, instrument, currentYear);
            } catch (IOException e) {
                ZorroLogger.log("IOException while wrting file: " + e.getMessage());
            }
        }

        return ReturnCodes.HISTORY_DOWNLOAD_OK;
    }

    private void writeBarsToTICKsFile(List<IBar> bars,
                                      Instrument instrument, int year) throws IOException {
        logger.info("Writing TICKs to file...");

        initByteBuffer(bars.size());
        writeBarsToBuffer(bars, bbf);
        writeBufferToFile(bbf, instrument, year);

        logger.info("Writing TICKs file finished.");
    }

    private void writeBarsToBuffer(List<IBar> bars,
                                   ByteBuffer bbf) {
        for (IBar bar : bars) {
            bbf.putFloat((float) bar.getOpen());
            bbf.putFloat((float) bar.getClose());
            bbf.putFloat((float) bar.getHigh());
            bbf.putFloat((float) bar.getLow());
            bbf.putDouble(DateTimeUtils.getOLEDateFromMillisRounded(bar.getTime()));
            // logger.info("Date: " +
            // DateTimeUtils.formatOLETime(DateTimeUtils.getOLEDateFromMillisRounded(bar.getTime())));
        }
    }

    private void initByteBuffer(int barsCount) {
        bbf = ByteBuffer.allocate(24 * barsCount);
        bbf.order(ByteOrder.LITTLE_ENDIAN);
    }

    private void writeBufferToFile(ByteBuffer bbf, Instrument instrument, int year) {
        try {
            outStream = new FileOutputStream(getFileName(instrument, year));
            outStream.write(bbf.array(), 0, bbf.limit());
            outStream.close();
        } catch (FileNotFoundException e) {
            ZorroLogger.log("FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            ZorroLogger.log("IOException while writing TICKs file! " + e.getMessage());
        }
    }

    private String getFileName(Instrument instrument, int year) {
        return "Plugin\\dztools\\dzconverter\\bars\\" + instrument.getPrimaryJFCurrency().getCurrencyCode() +
                instrument.getSecondaryJFCurrency().getCurrencyCode() +
                "_" + year + ".bar";
    }

    public void doDLLlog(String msg) {
        logger.info("DLL msg " + msg);
    }

    public IClient getClient() {
        return client;
    }

    public AccountInfo getAccountInfo() {
        return accountInfo;
    }
}
