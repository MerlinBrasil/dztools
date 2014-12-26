package com.jforex.dzconverter;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;
import com.dukascopy.api.system.JFAuthenticationException;
import com.dukascopy.api.system.JFVersionException;

public class ClientHandler {

    private static IClient client;
    private PropertiesHandler propHandler;

    private final static Logger logger = LogManager.getLogger(ClientHandler.class);

    public ClientHandler(PropertiesHandler propHandler) {
        this.propHandler = propHandler;
    }

    public boolean isLoginOK() {
        if (!isClientSetupOK())
            return false;
        logger.info("Start login...");

        try {
            client.connect("https://www.dukascopy.com/client/demo/jclient/jforex.jnlp", propHandler.getUser(), propHandler.getPassword());
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
        client.setCacheDirectory(new File(propHandler.getCacheDir()));
        return true;
    }
}
