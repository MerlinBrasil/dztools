package com.jforex.dzconverter;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PropertiesHandler {

    private Properties properties;
    private String propertiesPath;
    private String user;
    private String password;
    private String cacheDir;

    private final static Logger logger = LogManager.getLogger(PropertiesHandler.class);

    public PropertiesHandler(String propertiesPath) {
        this.propertiesPath = propertiesPath;
        this.properties = new Properties();
    }

    public boolean arePropertiesValid() {
        if (!isLoadPropertiesOK(readPropertiesStream()))
            return false;
        user = properties.getProperty("user");
        password = properties.getProperty("password");
        cacheDir = properties.getProperty("cachedir");
        return arePropertiesSet();
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getCacheDir() {
        return cacheDir;
    }

    private BufferedInputStream readPropertiesStream() {
        BufferedInputStream stream = null;
        try {
            stream = new BufferedInputStream(new FileInputStream(propertiesPath));
        } catch (FileNotFoundException e) {
            logger.error("Could not find " + propertiesPath + " file!");
        }
        return stream;
    }

    private boolean isLoadPropertiesOK(BufferedInputStream stream) {
        if (stream == null)
            return false;
        try {
            properties.load(stream);
            stream.close();
        } catch (IOException e) {
            logger.error("IOException: " + e.getMessage());
            return false;
        }
        return true;
    }

    private boolean arePropertiesSet() {
        if (user == null) {
            logger.error("Invalid username provided! Check " + propertiesPath + " file.");
            return false;
        }
        if (password == null) {
            logger.error("Invalid password provided! Check " + propertiesPath + " file.");
            return false;
        }
        if (cacheDir == null) {
            logger.error("Invalid cache directory provided! Check " + propertiesPath + " file.");
            return false;
        }
        return true;
    }
}
