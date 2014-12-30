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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.dzplugin.DukaZorroBridge;
import com.jforex.dzplugin.ZorroLogger;
import com.jforex.dzplugin.config.ReturnCodes;
import com.jforex.dzplugin.provider.AccountInfo;
import com.jforex.dzplugin.utils.DateTimeUtils;
import com.jforex.dzplugin.utils.InstrumentUtils;

import com.dukascopy.api.Filter;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

public class HistoryHandler {

    private final IHistory history;
    private final AccountInfo accountInfo;

    private final static Logger logger = LogManager.getLogger(HistoryHandler.class);

    public HistoryHandler(DukaZorroBridge dukaZorroBridge) {
        this.history = dukaZorroBridge.getContext().getHistory();
        this.accountInfo = dukaZorroBridge.getAccountInfo();
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
        if (instrument == null) {
            return ReturnCodes.HISTORY_FAIL;
        }

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
        if (bars.size() == 0)
            return ReturnCodes.HISTORY_FAIL;

        int tickParamsIndex = 0;
        int arraySizeForAllBars = 5 * bars.size();
        int maxIndex = nTicks;
        if (arraySizeForAllBars <= tickParams.length)
            maxIndex = bars.size();

        logger.debug("bars size " + bars.size() + " maxIndex " + maxIndex);

        for (int i = 0; i < maxIndex; ++i) {
            IBar bar = bars.get(i);
            tickParams[tickParamsIndex] = bar.getOpen();
            tickParams[tickParamsIndex + 1] = bar.getClose();
            tickParams[tickParamsIndex + 2] = bar.getHigh();
            tickParams[tickParamsIndex + 3] = bar.getLow();
            tickParams[tickParamsIndex + 4] = DateTimeUtils.getOLEDateFromMillisRounded(bar.getTime());
            tickParamsIndex += 5;
        }
        return maxIndex;
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
        logger.debug("Trying to fetch " + nTicks + " " + period + " bars from " + dateFrom + " to " + dateTo + " for " + instrument);

        List<IBar> bars = new ArrayList<IBar>();
        try {
            bars = history.getBars(instrument, period, offerSide, Filter.WEEKENDS, nTicks, endDateTimeRounded, 0);
        } catch (JFException e) {
            logger.error("getBars exc: " + e.getMessage());
        }
        Collections.reverse(bars);
        logger.debug("Fetched " + bars.size() + " bars.");

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
}
