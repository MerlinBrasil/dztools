package com.jforex.dzplugin;

/*
 * #%L dzplugin $Id:$ $HeadURL:$ %% Copyright (C) 2014 juxeii %% This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/gpl-3.0.html>. #L%
 */

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.ICurrency;
import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;
import com.jforex.dzplugin.config.DukascopyParams;
import com.jforex.dzplugin.config.ReturnCodes;
import com.jforex.dzplugin.handler.HistoryHandler;
import com.jforex.dzplugin.handler.LoginHandler;
import com.jforex.dzplugin.handler.OrderHandler;
import com.jforex.dzplugin.handler.SubscriptionHandler;
import com.jforex.dzplugin.provider.AccountInfo;
import com.jforex.dzplugin.provider.IPriceEngine;
import com.jforex.dzplugin.utils.DateTimeUtils;
import com.jforex.dzplugin.utils.InstrumentUtils;

public class DukaZorroBridge {

    private IClient client;
    private IContext context;
    private DukaZorroStrategy strategy;
    private ICurrency accountCurrency;
    private AccountInfo accountInfo;
    private HistoryHandler historyHandler;
    private OrderHandler orderHandler;
    private LoginHandler loginHandler;
    private SubscriptionHandler subscriptionHandler;
    private IPriceEngine priceEngine;
    private DateTimeUtils dateTimeUtils;
    private boolean isStrategyStarted;

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
        } catch (ClassNotFoundException e) {
            logger.error("IClient ClassNotFoundException occured!");
        } catch (IllegalAccessException e) {
            logger.error("IClient IllegalAccessException occured!");
        } catch (InstantiationException e) {
            logger.error("IClient InstantiationException occured!");
        }
    }

    public int doLogin(String User,
                       String Pwd,
                       String Type,
                       String accountInfos[]) {
        if (client.isConnected())
            return ReturnCodes.LOGIN_OK;

        if (Type.equals("Demo"))
            return handleLogin(User, Pwd, "", accountInfos);
        else if (Type.equals("Real")) {
            logger.info("Live login not yet supported.");
            return ReturnCodes.LOGIN_FAIL;
            // MainPin mp = new MainPin(client);
            // String pin = mp.getPin();
            // return handleLogin(User, Pwd, pin, accountInfos);
        } else {
            logger.warn("Received invalid login type: " + Type);
            return ReturnCodes.LOGIN_FAIL;
        }
    }

    private int handleLogin(String User,
                            String Pwd,
                            String Pin,
                            String accountInfos[]) {
        int loginResult = loginHandler.login(User, Pwd, Pin);
        if (loginResult == ReturnCodes.LOGIN_OK) {
            initComponentsAfterLogin();
            accountInfos[0] = accountInfo.getID();
        }

        return loginResult;
    }

    private void initComponentsAfterLogin() {
        if (!isStrategyStarted)
            client.startStrategy(strategy);

        context = strategy.getContext();
        priceEngine = strategy.getPriceEngine();
        accountInfo = new AccountInfo(context, priceEngine);
        historyHandler = new HistoryHandler(context.getHistory());
        orderHandler = new OrderHandler(context);
        subscriptionHandler = new SubscriptionHandler(client);
        dateTimeUtils = new DateTimeUtils(context.getDataService());
        accountCurrency = accountInfo.getCurrency();
    }

    public int doLogout() {
        return loginHandler.logout();
    }

    public int doBrokerTime(long serverTime[]) {
        if (!client.isConnected()) {
            ZorroLogger.log("No connection to Dukascopy!");
            return ReturnCodes.CONNECTION_FAIL;
        }
        serverTime[0] = dateTimeUtils.getServerTime();

        return dateTimeUtils.isMarketOffline() ? ReturnCodes.CONNECTION_OK_BUT_MARKET_CLOSED : ReturnCodes.CONNECTION_OK;
    }

    public int doSubscribeAsset(String instrumentName) {
        if (!client.isConnected())
            return ReturnCodes.ASSET_UNAVAILABLE;

        Instrument toSubscribeInstrument = InstrumentUtils.getByName(instrumentName);
        if (toSubscribeInstrument == null) {
            ZorroLogger.log(instrumentName + " is no valid asset name!");
            return ReturnCodes.ASSET_UNAVAILABLE;
        }

        Set<Instrument> instruments = new HashSet<Instrument>();
        instruments.add(toSubscribeInstrument);
        // we must subscribe to cross instrument also for margin calculations
        Instrument crossInstrument = InstrumentUtils.getfromCurrencies(accountCurrency, toSubscribeInstrument.getPrimaryJFCurrency());
        if (crossInstrument != null) {
            ZorroLogger.log("crossInstrument: " + crossInstrument);
            instruments.add(crossInstrument);
        }

        int subscriptionResult = subscriptionHandler.subscribe(instruments);
        if (subscriptionResult == ReturnCodes.ASSET_AVAILABLE)
            priceEngine.initInstruments(instruments);

        return subscriptionResult;
    }

    public int doBrokerAsset(String instrumentName,
                             double assetParams[]) {
        if (!accountInfo.isConnected())
            return ReturnCodes.ASSET_UNAVAILABLE;

        Instrument instrument = InstrumentUtils.getByName(instrumentName);
        if (instrument == null) {
            ZorroLogger.log(instrumentName + " is no valid asset name!");
            return ReturnCodes.ASSET_UNAVAILABLE;
        }
        ITick tick = priceEngine.getLastTick(instrument);
        if (tick == null) {
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
        // ZorroLogger.log("instrument: " + instrument);
        // ZorroLogger.log("price: " + assetParams[0]);
        // ZorroLogger.log("spread: " + assetParams[1]);
        // ZorroLogger.log("pipVal: " + assetParams[3]);
        // ZorroLogger.log("pipCost: " + assetParams[4]);
        // ZorroLogger.log("minLOT: " + assetParams[5]);
        // ZorroLogger.log("LOTMargin: " + assetParams[6]);

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

        Instrument instrument = InstrumentUtils.getByName(instrumentName);
        if (instrument == null) {
            ZorroLogger.log(instrumentName + " is no valid asset name!");
            return ReturnCodes.ORDER_SUBMIT_FAIL;
        }
        double amount = tradeParams[0];
        double dStopDist = tradeParams[1];

        OrderCommand cmd = OrderCommand.BUY;
        if (amount < 0) {
            amount = -amount;
            cmd = OrderCommand.SELL;
        }
        // Scale amount to millions
        amount /= DukascopyParams.LOT_SCALE;

        double currentAskPrice = priceEngine.getAsk(instrument);
        double spread = priceEngine.getSpread(instrument);

        double SLPrice = 0;
        if (dStopDist > 0) {
            if (cmd == OrderCommand.BUY)
                SLPrice = currentAskPrice - dStopDist - spread;
            else
                SLPrice = currentAskPrice + dStopDist;
        }
        int orderID = orderHandler.submitOrder(instrument, cmd, amount, priceEngine.getRounded(instrument, SLPrice));
        if (orderID == ReturnCodes.INVALID_ORDER_ID) {
            return ReturnCodes.ORDER_SUBMIT_FAIL;
        }
        tradeParams[2] = orderHandler.getOrderByID(orderID).getOpenPrice();

        return orderID;
    }

    public int doBrokerTrade(int orderID,
                             double orderParams[]) {
        if (!accountInfo.isConnected())
            return ReturnCodes.INVALID_ORDER_ID;

        if (!orderHandler.isOrderIDValid(orderID))
            return ReturnCodes.INVALID_ORDER_ID;

        IOrder order = orderHandler.getOrderByID(orderID);
        orderParams[0] = order.getOpenPrice();
        if (order.isLong())
            orderParams[1] = priceEngine.getAsk(order.getInstrument());
        else
            orderParams[1] = priceEngine.getBid(order.getInstrument());
        // Rollover not supported by Dukascopy
        orderParams[2] = 0f;
        orderParams[3] = order.getProfitLossInAccountCurrency();
        int orderAmount = (int) (order.getAmount() * DukascopyParams.LOT_SCALE);

        return order.getState() == IOrder.State.CLOSED ? -orderAmount : orderAmount;
    }

    public int doBrokerStop(int orderID,
                            double newSLPrice) {
        if (!accountInfo.isTradingPossible())
            return ReturnCodes.ADJUST_SL_FAIL;

        if (!orderHandler.isOrderIDValid(orderID))
            return ReturnCodes.ADJUST_SL_FAIL;

        IOrder order = orderHandler.getOrderByID(orderID);
        if (order.getStopLossPrice() == 0) {
            logger.warn("Order has no SL set -> reject BrokerStop!");
            return ReturnCodes.ADJUST_SL_FAIL;
        }

        return orderHandler.setSLPrice(order, priceEngine.getRounded(order.getInstrument(), newSLPrice));
    }

    public int doBrokerSell(int orderID,
                            int amount) {
        if (!accountInfo.isTradingPossible())
            return ReturnCodes.ORDER_CLOSE_FAIL;

        double convertedAmount = Math.abs(amount) / DukascopyParams.LOT_SCALE;
        return orderHandler.closeOrder(orderID, convertedAmount);
    }

    public int doBrokerHistory(String instrumentName,
                               long startDate,
                               long endDate,
                               int tickMinutes,
                               int nTicks,
                               double tickParams[]) {
        if (!accountInfo.isConnected())
            return ReturnCodes.HISTORY_FAIL;

        logger.debug("Broker tickMinutes " + tickMinutes);
        logger.debug("Broker nTicks " + nTicks);
        Instrument instrument = InstrumentUtils.getByName(instrumentName);
        if (instrument == null) {
            ZorroLogger.log(instrumentName + " is no valid asset!");
            return ReturnCodes.HISTORY_FAIL;
        }

        Period period = DateTimeUtils.getPeriodFromMinutes(tickMinutes);
        if (period == null) {
            ZorroLogger.log("Invalid tickMinutes: " + tickMinutes);
            return ReturnCodes.HISTORY_FAIL;
        }
        List<IBar> bars = historyHandler.getBars(instrument, period, OfferSide.ASK, startDate, endDate);
        if (bars.size() == 0)
            return ReturnCodes.HISTORY_FAIL;

        int tickParamsIndex = 0;
        int arraySizeForAllBars = 5 * bars.size();
        int maxIndex = nTicks;
        if (arraySizeForAllBars <= tickParams.length)
            maxIndex = bars.size();

        logger.debug("bars size " + bars.size());
        logger.debug("maxIndex " + maxIndex);

        for (int i = 0; i < maxIndex; ++i) {
            IBar bar = bars.get(i);
            tickParams[tickParamsIndex] = bar.getOpen();
            tickParams[tickParamsIndex + 1] = bar.getClose();
            tickParams[tickParamsIndex + 2] = bar.getHigh();
            tickParams[tickParamsIndex + 3] = bar.getLow();
            tickParams[tickParamsIndex + 4] = bar.getTime();
            tickParamsIndex += 5;
        }
        return maxIndex;
    }
}
