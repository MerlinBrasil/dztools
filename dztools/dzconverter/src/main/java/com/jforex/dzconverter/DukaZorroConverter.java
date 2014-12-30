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

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.Instrument;

public class DukaZorroConverter {

    private ArgumentHandler argHandler;
    private ClientHandler clientHandler;
    private Instrument instrument;
    private DZConverterConfig cfg;
    private int year;

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

        if (!isInstrumentOK())
            logErrorAndQuit("Provided instrument is invalid!");

        if (!isYearOK())
            logErrorAndQuit("Provided year is invalid!");

        cfg = ConfigFactory.create(DZConverterConfig.class);

        if (!isCacheDirOK())
            logErrorAndQuit("Provided cachedir " + cfg.cachedir() + " is missing the data folder " + getDataDirectory()
                    + ". Check the DZConverterConfig.properties cachedir entry!");

        if (!isLoginOK())
            logErrorAndQuit("Login failed!");

        clientHandler.startConversionStrategy(this);
    }

    public boolean isInstrumentOK() {
        instrument = argHandler.getInstrument();
        return instrument == null ? false : true;
    }

    public boolean isYearOK() {
        year = argHandler.getYear();
        return year == 0 ? false : true;
    }

    public boolean isCacheDirOK() {
        String cacheDir = cfg.cachedir();
        File file = new File(cacheDir + "\\" + getDataDirectory());
        return !file.isDirectory() ? false : true;
    }

    public boolean isLoginOK() {
        clientHandler = new ClientHandler(cfg);
        return !clientHandler.isLoginOK() ? false : true;
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

    private String getDataDirectory() {
        return instrument.getPrimaryJFCurrency().getCurrencyCode() + instrument.getSecondaryJFCurrency().getCurrencyCode() + "\\" + year;
    }
}
