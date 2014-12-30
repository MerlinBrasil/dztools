package com.jforex.dzconverter;

/*
 * #%L
 * dzconverter
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

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;
import com.dukascopy.api.system.JFAuthenticationException;
import com.dukascopy.api.system.JFVersionException;

public class ClientHandler {

    private static IClient client;
    private DZConverterConfig cfg;

    private final static Logger logger = LogManager.getLogger(ClientHandler.class);

    public ClientHandler(DZConverterConfig cfg) {
        this.cfg = cfg;
    }

    public boolean isLoginOK() {
        if (!isClientSetupOK())
            return false;
        logger.info("Start login...");

        try {
            client.connect("https://www.dukascopy.com/client/demo/jclient/jforex.jnlp", cfg.user(), cfg.password());
            for (int i = 0; i < 10 && !client.isConnected(); ++i)
                Thread.sleep(200);
        } catch (JFAuthenticationException e) {
            logger.error("Invalid login credentials!");
        } catch (JFVersionException e) {
            logger.error("Invalid JForex version!");
        } catch (Exception e) {
            logger.error("Login exc: " + e.getMessage());
        }
        return client.isConnected();
    }

    public void disconnect() {
        if (client != null)
            client.disconnect();
    }

    public void startConversionStrategy(DukaZorroConverter dukaZorroConverter) {
        logger.info("Starting conversion...");
        ConversionStrategy strategy = new ConversionStrategy(dukaZorroConverter);
        client.startStrategy(strategy);
    }

    private boolean isClientSetupOK() {
        try {
            client = ClientFactory.getDefaultInstance();
        } catch (ClassNotFoundException e) {
            logger.error("setUpClient fails with " + e.getMessage());
            return false;
        } catch (IllegalAccessException e) {
            logger.error("setUpClient fails with " + e.getMessage());
            return false;
        } catch (InstantiationException e) {
            logger.error("setUpClient fails with " + e.getMessage());
            return false;
        }
        client.setCacheDirectory(new File(cfg.cachedir()));
        return true;
    }
}
