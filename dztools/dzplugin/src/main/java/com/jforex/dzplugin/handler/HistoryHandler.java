package com.jforex.dzplugin.handler;

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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.Filter;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.dzplugin.ZorroLogger;
import com.jforex.dzplugin.config.HistoryConfig;
import com.jforex.dzplugin.config.ReturnCodes;
import com.jforex.dzplugin.utils.DateTimeUtils;
import com.jforex.dzplugin.utils.InstrumentUtils;

public class HistoryHandler {

    private final IHistory history;
    private FileOutputStream outStream;
    private ByteBuffer bbf;
    private HistoryConfig historyConfig;
    private String histSavePath;
    private int histStartYear;
    private int histEndYear;
    private String histFileName;

    private final static Logger logger = LogManager.getLogger(HistoryHandler.class);

    public HistoryHandler(IHistory history) {
        this.history = history;
    }

    public int doBrokerHistory(String instrumentName,
                               double startDate,
                               double endDate,
                               int tickMinutes,
                               int nTicks,
                               double tickParams[]) {
        logger.debug("startDate " + DateTimeUtils.formatOLETime(startDate) + " endDate: " + DateTimeUtils.formatOLETime(endDate));
        logger.debug("nTicks " + nTicks + " tickMinutes " + tickMinutes);
        Instrument instrument = InstrumentUtils.getByName(instrumentName);
        if (instrument == null)
            return ReturnCodes.HISTORY_FAIL;

        Period period = DateTimeUtils.getPeriodFromMinutes(tickMinutes);
        if (period == null) {
            ZorroLogger.indicateError(logger, "Invalid tickMinutes: " + tickMinutes);
            return ReturnCodes.HISTORY_FAIL;
        }
        List<IBar> bars = getBars(instrument,
                                  period,
                                  OfferSide.ASK,
                                  DateTimeUtils.getMillisFromOLEDate(endDate),
                                  nTicks);
        int numTicks = bars.size();
        if (numTicks == 0)
            return ReturnCodes.HISTORY_FAIL;

        logger.debug("Bars size " + numTicks);
        int tickParamsIndex = 0;
        for (int i = 0; i < numTicks; ++i) {
            IBar bar = bars.get(i);
            tickParams[tickParamsIndex] = bar.getOpen();
            tickParams[tickParamsIndex + 1] = bar.getClose();
            tickParams[tickParamsIndex + 2] = bar.getHigh();
            tickParams[tickParamsIndex + 3] = bar.getLow();
            tickParams[tickParamsIndex + 4] = DateTimeUtils.getOLEDateFromMillisRounded(bar.getTime());
            tickParamsIndex += 5;
        }
        return numTicks;
    }

    private List<IBar> getBars(Instrument instrument,
                               Period period,
                               OfferSide offerSide,
                               long endDateTimeRaw,
                               int nTicks) {
        long endDateTimeRounded = getEndDateTimeRounded(instrument, period, endDateTimeRaw);
        long startDateTimeRounded = endDateTimeRounded - (nTicks - 1) * period.getInterval();
        String dateFrom = DateTimeUtils.formatDateTime(startDateTimeRounded);
        String dateTo = DateTimeUtils.formatDateTime(endDateTimeRounded);
        ZorroLogger.log("Trying to fetch " + nTicks + " " + period + " bars from " + dateFrom + " to " + dateTo + " for " + instrument);

        List<IBar> bars = new ArrayList<IBar>();
        try {
            bars = history.getBars(instrument, period, offerSide, Filter.WEEKENDS, nTicks, endDateTimeRounded, 0);
        } catch (JFException e) {
            logger.error("getBars exc: " + e.getMessage());
        }
        Collections.reverse(bars);
        ZorroLogger.log("Fetched " + bars.size() + " bars.");

        return bars;
    }

    private long getEndDateTimeRounded(Instrument instrument,
                                       Period period,
                                       long endDateTimeRaw) {
        long endDateTimeRounded = 0L;
        try {
            endDateTimeRounded = history.getPreviousBarStart(period, endDateTimeRaw);
            logger.debug("endDateTimeRaw " + DateTimeUtils.formatDateTime(endDateTimeRaw)
                    + " endDateTimeRounded " + DateTimeUtils.formatDateTime(endDateTimeRounded));

        } catch (JFException e) {
            logger.error("getPreviousBarStart exc: " + e.getMessage());
        }
        return endDateTimeRounded;
    }

