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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.dzplugin.config.Configuration;
import com.jforex.dzplugin.task.NTPTimeSynchTask;

import com.dukascopy.api.ITick;

public class ServerTimeProvider {

    private final IPriceEngine priceEngine;
    private final NTPTimeSynchTask ntpSynchTask;
    private SynchState snychState;
    private long serverSynchTimer;
    private Future<Long> ntpFuture;
    private final ExecutorService singleThreadExecutor;
    private long startNTPTime;
    private long ntpTimer;

    private final static Logger logger = LogManager.getLogger(ServerTimeProvider.class);

    private enum SynchState {
        NTP,
        TICK
    }

    public ServerTimeProvider(IPriceEngine priceEngine) {
        this.priceEngine = priceEngine;
        ntpSynchTask = new NTPTimeSynchTask();
        singleThreadExecutor = Executors.newSingleThreadExecutor();

        init();
    }

    private void init() {
        snychState = SynchState.TICK;
        serverSynchTimer = System.currentTimeMillis();
        startNTPSynch();
        // at init wait for result
        getNTPFuture();
    }

    private void startNTPSynch() {
        logger.debug("Starting ntpSynchTask...");
        ntpFuture = singleThreadExecutor.submit(ntpSynchTask);
    }

    public long get() {
        if (snychState == SynchState.NTP)
            return startNTPTime + (System.currentTimeMillis() - ntpTimer);
        // We are using tick based server time here
        // Check if synch is ongoing
        if (ntpFuture.isDone()) {
            logger.debug("ntpSynchTask result available.");
            startNTPTime = getNTPFuture();
            if (startNTPTime != 0L) {
                // NTP time now available
                snychState = SynchState.NTP;
                ntpTimer = System.currentTimeMillis();
                logger.debug("Switched to SynchState NTP");
                return startNTPTime;
            }
            else {
                logger.debug("Synch taks failed, reset synch timer");
                serverSynchTimer = System.currentTimeMillis();
            }
        }
        // No synch ongoing, check if synch is triggered
        if (isServerTimeSynchTriggered()) {
            logger.debug("Server time synching was triggered.");
            startNTPSynch();
            serverSynchTimer = System.currentTimeMillis();
        }
        long latestTickTime = getLatestTickTime();
        if (latestTickTime == 0L) {
            logger.warn("latestTickTime is invalid, server time currently not available, fallback to system time!");
            return System.currentTimeMillis();
        }
        return latestTickTime;
    }

    private long getNTPFuture() {
        try {
            return ntpFuture.get();
        } catch (InterruptedException e) {
            logger.error("InterruptedException: " + e.getMessage());
        } catch (ExecutionException e) {
            logger.error("ExecutionException: " + e.getMessage());
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
        if ((System.currentTimeMillis() - serverSynchTimer) >= Configuration.SERVERTIME_SYNC_MILLIS)
            return true;
        return false;
    }
}
