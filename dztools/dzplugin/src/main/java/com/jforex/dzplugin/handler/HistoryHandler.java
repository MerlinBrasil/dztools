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

import com.dukascopy.api.Filter;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.dzplugin.utils.DateTimeUtils;

public class HistoryHandler {

    private final IHistory history;

    private final static Logger logger = LogManager.getLogger(HistoryHandler.class);

    public HistoryHandler(IHistory history) {
        this.history = history;
    }

    public List<IBar> getBars(Instrument instrument,
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
