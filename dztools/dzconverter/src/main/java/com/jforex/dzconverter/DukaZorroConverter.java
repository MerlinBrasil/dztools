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


import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.Instrument;

public class DukaZorroConverter {

    private static ArgumentHandler argHandler;
    private static PropertiesHandler propHandler;
    private ClientHandler clientHandler;
    private static Instrument instrument;
    private static int year;

    private final static Logger logger = LogManager.getLogger(DukaZorroConverter.class);

    public static void main(String[] args) {
        if (args.length != 2) {
            logger.error("Invalid arguments! Usage: <instrument> <year>\nExample: EUR//USD 2014");
            System.exit(0);
        }
        new DukaZorroConverter().start(args);
    }

    public void start(String[] args) {
        argHandler = new ArgumentHandler(args);

        instrument = argHandler.getInstrument();
        if (instrument == null)
            logErrorAndQuit("Provided instrument is invalid!");
        year = argHandler.getYear();
        if (year == 0)
            logErrorAndQuit("Provided year is invalid!");

        propHandler = new PropertiesHandler("config.properties");
        if (!propHandler.arePropertiesValid())
            logErrorAndQuit("Properties are invalid!");
        String cacheDir = propHandler.getCacheDir();

        validateCacheDir(cacheDir);

        clientHandler = new ClientHandler(propHandler);
        if (!clientHandler.isLoginOK())
            logErrorAndQuit("Login failed!");
        clientHandler.startConversionStrategy(this);
    }

    public void disconnect() {
        if (clientHandler != null)
            clientHandler.disconnect();
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public int getYear() {
        return year;
    }

    public static void log(String msg) {
        logger.info(msg);
    }

    public void quit() {
        disconnect();
        System.exit(0);
    }

    public void logErrorAndQuit(String msg) {
        logger.error(msg);
        quit();
    }

    private void validateCacheDir(String cacheDir) {
        String dataDirectory = instrument.getPrimaryJFCurrency().getCurrencyCode() + instrument.getSecondaryJFCurrency().getCurrencyCode() + "\\" + year;
        File file = new File(cacheDir + "\\" + dataDirectory);
        if (!file.isDirectory())
            logErrorAndQuit("Provided cachedir " + cacheDir + " is missing the data folder " + dataDirectory
                    + ". Check the config.properties cachedir entry!");
    }
}
