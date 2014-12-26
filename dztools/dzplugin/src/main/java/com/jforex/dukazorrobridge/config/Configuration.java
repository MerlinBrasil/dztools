package com.jforex.dukazorrobridge.config;

public class Configuration {

    public static final String connectURLForDEMO = "https://www.dukascopy.com/client/demo/jclient/jforex.jnlp";
    public static final String connectURLForLIVE = "https://www.dukascopy.com/client/live/jclient/jforex.jnlp";
    public static final String NTP_TIME_SERVER = "time-a.nist.gov";
    public static final long CONNECTION_WAIT_TIME = 1000;
    public static final int CONNECTION_RETRIES = 10;
    public static final long SUBSCRIPTION_WAIT_TIME = 200;
    public static final int SUBSCRIPTION_WAIT_TIME_RETRIES = 10;
    public static final long SUBMIT_WAIT_TIME = 100;
    public static final int SUBMIT_WAIT_RETRIES = 40;
    public static final long ORDER_UPDATE_WAITTIME = 3000;
    public static final int NTP_TIMEOUT = 3000;
}
