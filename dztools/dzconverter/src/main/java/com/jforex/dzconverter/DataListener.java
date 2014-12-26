package com.jforex.dzconverter;

import java.util.List;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.LoadingDataListener;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

public class DataListener implements LoadingDataListener {

    private final List<ZorroBar> bars;

    public DataListener(List<ZorroBar> bars) {
        this.bars = bars;
    }

    public void newTick(Instrument instrument, long time, double ask, double bid, double askVol, double bidVol) {
        // TODO Auto-generated method stub
    }

    public void newBar(Instrument instrument, Period period, OfferSide side, long time, double open, double close, double low, double high, double vol) {
        ZorroBar zorroBar = new ZorroBar(time, open, close, high, low);
        bars.add(zorroBar);
    }
}
