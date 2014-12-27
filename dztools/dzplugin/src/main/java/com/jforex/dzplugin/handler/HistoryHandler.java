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

import com.dukascopy.api.IBar;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

import com.jforex.dzplugin.ZorroLogger;
import com.jforex.dzplugin.utils.DateTimeUtils;

public class HistoryHandler {

    private final IHistory history;

    public HistoryHandler(IHistory history) {
        this.history = history;
    }

    public List<IBar> getBars(Instrument instrument,
                              Period period,
                              OfferSide offerSide,
                              long startDateTimeRaw,
                              long endDateTimeRaw) {
        long startDateTimeRounded = getStartDateTimeRounded(period, startDateTimeRaw);
        long endDateTimeRounded = getEndDateTimeRounded(instrument, period, endDateTimeRaw);
        ZorroLogger.log("Fetching " + period + " bars");
        String dateFrom = ZorroLogger.formatDateTime(startDateTimeRounded);
        String dateTo = ZorroLogger.formatDateTime(endDateTimeRounded);
        ZorroLogger.log("From " + dateFrom);
        ZorroLogger.log("To " + dateTo);

        List<IBar> bars = null;
        try {
            bars = history.getBars(instrument, period, offerSide, startDateTimeRounded, endDateTimeRounded);
        } catch (JFException e) {
            ZorroLogger.log("getBars exc: " + e.getMessage());
            return new ArrayList<IBar>();
        }
        Collections.reverse(bars);
        ZorroLogger.log("Fetched " + bars.size() + " bars.");

        return bars;
    }

    private long getStartDateTimeRounded(Period period,
                                         long startDateTime) {
        return DateTimeUtils.roundTimeToPeriod(period, startDateTime);
    }

    private long getEndDateTimeRounded(Instrument instrument,
                                       Period period,
                                       long endDateTimeRaw) {
        long endDateTimeRounded = 0L;
        try {
            long prevBarDateTime = history.getPreviousBarStart(period, history.getLastTick(instrument).getTime());
            if (endDateTimeRaw >= prevBarDateTime)
                endDateTimeRounded = prevBarDateTime;
            else
                endDateTimeRounded = DateTimeUtils.roundTimeToPeriod(period, endDateTimeRaw);
        } catch (JFException e) {
            ZorroLogger.log("getPreviousBarStart exc: " + e.getMessage());
        }
        return endDateTimeRounded;
    }
}
