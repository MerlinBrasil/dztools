package com.jforex.dzplugin.provider;

import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;

public interface ITickConsumer {

    abstract void onTick(Instrument instrument,
                         ITick tick);
}