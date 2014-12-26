package com.jforex.dukazorrobridge.utils;

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
