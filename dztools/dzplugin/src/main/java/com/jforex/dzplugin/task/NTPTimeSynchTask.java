package com.jforex.dzplugin.task;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.dzplugin.config.Configuration;

public class NTPTimeSynchTask implements Callable<Long> {

    private final NTPUDPClient timeClient;
    private InetAddress inetAddress;
    private TimeInfo timeInfo;

    private final static Logger logger = LogManager.getLogger(NTPTimeSynchTask.class);

    public NTPTimeSynchTask() {
        timeClient = new NTPUDPClient();
        init();
    }

    private void init() {
        timeClient.setDefaultTimeout(Configuration.NTP_TIMEOUT);
        try {
            inetAddress = InetAddress.getByName(Configuration.NTP_TIME_SERVER_URL);
        } catch (UnknownHostException e) {
            logger.error("NTP server url " + Configuration.NTP_TIME_SERVER_URL + " is unkown!");
        }
    }

    @Override
    public Long call() {
        if (inetAddress == null)
            return 0L;
        try {
            timeInfo = timeClient.getTime(inetAddress);
        } catch (IOException e) {
            logger.warn("Unable to get time from NTP server: " + e.getMessage());
            return 0L;
        }
        return timeInfo.getMessage().getTransmitTimeStamp().getTime();
    }
}
