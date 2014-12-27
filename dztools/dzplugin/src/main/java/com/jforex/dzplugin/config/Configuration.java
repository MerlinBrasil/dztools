package com.jforex.dzplugin.config;

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
