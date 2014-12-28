package com.jforex.dzplugin.handler;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.dzplugin.ZorroLogger;
import com.jforex.dzplugin.config.Configuration;
import com.jforex.dzplugin.config.ReturnCodes;

import com.dukascopy.api.system.IClient;
import com.dukascopy.api.system.JFAuthenticationException;
import com.dukascopy.api.system.JFVersionException;

public class LoginHandler {

    private final IClient client;

    private final static Logger logger = LogManager.getLogger(LoginHandler.class);

    public LoginHandler(IClient client) {
        this.client = client;
    }

    public int login(String User,
                     String Pwd,
                     String Pin) {
        try {
            if (Pin.isEmpty())
                client.connect(Configuration.connectURLForDEMO, User, Pwd);
            else
                client.connect(Configuration.connectURLForLIVE, User, Pwd, Pin);

            for (int i = 0; i < Configuration.CONNECTION_RETRIES && !client.isConnected(); ++i)
                Thread.sleep(Configuration.CONNECTION_WAIT_TIME);
        } catch (JFAuthenticationException e) {
            ZorroLogger.showError(logger, "Invalid login credentials!");
        } catch (JFVersionException e) {
            ZorroLogger.showError(logger, "Invalid JForex version!");
        } catch (Exception e) {
            ZorroLogger.indicateError(logger, "Login exc: " + e.getMessage());
        }
        return client.isConnected() ? ReturnCodes.LOGIN_OK : ReturnCodes.LOGIN_FAIL;
    }

    public int logout() {
        logger.debug("Trying to logout...");
        client.disconnect();
        logger.debug("Logout OK");
        return ReturnCodes.LOGOUT_OK;
    }
}
