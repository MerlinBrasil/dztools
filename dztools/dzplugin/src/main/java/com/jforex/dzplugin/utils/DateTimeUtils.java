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
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IDataService;
import com.dukascopy.api.ITimeDomain;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;
import com.jforex.dzplugin.ZorroLogger;
import com.jforex.dzplugin.provider.ServerTimeProvider;

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

    public DateTimeUtils(IDataService dataService,
                         ServerTimeProvider serverTimeProvider) {
        this.dataService = dataService;
        this.serverTimeProvider = serverTimeProvider;
    }

    public static double getOLEDateFromMillis(long millis) {
        return DAYS_SINCE_UTC_EPOCH + (double) millis / (1000f * 3600f * 24f);
    }

    public static double getOLEDateFromMillisRounded(long millis) {
        return getOLEDateFromMillis(millis) + 1e-8;
    }

    public static long getMillisFromOLEDate(double oleDate) {
        Date date = new Date();
        date.setTime((long) ((oleDate - DAYS_SINCE_UTC_EPOCH) * 24 * 3600 * 1000));
        return date.getTime();
    }

    public boolean isMarketOffline() {
        long serverTime = serverTimeProvider.get();
        Set<ITimeDomain> offlines = getOfflineTimes(serverTime, serverTime + Period.ONE_MIN.getInterval());
        return offlines == null ? true : isServerTimeInOfflineDomains(serverTime, offlines);
    }

    private boolean isServerTimeInOfflineDomains(long serverTime,
                                                 Set<ITimeDomain> offlines) {
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
            logger.error("getOfflineTimes exc: " + e.getMessage());
            ZorroLogger.indicateError();
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

    public static long getUTCYearStartTime(int year) {
        return getUTCTime(year, 0, 1, 0, 0, 0);
    }

    public static long getUTCYearEndTime(int year) {
        return getUTCTime(year, 11, 31, 23, 59, 0);
    }

    public static long getUTCTime(int year,
                                  int month,
                                  int day,
                                  int hour,
                                  int min,
                                  int sec) {
        GregorianCalendar calendar = new GregorianCalendar(year, month, day, hour, min, sec);
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        return calendar.getTimeInMillis();
    }

    public static double getUTCTimeFromBar(IBar bar) {
        return getOLEDateFromMillisRounded(bar.getTime());
    }
}
