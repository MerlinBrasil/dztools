package com.jforex.dzplugin.task;

/*
 * #%L
 * dzplugin
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
