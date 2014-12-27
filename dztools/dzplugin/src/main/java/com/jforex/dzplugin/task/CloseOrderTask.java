package com.jforex.dzplugin.task;

import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

import com.jforex.dzplugin.ZorroLogger;
import com.jforex.dzplugin.config.Configuration;

public class CloseOrderTask implements Callable<IOrder> {

    private final IOrder order;
    private final double amount;

    public CloseOrderTask(IOrder order,
                          double amount) {
        this.order = order;
        this.amount = amount;
    }

    @Override
    public IOrder call() {
        boolean isPartialCloseRequest = amount < order.getAmount();
        try {
            if (!isPartialCloseRequest) {
                order.close();
                order.waitForUpdate(Configuration.ORDER_UPDATE_WAITTIME, IOrder.State.CLOSED);
            }
            else
                order.close(amount);
        } catch (JFException e) {
            ZorroLogger.logSystem("orderclose exc: " + e.getMessage());
        }
        return order;
    }
}
