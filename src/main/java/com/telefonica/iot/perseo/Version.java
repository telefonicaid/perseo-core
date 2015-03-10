/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.telefonica.iot.perseo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brox
 */
public final class Version {

    private Version() {

    }
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    private static final Properties POM = new Properties();

    static {
        try {
            InputStream stream;
            stream = Version.class.getResourceAsStream("/version.properties");
            POM.load(stream);
            stream.close();
        } catch (IOException ioe) {
            LOGGER.error(ioe.getMessage());
            POM.setProperty("version", "UNKNOWN");
        }

    }

    /**
     * Returns the version of perseo-core.
     * @return String
     */
    public static String get() {
        return (String) POM.get("version");
    }
}
