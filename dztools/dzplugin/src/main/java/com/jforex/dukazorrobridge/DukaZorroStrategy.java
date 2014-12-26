package com.jforex.dukazorrobridge;

import java.time.Period;

import com.jforex.dukazorrobridge.provider.IPriceEngine;
import com.jforex.dukazorrobridge.provider.ITickConsumer;
import com.jforex.dukazorrobridge.provider.PriceEngine;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;

public class DukaZorroStrategy implements IStrategy {

    private IContext context;
    private IPriceEngine priceEngine;
    private ITickConsumer priceEngineConsumer;

    public IContext getContext() {
        return context;
    }

    public IPriceEngine getPriceEngine() {
        return priceEngine;
    }

    public void onStart(IContext context) throws JFException {
        this.context = context;
        priceEngine = new PriceEngine(context.getHistory());
        priceEngineConsumer = (ITickConsumer) priceEngine;
    }

    public void onTick(Instrument instrument,
                       ITick tick) {
        priceEngineConsumer.onTick(instrument, tick);
    }

    public void onStop() {

    }

    public void onBar(Instrument instrument,
                      Period period,
                      IBar bar1,
                      IBar bar2) {
    }

    public void onBar(com.dukascopy.api.Instrument instrument, com.dukascopy.api.Period period, IBar askBar, IBar bidBar) throws JFException {
        // TODO Auto-generated method stub

    }

    public void onMessage(IMessage message) throws JFException {
        // TODO Auto-generated method stub

    }

    public void onAccount(IAccount account) throws JFException {
        // TODO Auto-generated method stub

    }
}
