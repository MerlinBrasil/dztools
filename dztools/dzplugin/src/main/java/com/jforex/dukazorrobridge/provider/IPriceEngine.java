package com.jforex.dukazorrobridge.provider;

import java.util.Set;

import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;

public interface IPriceEngine {

    abstract ITick getLastTick(Instrument instrument);

    abstract double getBid(Instrument instrument);

    abstract double getAsk(Instrument instrument);

    abstract double getSpread(Instrument instrument);

    abstract void initInstruments(Set<Instrument> instruments);

    abstract double getRounded(Instrument instrument,
                               double rawPrice);
}
