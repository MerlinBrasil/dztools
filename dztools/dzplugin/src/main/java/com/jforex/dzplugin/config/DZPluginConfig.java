package com.jforex.dzplugin.config;

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

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources({ "classpath:DZPluginConfig.properties" })
public interface DZPluginConfig extends Config {
    @DefaultValue(".\\Plugin\\dukascopy\\.cache")
    String CACHE_DIR();

    @DefaultValue("zorro")
    String ORDER_PREFIX_LABEL();

    @DefaultValue("3f")
    double DEFAULT_SLIPPAGE();

    @DefaultValue("1000")
    long CONNECTION_WAIT_TIME();

    @DefaultValue("10")
    int CONNECTION_RETRIES();

    @DefaultValue("https://www.dukascopy.com/client/demo/jclient/jforex.jnlp")
    String CONNECT_URL_DEMO();

    @DefaultValue("https://www.dukascopy.com/client/live/jclient/jforex.jnlp")
    String CONNECT_URL_LIVE();

    @DefaultValue("time-a.nist.gov")
    String NTP_TIME_SERVER_URL();
}
