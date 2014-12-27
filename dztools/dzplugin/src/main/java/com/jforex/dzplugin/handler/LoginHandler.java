package com.jforex.dzplugin.handler;

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
