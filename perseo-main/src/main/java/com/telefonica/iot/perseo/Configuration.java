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
    private static final long DEFAULT_INTERNAL_TIMER_MSEC_RESOLUTION = 10;

    private static final String PERSEO_FE_URL_ENV = "PERSEO_FE_URL";
    private static final String PERSEO_MAX_AGE_ENV = "MAX_AGE";
    private static final String PERSEO_INTERNAL_TIMER_MSEC_RESOLUTION_ENV = "INTERNAL_TIMER_MSEC_RESOLUTION";
    private static final String ESPER_BASIC_FILTER_FALLBACK_ENV = "PERSEO_ESPER_BASIC_FILTER_FALLBACK_ENABLED";
    private static final String EMPTY_NUMERIC_CASTS_ENV = "PERSEO_ESPER_EMPTY_NUMERIC_CASTS_ENABLED";
    private static final String ACTION_HTTP_HARDENING_ENV = "PERSEO_ACTION_HTTP_HARDENING_ENABLED";
    private static final String RULE_UPDATE_SAFETY_ENV = "PERSEO_RULE_UPDATE_SAFETY_ENABLED";

    private static final Properties PROPERTIES = new Properties();
    private static final String PATH = "/etc/perseo-core.properties";
    private static final String ACTION_URL_PROP = "action.url";
    private static final String MAX_AGE_PROP = "rule.max_age";
    private static final String INTERNAL_TIMER_MSEC_RESOLUTION = "internal_timer_msec_resolution";

    private static String actionRule;
    private static long maxAge;
    private static long internalTimerMsecResolution;
    private static int actionConnectTimeout = 5000;
    private static int actionReadTimeout = 10000;
    private static boolean esperBasicFilterFallbackEnabled;
    private static boolean emptyNumericCastsEnabled;
    private static boolean actionHttpHardeningEnabled;
    private static boolean ruleUpdateSafetyEnabled;

    static {
        LOGGER.debug(String.format("Configuration init: %s",reload()));
    }

    /**
     * Reads configuration file, and so refresh configuration.
     *
     * @return true if success, false otherwise
     */
    public static synchronized boolean reload() {

        LOGGER.info("Configuration is being reloaded");
        InputStream stream;
        String defaultMaxAge;
        String defaultInternalTimerMsecResolution;
        String defaultURL;
        String actionPath = "/actions/do";

        // Check configuration file. If exist, set as default configuration for perseo-core
        try {
            PROPERTIES.clear();
            stream = new FileInputStream(PATH);
            PROPERTIES.load(stream);
            stream.close();
            defaultURL = PROPERTIES.getProperty(ACTION_URL_PROP);
            defaultMaxAge = PROPERTIES.getProperty(MAX_AGE_PROP);
            defaultInternalTimerMsecResolution = PROPERTIES.getProperty(INTERNAL_TIMER_MSEC_RESOLUTION);
        } catch (IOException e) {
            // No config file. Set basic default values
            defaultURL = DEFAULT_PERSEO_FE_URL;
            defaultMaxAge = String.valueOf(DEFAULT_MAX_AGE);
            defaultInternalTimerMsecResolution = String.valueOf(DEFAULT_INTERNAL_TIMER_MSEC_RESOLUTION);
        }

        // Add 'http://' if necessary (backward compatibility with existing deployments)
        String perseoFeURLEnv = System.getenv(PERSEO_FE_URL_ENV);
        if (perseoFeURLEnv != null && !perseoFeURLEnv.toLowerCase().matches("^(http|https)://.*$")) {
            perseoFeURLEnv =  "http://" + perseoFeURLEnv;
        }
        // Get Persep-fe url from env var if exist, else default
        perseoFeURLEnv = perseoFeURLEnv != null ? perseoFeURLEnv : defaultURL;
        // Validate URL
        if (Utils.isValidURL(perseoFeURLEnv)) {
            // Add actions/do path if perseoFeURLEnv not contains it yet
            actionRule = perseoFeURLEnv.contains(actionPath) ? perseoFeURLEnv : perseoFeURLEnv + actionPath;
        } else {
            LOGGER.error(String.format("Invalid value for %s: %s",PERSEO_FE_URL_ENV, perseoFeURLEnv));
            return false;
        }
        LOGGER.info(String.format("actionRule configuration is: %s",actionRule));

        // Get MAX_AGE from env var if exist, else default
        String maxAgeEnv = System.getenv(PERSEO_MAX_AGE_ENV);
        // Check maxAge numerical value
        try {
            maxAge = maxAgeEnv != null ? Long.parseLong(maxAgeEnv) : Long.parseLong(defaultMaxAge);
        } catch (NumberFormatException nfe) {
            LOGGER.error(String.format("Invalid value for %s: %s",PERSEO_MAX_AGE_ENV,nfe));
            return false;
        }
        LOGGER.info(String.format("maxAge configuration is: %s",maxAge));

        // Get INTERNAL_TIMER_MSEC_RESOLUTION from env var if exist, else default
        String internalTimerMsecResolutionEnv = System.getenv(PERSEO_INTERNAL_TIMER_MSEC_RESOLUTION_ENV);
        // Check maxAge numerical value
        try {
            internalTimerMsecResolution = internalTimerMsecResolutionEnv != null ? Long.parseLong(internalTimerMsecResolutionEnv) : Long.parseLong(defaultInternalTimerMsecResolution);
        } catch (NumberFormatException nfe) {
            LOGGER.error(String.format("Invalid value for %s: %s",PERSEO_INTERNAL_TIMER_MSEC_RESOLUTION_ENV,nfe));
            return false;
        }
        LOGGER.info(String.format("internalTimerMsecResolution configuration is: %s",internalTimerMsecResolution));

        try {
            actionConnectTimeout = positiveTimeout("ACTION_HTTP_CONNECT_TIMEOUT_MS", "action.http.connect_timeout_ms", 5000);
            actionReadTimeout = positiveTimeout("ACTION_HTTP_READ_TIMEOUT_MS", "action.http.read_timeout_ms", 10000);
            esperBasicFilterFallbackEnabled = featureFlag(ESPER_BASIC_FILTER_FALLBACK_ENV);
            emptyNumericCastsEnabled = featureFlag(EMPTY_NUMERIC_CASTS_ENV);
            actionHttpHardeningEnabled = featureFlag(ACTION_HTTP_HARDENING_ENV);
            ruleUpdateSafetyEnabled = featureFlag(RULE_UPDATE_SAFETY_ENV);
        } catch (IllegalArgumentException ex) {
            LOGGER.error("Invalid feature flag or action HTTP timeout configuration", ex);
            return false;
        }
        return true;
    }

    private static boolean featureFlag(String environment) {
        return parseFeatureFlag(System.getenv(environment), environment);
    }

    static boolean parseFeatureFlag(String value, String environment) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        if ("true".equalsIgnoreCase(value.trim())) {
            return true;
        }
        if ("false".equalsIgnoreCase(value.trim())) {
            return false;
        }
        throw new IllegalArgumentException(environment + " must be true or false");
    }

    private static int positiveTimeout(String environment, String property, int defaultValue) {
        String value = System.getenv(environment);
        int timeout = Integer.parseInt(value != null ? value : PROPERTIES.getProperty(property, String.valueOf(defaultValue)));
        if (timeout <= 0) {
            throw new IllegalArgumentException(environment + " must be positive");
        }
        return timeout;
    }

    public static synchronized int getActionConnectTimeout() {
        return actionConnectTimeout;
    }

    public static synchronized int getActionReadTimeout() {
        return actionReadTimeout;
    }

    public static synchronized boolean isEsperBasicFilterFallbackEnabled() {
        return esperBasicFilterFallbackEnabled;
    }

    public static synchronized boolean isEmptyNumericCastsEnabled() {
        return emptyNumericCastsEnabled;
    }

    public static synchronized boolean isActionHttpHardeningEnabled() {
        return actionHttpHardeningEnabled;
    }

    public static synchronized boolean isRuleUpdateSafetyEnabled() {
        return ruleUpdateSafetyEnabled;
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

    /**
     *
     * @return millisecond resolutuion of the internal timer thread
     */
    public static synchronized long getInternalTimerMsecResolution() {
        return internalTimerMsecResolution;
    }
}
