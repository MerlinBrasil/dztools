package com.jforex.dukazorrobridge.handler;

import java.util.Set;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.system.IClient;
import com.jforex.dukazorrobridge.ZorroLogger;
import com.jforex.dukazorrobridge.config.Configuration;
import com.jforex.dukazorrobridge.config.ReturnCodes;

public class SubscriptionHandler {

    private final IClient client;

    public SubscriptionHandler(IClient client) {
        this.client = client;
    }

    public int subscribe(Set<Instrument> instruments) {
        client.setSubscribedInstruments(instruments);

        waitForSubscription(instruments);
        if (!client.getSubscribedInstruments().containsAll(instruments)) {
            ZorroLogger.log("Subscription for assets failed!");
            return ReturnCodes.ASSET_UNAVAILABLE;
        }
        return ReturnCodes.ASSET_AVAILABLE;
    }

    private void waitForSubscription(Set<Instrument> instruments) {
        for (int i = 0; i < Configuration.SUBSCRIPTION_WAIT_TIME_RETRIES; ++i) {
            if (client.getSubscribedInstruments().containsAll(instruments))
                break;
            try {
                Thread.sleep(Configuration.SUBSCRIPTION_WAIT_TIME);
            } catch (InterruptedException e) {
                ZorroLogger.log("Thread exc: " + e.getMessage());
                break;
            }
        }
    }
}
