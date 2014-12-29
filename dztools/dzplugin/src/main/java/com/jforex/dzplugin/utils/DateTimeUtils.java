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

import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.dzplugin.ZorroLogger;
import com.jforex.dzplugin.config.Configuration;

import com.dukascopy.api.IDataService;
import com.dukascopy.api.ITimeDomain;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;

public class DateTimeUtils {

    private static final HashMap<Integer, Period> minuteToPeriodMap;
    private static final int DAYS_EPOCH = 25569;

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

    private IDataService dataService;
    private long startGMTTime;
    private long timerStart;

    public DateTimeUtils(IDataService dataService) {
        this.dataService = dataService;
        startGMTTime = getGMTTime();
        timerStart = System.currentTimeMillis();
    }

    public static double getOLEDateFromMillis(long millis) {
        return DAYS_EPOCH + (double) millis / (1000f * 3600f * 24f);
    }

    public static double getOLEDateFromMillisRounded(long millis) {
        return getOLEDateFromMillis(millis) + 1e-8;
    }

    public static Date getDateFromOLEDate(double oaDate) {
        Date date = new Date();
        date.setTime((long) ((oaDate - DAYS_EPOCH) * 24 * 3600 * 1000));
        return date;
    }

    public long getGMTTime() {
        NTPUDPClient timeClient = new NTPUDPClient();
        timeClient.setDefaultTimeout(Configuration.NTP_TIMEOUT);
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(Configuration.NTP_TIME_SERVER);
            TimeInfo timeInfo = timeClient.getTime(inetAddress);
            return timeInfo.getMessage().getTransmitTimeStamp().getTime();
        } catch (IOException e) {
            logger.warn("Unable to get GMT time: " + e.getMessage());
        }
        logger.warn("Using System time now!");
        return System.currentTimeMillis();
    }

    public long getServerTime() {
        return startGMTTime + (System.currentTimeMillis() - timerStart);
    }

    public boolean isMarketOffline() {
        long serverTime = getServerTime();
        Set<ITimeDomain> offlines = getOfflineTimes(serverTime, serverTime + Period.ONE_MIN.getInterval());
        if (offlines == null)
            return true;

        for (ITimeDomain offline : offlines)
            if (serverTime >= offline.getStart() && serverTime <= offline.getEnd())
                return true;
        return false;
    }

    public static String formatDateTime(long dateTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return dateFormat.format(new Date(dateTime));
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

    public static Period getPeriodFromMinutes(int minutes) {
        return minuteToPeriodMap.get(minutes);
    }

    public static long roundTimeToPeriod(Period barPeriod,
                                         long rawTime) {
        long roundedTime = rawTime;
        if (barPeriod == Period.DAILY)
            roundedTime = truncateHours(rawTime);
        else if (barPeriod == Period.ONE_HOUR)
            roundedTime = truncateMinutes(rawTime);
        else if (barPeriod == Period.THIRTY_MINS)
            roundedTime = roundToMinutes(rawTime, 30);
        else if (barPeriod == Period.FIFTEEN_MINS)
            roundedTime = roundToMinutes(rawTime, 15);
        else if (barPeriod == Period.FIVE_MINS)
            roundedTime = roundToMinutes(rawTime, 5);
        else if (barPeriod == Period.ONE_MIN)
            roundedTime = truncateSeconds(rawTime);

        return roundedTime;
    }

    private static Calendar getCalendarFromTime(long dateTime) {
        Date date = new Date(dateTime);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar;
    }

    private static long roundToMinutes(long rawTime,
                                       int minutes) {
        Calendar calendar = getCalendarFromTime(rawTime);
        int unroundedMinutes = calendar.get(Calendar.MINUTE);
        int mod = unroundedMinutes % minutes;
        calendar.add(Calendar.MINUTE, mod < (minutes / 2) + 1 ? -mod : (minutes - mod));

        return truncateSeconds(calendar.getTimeInMillis());
    }

    private static long truncateHours(long rawTime) {
        Calendar calendar = getCalendarFromTime(truncateMinutes(rawTime));
        calendar.set(Calendar.HOUR, 0);

        return calendar.getTimeInMillis();
    }

    private static long truncateMinutes(long rawTime) {
        Calendar calendar = getCalendarFromTime(truncateSeconds(rawTime));
        calendar.set(Calendar.MINUTE, 0);

        return calendar.getTimeInMillis();
    }

    private static long truncateSeconds(long rawTime) {
        Calendar calendar = getCalendarFromTime(rawTime);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }
}
