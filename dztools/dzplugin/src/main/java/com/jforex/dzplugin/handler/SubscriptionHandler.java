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

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.system.IClient;
import com.jforex.dzplugin.ZorroLogger;
import com.jforex.dzplugin.config.Configuration;
import com.jforex.dzplugin.config.ReturnCodes;

public class SubscriptionHandler {

    private final IClient client;

    private final static Logger logger = LogManager.getLogger(SubscriptionHandler.class);

    public SubscriptionHandler(IClient client) {
        this.client = client;
    }

    public int subscribe(Set<Instrument> instruments) {
        client.setSubscribedInstruments(instruments);

        waitForSubscription(instruments);
        if (!client.getSubscribedInstruments().containsAll(instruments)) {
            ZorroLogger.showError(logger, "Subscription for assets failed!");
            return ReturnCodes.ASSET_UNAVAILABLE;
        }
        return ReturnCodes.ASSET_AVAILABLE;
    }

    private void waitForSubscription(Set<Instrument> instruments) {
        for (int i = 0; i < Configuration.SUBSCRIPTION_WAIT_TIME_RETRIES; ++i) {
            if (client.getSubscribedInstruments().containsAll(instruments))
                break;
            try {
                Thread.sleep(Configuration.SUBSCRIPTION_WAIT_TIME);
            } catch (InterruptedException e) {
                ZorroLogger.inicateError(logger, "Thread exc: " + e.getMessage());
                break;
            }
        }
    }
}
