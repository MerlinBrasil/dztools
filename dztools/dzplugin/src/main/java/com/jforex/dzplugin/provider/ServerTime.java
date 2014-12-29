package com.jforex.dzplugin.provider;

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


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.jforex.dzplugin.config.Configuration;
import com.jforex.dzplugin.task.NTPTimeSynchTask;

import com.dukascopy.api.ITick;

public class ServerTime {

    private final IPriceEngine priceEngine;
    private final NTPTimeSynchTask ntpSynchTask;
    private SnychState snychState;
    private long serverTimeSynchTimer;
    private Future<Long> ntpFuture;
    private ExecutorService singleThreadExecutor;
    private long startNTPTime;
    private long ntpTimerStart;

    private enum SnychState {
        NTP,
        TICK,
        SYSTEM
    }

    public ServerTime(IPriceEngine priceEngine) {
        this.priceEngine = priceEngine;
        ntpSynchTask = new NTPTimeSynchTask();
        singleThreadExecutor = Executors.newSingleThreadExecutor();
    }

    private void init() {
        snychState = SnychState.SYSTEM;
        serverTimeSynchTimer = System.currentTimeMillis();
    }

    private void startNTPSynch() {
        ntpFuture = singleThreadExecutor.submit(ntpSynchTask);
    }

    public long get() {
        if (snychState == SnychState.NTP) {
            return startNTPTime + (System.currentTimeMillis() - ntpTimerStart);
        }
        return 0L;
    }

    private long getLatestTickTime() {
        ITick tick = priceEngine.getLatestTick();
        if (tick == null)
            return 0L;
        return tick.getTime();
    }

    private boolean isServerTimeSynchTriggered() {
        if ((System.currentTimeMillis() - serverTimeSynchTimer) >= Configuration.SERVERTIME_SYNC_MILLIS) {
            serverTimeSynchTimer = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    private void synchServerTime() {

        //        long NTPTime = getGMTTime();
        //        if (NTPTime != 0L) {
        //            logger.info("Successfully synched server time via NTP provider. Time: " + formatDateTime(NTPTime));
        //            serverTimeState = ServerTimeState.NTP;
        //            return;
        //        }
        //        logger.warn("Server time synch via NTP failed! rying to synch server time with latest tick...");
        //        NTPTime = getLatestTickTime();
        //        if (NTPTime != 0L) {
        //            logger.info("Successfully synched server time via latest tick. Time: " + formatDateTime(NTPTime));
        //            serverTimeState = ServerTimeState.TICK;
        //            return;
        //        }
        //        logger.warn("Server time synch via latest tick time failed! Using system time now as fallback(maybe incorrect)!");
        //        NTPTime = System.currentTimeMillis();
        //        serverTimeState = ServerTimeState.SYSTEM;
        //        logger.debug("Server time after system time synching is now: " + formatDateTime(NTPTime));
    }

}
