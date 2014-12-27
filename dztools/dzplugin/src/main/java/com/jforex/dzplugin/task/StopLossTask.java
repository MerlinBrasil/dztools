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

public class StopLossTask implements Callable<IOrder> {

    private final IOrder order;
    private final double SLPrice;

    private final static Logger logger = LogManager.getLogger(StopLossTask.class);

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
            logger.error("Setting SL to " + SLPrice + " failed: " + e.getMessage());
        }
        return order;
    }
}
