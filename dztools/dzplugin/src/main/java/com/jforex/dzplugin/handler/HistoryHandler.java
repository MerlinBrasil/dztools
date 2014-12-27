package com.jforex.dzplugin.handler;

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
