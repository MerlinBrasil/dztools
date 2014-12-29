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

import java.util.Set;

import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;

public interface IPriceEngine {

    abstract ITick getLastTick(Instrument instrument);

    abstract ITick getLatestTick();

    abstract double getBid(Instrument instrument);

    abstract double getAsk(Instrument instrument);

    abstract double getSpread(Instrument instrument);

    abstract void initInstruments(Set<Instrument> instruments);

    abstract double getRounded(Instrument instrument,
                               double rawPrice);
}
