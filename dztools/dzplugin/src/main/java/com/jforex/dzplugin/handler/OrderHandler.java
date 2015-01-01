package com.jforex.dzplugin.handler;

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

import java.rmi.server.UID;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.jforex.dzplugin.DZPluginConfig;
import com.jforex.dzplugin.ZorroLogger;
import com.jforex.dzplugin.config.DukascopyParams;
import com.jforex.dzplugin.config.ReturnCodes;
import com.jforex.dzplugin.provider.AccountInfo;
import com.jforex.dzplugin.provider.IPriceEngine;
import com.jforex.dzplugin.task.CloseOrderTask;
import com.jforex.dzplugin.task.StopLossTask;
import com.jforex.dzplugin.task.SubmitOrderTask;
import com.jforex.dzplugin.utils.InstrumentUtils;

public class OrderHandler {

    private final IContext context;
    private final IEngine engine;
    private final IPriceEngine priceEngine;
    private final AccountInfo accountInfo;
    private final HashMap<Integer, IOrder> orderMap;
    private final DZPluginConfig pluginConfig = ConfigFactory.create(DZPluginConfig.class);

    private final static Logger logger = LogManager.getLogger(OrderHandler.class);

    public OrderHandler(IContext context,
                        IPriceEngine priceEngine,
                        AccountInfo accountInfo) {
        this.context = context;
        this.priceEngine = priceEngine;
        this.accountInfo = accountInfo;
        this.engine = context.getEngine();

        orderMap = new HashMap<Integer, IOrder>();
        resumeOrderIDs();
    }

    public int doBrokerBuy(String instrumentName,
                           double tradeParams[]) {
        if (!accountInfo.isTradingPossible())
            return ReturnCodes.ORDER_SUBMIT_FAIL;

        Instrument instrument = InstrumentUtils.getByName(instrumentName);
        if (instrument == null)
            return ReturnCodes.ORDER_SUBMIT_FAIL;
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
        int orderID = submitOrder(instrument, cmd, amount, priceEngine.getRounded(instrument, SLPrice));
        if (orderID == ReturnCodes.INVALID_ORDER_ID) {
            ZorroLogger.log("Could not open position for " + instrument + ".Check logs!");
            return ReturnCodes.ORDER_SUBMIT_FAIL;
        }
        tradeParams[2] = getOrderByID(orderID).getOpenPrice();

        return orderID;
    }

    public int doBrokerTrade(int orderID,
                             double orderParams[]) {
        if (!isOrderIDValid(orderID)) {
            logger.warn("Order ID " + orderID + " is unknown!");
            ZorroLogger.log("Order ID " + orderID + " is unknown!");
            return ReturnCodes.INVALID_ORDER_ID;
        }

        IOrder order = getOrderByID(orderID);
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
        logger.debug("orderID " + orderID + " newSLPrice: " + newSLPrice);

        if (!isOrderIDValid(orderID)) {
            logger.warn("Order ID " + orderID + " is unknown!");
            ZorroLogger.log("Order ID " + orderID + " is unknown!");
            return ReturnCodes.ADJUST_SL_FAIL;
        }

        IOrder order = getOrderByID(orderID);
        if (order.getStopLossPrice() == 0) {
            logger.warn("Order has no SL set -> reject BrokerStop!");
            return ReturnCodes.ADJUST_SL_FAIL;
        }

        return setSLPrice(order, priceEngine.getRounded(order.getInstrument(), newSLPrice));
    }

    public int doBrokerSell(int orderID,
                            int amount) {
        double convertedAmount = Math.abs(amount) / DukascopyParams.LOT_SCALE;
        logger.debug("orderID " + orderID + " amount: " + amount + " convertedAmount " + convertedAmount);

        return closeOrder(orderID, convertedAmount);
    }

    public synchronized int submitOrder(Instrument instrument,
                                        OrderCommand cmd,
                                        double amount,
                                        double SLPrice) {
        int orderID = Math.abs(new UID().hashCode());
        String orderLabel = pluginConfig.ORDER_PREFIX_LABEL() + orderID;

        logger.info("Try to open position for " + instrument +
                " with cmd " + cmd + " ,amount " + amount +
                " ,SLPrice " + SLPrice + " ,orderLabel " +
                orderLabel + " orderID " + orderID);
        SubmitOrderTask task = new SubmitOrderTask(engine, orderLabel, instrument, cmd, amount, SLPrice);
        IOrder order = getOrderFromFuture(context.executeTask(task));
        if (order == null)
            return ReturnCodes.INVALID_ORDER_ID;
        logger.info("Order submission for " + instrument +
                " with cmd " + cmd + " ,amount " + amount +
                " ,SLPrice " + SLPrice + " ,orderLabel " +
                orderLabel + " orderID " + orderID + " successful.");
        orderMap.put(orderID, order);

        return orderID;
    }

    public IOrder getOrderByID(int orderID) {
        return orderMap.get(orderID);
    }

    public boolean isOrderIDValid(int orderID) {
        return orderMap.containsKey(orderID);
    }

    public int closeOrder(int orderID,
                          double amount) {
        if (!orderMap.containsKey(orderID))
            return ReturnCodes.INVALID_ORDER_ID;

        IOrder order = orderMap.get(orderID);
        if (order.getState() != IOrder.State.OPENED && order.getState() != IOrder.State.FILLED) {
            logger.warn("Order " + orderID + " could not be closed. Order state: " + order.getState());
            return ReturnCodes.INVALID_ORDER_ID;
        }

        CloseOrderTask task = new CloseOrderTask(order, amount);
        order = getOrderFromFuture(context.executeTask(task));
        if (order.getState() != IOrder.State.CLOSED)
            return ReturnCodes.INVALID_ORDER_ID;
        else
            return orderID;
    }

    public int setSLPrice(IOrder order,
                          double newSLPrice) {
        StopLossTask task = new StopLossTask(order, newSLPrice);
        order = getOrderFromFuture(context.executeTask(task));

        return ReturnCodes.ADJUST_SL_OK;
    }

    private IOrder getOrderFromFuture(Future<IOrder> orderFuture) {
        IOrder order = null;
        try {
            order = orderFuture.get();
        } catch (InterruptedException e) {
            ZorroLogger.indicateError(logger, "InterruptedException: " + e.getMessage());
        } catch (ExecutionException e) {
            ZorroLogger.indicateError(logger, "ExecutionException: " + e.getMessage());
        }
        return order;
    }

    private synchronized void resumeOrderIDs() {
        List<IOrder> orders = null;
        try {
            orders = engine.getOrders();
        } catch (JFException e) {
            ZorroLogger.indicateError(logger, "getOrders exc: " + e.getMessage());
        }
        for (IOrder order : orders)
            resumeOrderIDIfFound(order);
    }

    private void resumeOrderIDIfFound(IOrder order) {
        String label = order.getLabel();
        if (label.startsWith(pluginConfig.ORDER_PREFIX_LABEL())) {
            int id = getOrderIDFromLabel(label);
            orderMap.put(id, order);
        }
    }

    private int getOrderIDFromLabel(String label) {
        String idName = label.substring(pluginConfig.ORDER_PREFIX_LABEL().length());
        return Integer.parseInt(idName);
    }
}
