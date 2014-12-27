package com.jforex.dzplugin;

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


import java.time.Period;

import com.jforex.dzplugin.provider.IPriceEngine;
import com.jforex.dzplugin.provider.ITickConsumer;
import com.jforex.dzplugin.provider.PriceEngine;

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
