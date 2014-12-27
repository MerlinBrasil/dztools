package com.jforex.dzplugin.task;

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


import java.util.concurrent.Callable;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;

import com.jforex.dzplugin.ZorroLogger;
import com.jforex.dzplugin.config.Configuration;
import com.jforex.dzplugin.config.DukascopyParams;

public class SubmitOrderTask implements Callable<IOrder> {

    private final IEngine engine;
    private final String orderLabel;
    private final Instrument instrument;
    private final OrderCommand cmd;
    private final double amount;
    private final double SLPrice;

    public SubmitOrderTask(IEngine engine,
                           String orderLabel,
                           Instrument instrument,
                           OrderCommand cmd,
                           double amount,
                           double SLPrice) {
        this.engine = engine;
        this.orderLabel = orderLabel;
        this.instrument = instrument;
        this.cmd = cmd;
        this.amount = amount;
        this.SLPrice = SLPrice;
    }

    @Override
    public IOrder call() {
        IOrder order = null;
        try {
            order = engine.submitOrder(orderLabel,
                                       instrument,
                                       cmd,
                                       amount,
                                       0f,
                                       DukascopyParams.DEFAULT_SLIPPAGE,
                                       SLPrice,
                                       0f,
                                       0L,
                                       "");
            order.waitForUpdate(Configuration.ORDER_UPDATE_WAITTIME, IOrder.State.FILLED);
        } catch (JFException e) {
            ZorroLogger.logSystem("submitOrder exception: " + e.getMessage());
        }
        return order;
    }
}
