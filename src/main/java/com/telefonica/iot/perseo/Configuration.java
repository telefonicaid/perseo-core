/**
* Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
*
* This file is part of perseo-core project.
*
* perseo-core is free software: you can redistribute it and/or modify it under the terms of the GNU
* General Public License version 2 as published by the Free Software Foundation.
*
* perseo-core is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
* implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
* for more details.
*
* You should have received a copy of the GNU General Public License along with perseo-core. If not, see
* http://www.gnu.org/licenses/.
*
* For those usages not covered by the GNU General Public License please contact with
* iot_support at tid dot es
*/

package com.telefonica.iot.perseo;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brox
 */
public final class Configuration {

    private Configuration() {
        super();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    private static final String DEFAULT_PERSEO_FE_URL = "http://127.0.0.1:9090";
    private static final long DEFAULT_MAX_AGE = 60000;
    private static final String PERSEO_FE_URL_ENV = "PERSEO_FE_URL";
    private static final String PERSEO_MAX_AGE_ENV = "MAX_AGE";

    private static final Properties PROPERTIES = new Properties();
    private static final String PATH = "/etc/perseo-core.properties";
    private static final String ACTION_URL_PROP = "action.url";
    private static final String MAX_AGE_PROP = "rule.max_age";

    private static String actionRule;
    private static long maxAge;

    static {
        LOGGER.debug("Configuration init: " + reload());
    }

    /**
     * Reads configuration file, and so refresh configuration.
     *
     * @return true if success, false otherwise
     */
    public static synchronized boolean reload() {

        LOGGER.info("Configuration is being reloaded");
        InputStream stream;
        Long default_max_age;
        String default_url;

        // Check configuration file. If exist, set as default configuration for perseo-core
        try {
            PROPERTIES.clear();
            stream = new FileInputStream(PATH);
            PROPERTIES.load(stream);
            stream.close();
            default_url = PROPERTIES.getProperty(ACTION_URL_PROP);
            // Valid url check
            if (!Utils.isValidURL(default_url)) {
                LOGGER.error("Invalid configuration value in " + PATH + " file " + ACTION_URL_PROP + ": " + default_url);
                return false;
            }

            // Valid Number Check
            try {
                default_max_age = Long.parseLong(PROPERTIES.getProperty(MAX_AGE_PROP));
            } catch (NumberFormatException nfe) {
                LOGGER.error("Invalid configuration value in " + PATH + " file " + MAX_AGE_PROP + ": " + nfe);
                return false;
            }

        } catch (IOException e) {

            // No config file. Set basic default values
            default_url = DEFAULT_PERSEO_FE_URL;
            default_max_age = DEFAULT_MAX_AGE;
        }

        // Get Persep-fe url from env var if exist, else default
        String perseo_fe_url = System.getenv(PERSEO_FE_URL_ENV);
        perseo_fe_url = perseo_fe_url != null ? perseo_fe_url : default_url;
        // Validate URL
        if (Utils.isValidURL(perseo_fe_url)) {
            actionRule = perseo_fe_url + "/actions/do";
        } else {
            LOGGER.error("Invalid configuration environment var value for " + PERSEO_FE_URL_ENV + ": " + perseo_fe_url);
            return false;
        }
        LOGGER.debug("actionRule: " + actionRule);

        // Get MAX_AGE from env var if exist, else default
        String max_age_string = System.getenv(PERSEO_MAX_AGE_ENV);
        // Check maxAge numerical value
        try {
            maxAge = max_age_string != null ? Long.parseLong(max_age_string) : default_max_age;
        } catch (NumberFormatException nfe) {
            LOGGER.error("Invalid configuration environment var value for " + PERSEO_MAX_AGE_ENV + ": " + nfe);
            return false;
        }

        return true;
    }

    /**
     *
     * @return URL to send actions (fired events by a rule)
     */
    public static synchronized String getActionURL() {
        return actionRule;
    }

    /**
     *
     * @return max age for keeping a rule that exist before a complete refresh
     * but it is not included in the new set. In case of error returns 0, so the
     * unexpected rules will be deleted immediately.
     */
    public static synchronized long getMaxAge() {
        return maxAge;
    }
}
