package com.jforex.dzplugin.provider;

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


import java.util.HashMap;
import java.util.Set;

import com.dukascopy.api.IHistory;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;

import com.jforex.dzplugin.ZorroLogger;

public class PriceEngine implements IPriceEngine, ITickConsumer {

    private IHistory history;
    private final HashMap<Instrument, ITick> lastTickMap;

    public PriceEngine(IHistory history) {
        this.history = history;
        lastTickMap = new HashMap<Instrument, ITick>();
    }

    @Override
    public void initInstruments(Set<Instrument> instruments) {
        for (Instrument instrument : instruments) {
            ITick historyTick = getLastTickFromHistory(instrument);
            if (historyTick != null)
                lastTickMap.put(instrument, historyTick);
        }
    }

    @Override
    public void onTick(Instrument instrument,
                       ITick tick) {
        lastTickMap.put(instrument, tick);
    }

    @Override
    public ITick getLastTick(Instrument instrument) {
        if (lastTickMap.containsKey(instrument))
            return lastTickMap.get(instrument);

        return getLastTickFromHistory(instrument);
    }

    private ITick getLastTickFromHistory(Instrument instrument) {
        try {
            return history.getLastTick(instrument);
        } catch (JFException e) {
            ZorroLogger.log("Last tick for " + instrument + " not availavle!");
            return null;
        }
    }

    @Override
    public double getBid(Instrument instrument) {
        return getPrice(instrument, OfferSide.BID);
    }

    @Override
    public double getAsk(Instrument instrument) {
        return getPrice(instrument, OfferSide.ASK);
    }

    private double getPrice(Instrument instrument,
                            OfferSide offerSide) {
        ITick tick = getLastTick(instrument);
        if (tick == null)
            return 0f;
        return offerSide == OfferSide.ASK ? tick.getAsk() : tick.getBid();
    }

    @Override
    public double getSpread(Instrument instrument) {
        ITick tick = getLastTick(instrument);
        return tick.getAsk() - tick.getBid();
    }

    @Override
    public double getRounded(Instrument instrument,
                             double rawPrice) {
        double roundingFactor = Math.pow(10, instrument.getPipScale() + 1);
        return Math.round(rawPrice * roundingFactor) / roundingFactor;
    }
}
