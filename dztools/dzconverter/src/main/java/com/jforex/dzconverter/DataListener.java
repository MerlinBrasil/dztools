package com.jforex.dzconverter;

/*
 * #%L
 * dzconverter
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
