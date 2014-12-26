package com.jforex.dukazorrobridge.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import com.dukascopy.api.IDataService;
import com.dukascopy.api.ITimeDomain;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;
import com.jforex.dukazorrobridge.ZorroLogger;
import com.jforex.dukazorrobridge.config.Configuration;

public class DateTimeUtils {

    private static final HashMap<Integer, Period> minuteToPeriodMap;

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

    public static double getOLEDateFromDate(Date date) {
        double oleDate = 25569; // dates from 1899 to 1970
        oleDate += (double) date.getTime() / (1000f * 3600f * 24f) + 1e-8;
        return oleDate;
    }

    public static Date getDateFromOLEDate(double oaDate) {
        Date date = new Date();
        date.setTime((long) ((oaDate - 25569) * 24 * 3600 * 1000));
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
            ZorroLogger.log("Unable to get GMT time: " + e.getMessage());
        }
        ZorroLogger.log("Using System time now!");
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
            ZorroLogger.log("getOfflineTimes exc: " + e.getMessage());
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
