package com.jforex.dzplugin.utils;

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.SimpleTimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.dzplugin.DukaZorroBridge;
import com.jforex.dzplugin.ZorroLogger;
import com.jforex.dzplugin.provider.ServerTimeProvider;

import com.dukascopy.api.IDataService;
import com.dukascopy.api.ITimeDomain;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;

public class DateTimeUtils {

    private static final HashMap<Integer, Period> minuteToPeriodMap;
    private static final int DAYS_SINCE_UTC_EPOCH = 25569;

    private final static Logger logger = LogManager.getLogger(DateTimeUtils.class);

    static {
        minuteToPeriodMap = new HashMap<Integer, Period>();

        minuteToPeriodMap.put(1440, Period.DAILY);
        minuteToPeriodMap.put(60, Period.ONE_HOUR);
        minuteToPeriodMap.put(30, Period.THIRTY_MINS);
        minuteToPeriodMap.put(15, Period.FIFTEEN_MINS);
        minuteToPeriodMap.put(5, Period.FIVE_MINS);
        minuteToPeriodMap.put(1, Period.ONE_MIN);
        minuteToPeriodMap.put(0, Period.TICK);
    }

    private final IDataService dataService;
    private final ServerTimeProvider serverTimeProvider;

    public DateTimeUtils(DukaZorroBridge dukaZorroBridge) {
        this.dataService = dukaZorroBridge.getContext().getDataService();
        this.serverTimeProvider = dukaZorroBridge.getServerTimeProvider();
    }

    public static double getOLEDateFromMillis(long millis) {
        return DAYS_SINCE_UTC_EPOCH + (double) millis / (1000f * 3600f * 24f);
    }

    public static double getOLEDateFromMillisRounded(long millis) {
        return getOLEDateFromMillis(millis) + 1e-8;
    }

    public static long getMillisFromOLEDate(double oaDate) {
        Date date = new Date();
        date.setTime((long) ((oaDate - DAYS_SINCE_UTC_EPOCH) * 24 * 3600 * 1000));
        return date.getTime();
    }

    public boolean isMarketOffline() {
        long serverTime = serverTimeProvider.get();
        Set<ITimeDomain> offlines = getOfflineTimes(serverTime, serverTime + Period.ONE_MIN.getInterval());
        if (offlines == null)
            return true;

        for (ITimeDomain offline : offlines)
            if (serverTime >= offline.getStart() && serverTime <= offline.getEnd())
                return true;
        return false;
    }

    private Set<ITimeDomain> getOfflineTimes(long startTime,
                                             long endTime) {
        Set<ITimeDomain> offlineTimes = null;
        try {
            offlineTimes = dataService.getOfflineTimeDomains(startTime, endTime);
        } catch (JFException e) {
            ZorroLogger.indicateError(logger, "getOfflineTimes exc: " + e.getMessage());
        }
        return offlineTimes;
    }

    public static String formatDateTime(long dateTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        dateFormat.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
        return dateFormat.format(new Date(dateTime));
    }

    public static String formatOLETime(double oleTime) {
        long dateTime = getMillisFromOLEDate(oleTime);
        return formatDateTime(dateTime);
    }

    public static Period getPeriodFromMinutes(int minutes) {
        return minuteToPeriodMap.get(minutes);
    }
}
