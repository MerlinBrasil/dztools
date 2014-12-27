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


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

public class ConversionStrategy implements IStrategy {

    private final DukaZorroConverter dukaZorroConverter;
    private IHistory history;
    private final Instrument instrument;
    private final Period period;
    private final int year;

    private final static Logger logger = LogManager.getLogger(ConversionStrategy.class);

    public ConversionStrategy(DukaZorroConverter dukaZorroConverter) {
        this.dukaZorroConverter = dukaZorroConverter;
        this.instrument = dukaZorroConverter.getInstrument();
        this.year = dukaZorroConverter.getYear();
        this.period = Period.ONE_MIN;
    }

    public void onStart(final IContext context) {
        history = context.getHistory();
        List<ZorroBar> bars = new ArrayList<ZorroBar>();

        DataListener dataListener = new DataListener(bars);
        ProgressListener progressListener = new ProgressListener(dukaZorroConverter, bars);

        long startTime = getStartTime();
        long endTime = getEndTime();

        String startTimeString = formatDateTime(startTime);
        String endTimeString = formatDateTime(endTime);
        logger.info("Fetchin bars from " + startTimeString + " to " + endTimeString);

        try {
            history.readBars(instrument, period, OfferSide.ASK, startTime, endTime, dataListener, progressListener);
        } catch (JFException e) {
            logErrorAndExit("History exception: " + e.getMessage());
        }
    }

    private long getStartTime() {
        return new GregorianCalendar(year, 0, 1, 0, 0, 0).getTimeInMillis();
    }

    private long getEndTime() {
        long endTime = new GregorianCalendar(year, 11, 31, 23, 59, 0).getTimeInMillis();
        try {
            long prevBarDateTime = history.getPreviousBarStart(period, history.getLastTick(instrument).getTime());
            if (prevBarDateTime < endTime)
                endTime = prevBarDateTime;
        } catch (JFException e) {
            logErrorAndExit("getPreviousBarStart exception: " + e.getMessage());
        }
        return endTime;
    }

    private void logErrorAndExit(String errorMsg) {
        logger.error(errorMsg);
        dukaZorroConverter.disconnect();
        System.exit(0);
    }

    private String formatDateTime(long dateTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return dateFormat.format(new Date(dateTime));
    }

    public void onTick(Instrument instrument, ITick tick) throws JFException {
        // TODO Auto-generated method stub
    }

    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        // TODO Auto-generated method stub
    }

    public void onMessage(IMessage message) throws JFException {
        // TODO Auto-generated method stub
    }

    public void onAccount(IAccount account) throws JFException {
        // TODO Auto-generated method stub
    }

    public void onStop() throws JFException {
        // TODO Auto-generated method stub
    }
}
