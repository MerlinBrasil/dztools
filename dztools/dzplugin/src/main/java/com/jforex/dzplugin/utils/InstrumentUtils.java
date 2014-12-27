package com.jforex.dzplugin.utils;

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

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;

public class InstrumentUtils {

    private static final HashMap<String, Instrument> assetNameToInstrumentMap = new HashMap<String, Instrument>();

    public static Instrument getfromString(String instrumentString) {
        String uppercaseInstrumentString = instrumentString.toUpperCase();
        if (Instrument.isInverted(uppercaseInstrumentString))
            return Instrument.fromInvertedString(uppercaseInstrumentString);
        return Instrument.fromString(uppercaseInstrumentString);
    }

    public static Instrument getfromCurrencies(ICurrency currencyA,
                                               ICurrency currencyB) {
        return getfromString(currencyA + Instrument.getPairsSeparator() + currencyB);
    }

    public static Instrument getByName(String instrumentName) {
        if (assetNameToInstrumentMap.containsKey(instrumentName))
            return assetNameToInstrumentMap.get(instrumentName);

        return getFromNewName(instrumentName);
    }

    private static synchronized Instrument getFromNewName(String instrumentName) {
        Instrument instrument = InstrumentUtils.getfromString(instrumentName);
        if (instrument == null)
            return null;

        assetNameToInstrumentMap.put(instrumentName, instrument);
        return instrument;
    }
}
