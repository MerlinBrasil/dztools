package com.jforex.dzplugin.task;

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
