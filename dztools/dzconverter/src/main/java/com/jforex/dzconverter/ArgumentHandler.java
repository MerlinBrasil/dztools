package com.jforex.dzconverter;

import com.dukascopy.api.Instrument;

public class ArgumentHandler {

    private final String instrumentString;
    private final String yearString;

    private Instrument instrument = null;
    private int year = 0;

    public ArgumentHandler(String[] args) {
        this.instrumentString = args[0];
        this.yearString = args[1];

        constructInstrument();
        constructYear();
    }

    private void constructInstrument() {
        instrument = getInstrumentfromString(instrumentString);
    }

    private void constructYear() {
        year = Integer.parseInt(yearString);
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public int getYear() {
        return year;
    }

    private Instrument getInstrumentfromString(String instrumentString) {
        String uppercaseInstrumentString = instrumentString.toUpperCase();
        if (Instrument.isInverted(uppercaseInstrumentString))
            return Instrument.fromInvertedString(uppercaseInstrumentString);
        return Instrument.fromString(uppercaseInstrumentString);
    }
}
