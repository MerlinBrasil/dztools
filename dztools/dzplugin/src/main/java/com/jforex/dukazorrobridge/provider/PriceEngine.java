package com.jforex.dukazorrobridge.provider;

import java.util.HashMap;
import java.util.Set;

import com.dukascopy.api.IHistory;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.jforex.dukazorrobridge.ZorroLogger;

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
