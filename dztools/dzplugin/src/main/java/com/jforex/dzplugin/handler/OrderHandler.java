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

import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;

import com.jforex.dzplugin.ZorroLogger;
import com.jforex.dzplugin.config.DukascopyParams;
import com.jforex.dzplugin.config.ReturnCodes;
import com.jforex.dzplugin.task.CloseOrderTask;
import com.jforex.dzplugin.task.StopLossTask;
import com.jforex.dzplugin.task.SubmitOrderTask;

public class OrderHandler {

    private final IContext context;
    private final IEngine engine;
    private final HashMap<Integer, IOrder> orderMap;

    public OrderHandler(IContext context) {
        this.context = context;
        this.engine = context.getEngine();
        orderMap = new HashMap<Integer, IOrder>();
        resumeOrderIDs();
    }

    public synchronized int submitOrder(Instrument instrument,
                                        OrderCommand cmd,
                                        double amount,
                                        double SLPrice) {
        int orderID = new UID().hashCode();
        String orderLabel = DukascopyParams.ORDER_PREFIX_LABEL + orderID;

        SubmitOrderTask task = new SubmitOrderTask(engine, orderLabel, instrument, cmd, amount, SLPrice);
        IOrder order = getOrderFromFuture(context.executeTask(task));
        if (order == null)
            return ReturnCodes.INVALID_ORDER_ID;
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
        if (order.getState() != IOrder.State.OPENED && order.getState() != IOrder.State.FILLED)
            return ReturnCodes.INVALID_ORDER_ID;

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
            ZorroLogger.log("InterruptedException");
        } catch (ExecutionException e) {
            ZorroLogger.log("ExecutionException");
        }
        return order;
    }

    private synchronized void resumeOrderIDs() {
        List<IOrder> orders = null;
        try {
            orders = engine.getOrders();
        } catch (JFException e) {
            ZorroLogger.log("getOrders exc: " + e.getMessage());
        }
        for (IOrder order : orders) {
            String label = order.getLabel();
            if (label.startsWith(DukascopyParams.ORDER_PREFIX_LABEL)) {
                int id = getOrderIDFromLabel(label);
                orderMap.put(id, order);
            }
        }
    }

    private int getOrderIDFromLabel(String label) {
        String idName = label.substring(DukascopyParams.ORDER_PREFIX_LABEL.length());
        return Integer.parseInt(idName);
    }
}
