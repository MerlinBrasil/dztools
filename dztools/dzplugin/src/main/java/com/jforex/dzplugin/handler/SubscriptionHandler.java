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

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.system.IClient;
import com.jforex.dzplugin.ZorroLogger;
import com.jforex.dzplugin.config.Configuration;
import com.jforex.dzplugin.config.ReturnCodes;
import com.jforex.dzplugin.provider.AccountInfo;
import com.jforex.dzplugin.provider.IPriceEngine;
import com.jforex.dzplugin.utils.InstrumentUtils;

public class SubscriptionHandler {

    private final IClient client;
    private final AccountInfo accountInfo;
    private final IPriceEngine priceEngine;

    private final static Logger logger = LogManager.getLogger(SubscriptionHandler.class);

    public SubscriptionHandler(IClient client,
                               IPriceEngine priceEngine,
                               AccountInfo accountInfo) {
        this.client = client;
        this.accountInfo = accountInfo;
        this.priceEngine = priceEngine;
    }

    public int doSubscribeAsset(String instrumentName) {
        Instrument toSubscribeInstrument = InstrumentUtils.getByName(instrumentName);
        if (toSubscribeInstrument == null)
            return ReturnCodes.ASSET_UNAVAILABLE;

        Set<Instrument> instruments = new HashSet<Instrument>();
        instruments.add(toSubscribeInstrument);
        // we must subscribe to cross instrument also for margin calculations
        Instrument crossInstrument = InstrumentUtils.getfromCurrencies(accountInfo.getCurrency(), toSubscribeInstrument.getPrimaryJFCurrency());
        if (crossInstrument != null) {
            logger.debug("crossInstrument: " + crossInstrument);
            instruments.add(crossInstrument);
        }

        int subscriptionResult = subscribe(instruments);
        if (subscriptionResult == ReturnCodes.ASSET_AVAILABLE)
            priceEngine.initInstruments(instruments);

        logger.info("Subscription for " + toSubscribeInstrument + " successful.");
        return subscriptionResult;
    }

    private int subscribe(Set<Instrument> instruments) {
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
                logger.error("Thread exc: " + e.getMessage());
                ZorroLogger.indicateError();
                break;
            }
        }
    }
}
