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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.jforex.dzplugin.config.Configuration;

public class CloseOrderTask implements Callable<IOrder> {

    private final IOrder order;
    private final double amount;

    private final static Logger logger = LogManager.getLogger(CloseOrderTask.class);

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
            logger.error("order close exc: " + e.getMessage());
        }
        return order;
    }
}
