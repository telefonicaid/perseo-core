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
