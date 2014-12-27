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


import com.dukascopy.api.system.IClient;
import com.dukascopy.api.system.JFAuthenticationException;
import com.dukascopy.api.system.JFVersionException;

import com.jforex.dzplugin.ZorroLogger;
import com.jforex.dzplugin.config.Configuration;
import com.jforex.dzplugin.config.ReturnCodes;

public class LoginHandler {

    private final IClient client;

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
            ZorroLogger.log("Invalid login credentials!");
        } catch (JFVersionException e) {
            ZorroLogger.log("Invalid JForex version!");
        } catch (Exception e) {
            ZorroLogger.log("Login exc: " + e.getMessage());
        }
        return client.isConnected() ? ReturnCodes.LOGIN_OK : ReturnCodes.LOGIN_FAIL;
    }

    public int logout() {
        client.disconnect();
        return ReturnCodes.LOGOUT_OK;
    }
}
