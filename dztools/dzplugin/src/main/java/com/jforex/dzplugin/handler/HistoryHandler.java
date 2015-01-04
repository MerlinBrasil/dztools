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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        logger.debug("startDate " + DateTimeUtils.formatOLETime(startDate) +
                " endDate: " + DateTimeUtils.formatOLETime(endDate) +
                "nTicks " + nTicks + " tickMinutes " + tickMinutes);
        Instrument instrument = InstrumentUtils.getByName(instrumentName);
        if (instrument == null)
            return ReturnCodes.HISTORY_FAIL;

        Period period = DateTimeUtils.getPeriodFromMinutes(tickMinutes);
        if (period == null) {
            logger.error("Invalid tickMinutes: " + tickMinutes);
            ZorroLogger.indicateError();
            return ReturnCodes.HISTORY_FAIL;
        }
        long endDateTimeRounded = getEndDateTimeRounded(instrument, period, DateTimeUtils.getMillisFromOLEDate(endDate));
        long startDateTimeRounded = endDateTimeRounded - (nTicks - 1) * period.getInterval();

        List<IBar> bars = getBars(instrument, period, startDateTimeRounded, endDateTimeRounded);
        int numTicks = bars.size();
        logger.debug("numTicks " + numTicks);
        if (numTicks == 0)
            return ReturnCodes.HISTORY_FAIL;

        fillTICKs(bars, tickParams);
        return numTicks;
    }

    private void fillTICKs(List<IBar> bars,
                           double tickParams[]) {
        int tickParamsIndex = 0;
        Collections.reverse(bars);
        for (int i = 0; i < bars.size(); ++i) {
            IBar bar = bars.get(i);
            tickParams[tickParamsIndex] = bar.getOpen();
            tickParams[tickParamsIndex + 1] = bar.getClose();
            tickParams[tickParamsIndex + 2] = bar.getHigh();
            tickParams[tickParamsIndex + 3] = bar.getLow();
            tickParams[tickParamsIndex + 4] = DateTimeUtils.getUTCTimeFromBar(bar);
            tickParamsIndex += 5;
        }
    }

    private List<IBar> getBars(Instrument instrument,
                               Period period,
                               long startTime,
                               long endTime) {
        String dateFrom = DateTimeUtils.formatDateTime(startTime);
        String dateTo = DateTimeUtils.formatDateTime(endTime);
        logger.debug("Trying to fetch " + period + " bars from " + dateFrom + " to " + dateTo + " for " + instrument);

        List<IBar> bars = new ArrayList<IBar>();
        try {
            history.getBar(instrument, period, OfferSide.ASK, 0);
            long prevBarStart = history.getPreviousBarStart(period, history.getLastTick(instrument).getTime());
            if (prevBarStart < endTime)
                endTime = prevBarStart;

            bars = history.getBars(instrument, period, OfferSide.ASK, Filter.WEEKENDS, startTime, endTime);
        } catch (JFException e) {
            logger.error("getBars exception: " + e.getMessage());
            ZorroLogger.indicateError();
        }
        logger.debug("Fetched " + bars.size() + " bars from " + dateFrom + " to " + dateTo + " for " + instrument);
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
        HistoryConfig historyConfig = ConfigFactory.create(HistoryConfig.class);
        String instrumentName = historyConfig.Asset();
        String savePath = historyConfig.Path();
        int startYear = historyConfig.StartYear();
        int endYear = historyConfig.EndYear();

        Instrument instrument = InstrumentUtils.getByName(instrumentName);
        if (instrument == null)
            return ReturnCodes.HISTORY_DOWNLOAD_FAIL;

        int numYears = endYear - startYear + 1;
        for (int i = 0; i < numYears; ++i) {
            int currentYear = startYear + i;

            ZorroLogger.log("Load " + instrument + " for " + currentYear + "...");
            List<IBar> bars = fetchBarsForYear(instrument, currentYear);
            if (bars.size() == 0) {
                ZorroLogger.log("Load " + instrument + " for " + currentYear + " failed!");
                return ReturnCodes.HISTORY_DOWNLOAD_FAIL;
            }
            ZorroLogger.log("Load " + instrument + " for " + currentYear + " OK");

            String fileName = getBarFileName(instrument, currentYear, savePath);
            if (!isWriteBarsToFileOK(fileName, bars))
                return ReturnCodes.HISTORY_DOWNLOAD_FAIL;
        }
        return ReturnCodes.HISTORY_DOWNLOAD_OK;
    }

    private List<IBar> fetchBarsForYear(Instrument instrument,
                                        int year) {
        long startTime = DateTimeUtils.getUTCYearStartTime(year);
        long endTime = DateTimeUtils.getUTCYearEndTime(year);
        return getBars(instrument, Period.ONE_MIN, startTime, endTime);
    }

    private boolean isWriteBarsToFileOK(String fileName,
                                        List<IBar> bars) {
        Collections.reverse(bars);
        logger.info("Writing " + fileName + " ...");
        BarFileWriter barFileWriter = new BarFileWriter(fileName, bars);
        if (!barFileWriter.isWriteBarsToTICKsFileOK())
            return false;

        logger.info("Writing " + fileName + " OK");
        return true;
    }

    private String getBarFileName(Instrument instrument,
                                  int year,
                                  String histSavePath) {
        return histSavePath + "\\" + InstrumentUtils.getNameWODash(instrument) + "_" + year + ".bar";
    }
}