    public int doHistoryDownload() {
        historyConfig = ConfigFactory.create(HistoryConfig.class);

        String instrumentName = historyConfig.Asset();
        histSavePath = historyConfig.Path();
        histStartYear = historyConfig.StartYear();
        histEndYear = historyConfig.EndYear();
        int numYears = histEndYear - histStartYear + 1;

        Instrument instrument = InstrumentUtils.getByName(instrumentName);
        if (instrument == null) {
            ZorroLogger.log("Asset " + instrumentName + " is invalid!");
            return ReturnCodes.HISTORY_DOWNLOAD_FAIL;
        }
        for (int i = 0; i < numYears; ++i) {
            int currentYear = histStartYear + i;
            ZorroLogger.log("Load " + instrument + " for " + currentYear + "...");

            List<IBar> bars = new ArrayList<IBar>();
            try {
                bars = history.getBars(instrument, Period.ONE_MIN, OfferSide.ASK, Filter.WEEKENDS, getYearStartTime(currentYear), getYearEndTime(currentYear));
            } catch (JFException e) {
                ZorroLogger.log("History exception: " + e.getMessage());
                return ReturnCodes.HISTORY_DOWNLOAD_FAIL;
            }
            processYearBars(bars, instrument, currentYear);
        }

        return ReturnCodes.HISTORY_DOWNLOAD_OK;
    }

    private void processYearBars(List<IBar> bars,
                                 Instrument instrument,
                                 int year) {
        Collections.reverse(bars);
        ZorroLogger.log("Load " + instrument + " for " + year + " OK");
        histFileName = getBarFileName(instrument, year);
        writeBarsToTICKsFile(bars);
    }

    private long getYearStartTime(int year) {
        GregorianCalendar calendarStart = new GregorianCalendar(year, 0, 1, 0, 0, 0);
        calendarStart.setTimeZone(TimeZone.getTimeZone("UTC"));
        return calendarStart.getTimeInMillis();
    }

    private long getYearEndTime(int year) {
        GregorianCalendar calendarEnd = new GregorianCalendar(year, 11, 31, 23, 59, 0);
        calendarEnd.setTimeZone(TimeZone.getTimeZone("UTC"));
        return calendarEnd.getTimeInMillis();
    }

    private void writeBarsToTICKsFile(List<IBar> bars) {
        logger.info("Writing " + histFileName + " ...");

        initByteBuffer(bars.size());
        writeBarsToBuffer(bars, bbf);
        writeBufferToFile(bbf);

        logger.info("Writing " + histFileName + " OK.");
    }

    private void writeBarsToBuffer(List<IBar> bars,
                                   ByteBuffer bbf) {
        for (IBar bar : bars) {
            bbf.putFloat((float) bar.getOpen());
            bbf.putFloat((float) bar.getClose());
            bbf.putFloat((float) bar.getHigh());
            bbf.putFloat((float) bar.getLow());
            bbf.putDouble(DateTimeUtils.getOLEDateFromMillisRounded(bar.getTime()));
        }
    }

    private void initByteBuffer(int barsCount) {
        bbf = ByteBuffer.allocate(24 * barsCount);
        bbf.order(ByteOrder.LITTLE_ENDIAN);
    }

    private void writeBufferToFile(ByteBuffer bbf) {
        try {
            outStream = new FileOutputStream(histFileName);
            outStream.write(bbf.array(), 0, bbf.limit());
            outStream.close();
        } catch (FileNotFoundException e) {
            ZorroLogger.log("FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            ZorroLogger.log("IOException while writing TICKs file! " + e.getMessage());
        }
    }

    private String getBarFileName(Instrument instrument,
                                  int year) {
        return histSavePath + "\\" + instrument.getPrimaryJFCurrency().getCurrencyCode() +
                instrument.getSecondaryJFCurrency().getCurrencyCode() + "_" + year + ".bar";
    }
}
