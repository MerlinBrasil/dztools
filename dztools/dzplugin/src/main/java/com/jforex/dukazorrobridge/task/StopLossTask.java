package com.jforex.dukazorrobridge.task;

import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.jforex.dukazorrobridge.ZorroLogger;
import com.jforex.dukazorrobridge.config.Configuration;

public class StopLossTask implements Callable<IOrder> {

    private final IOrder order;
    private final double SLPrice;

    public StopLossTask(IOrder order,
                        double SLPrice) {
        this.order = order;
        this.SLPrice = SLPrice;
    }

    @Override
    public IOrder call() {
        try {
            order.setStopLossPrice(SLPrice);
            order.waitForUpdate(Configuration.ORDER_UPDATE_WAITTIME);
        } catch (JFException e) {
            ZorroLogger.log("Setting SL to " + SLPrice + " failed: " + e.getMessage());
        }
        return order;
    }
}
