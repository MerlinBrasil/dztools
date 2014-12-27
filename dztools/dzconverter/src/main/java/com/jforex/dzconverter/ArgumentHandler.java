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
