 
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

package com.telefonica.iot.perseo.utils;

import junit.framework.TestCase;

import java.util.Calendar;
import java.util.Date;

/**
 * The type Date time utils test.
 */
public class DateTimeUtilsTest extends TestCase {

    /**
     * Test get next sunrise.
     */
    public void testGetNextSunrise() {
        System.out.println("getNextSunrise");
        Calendar calendar = Calendar.getInstance();
        Calendar nextSunrise = DateTimeUtils.getNextSunrise(calendar, 40.4131699, -3.6830699);
        assertTrue(calendar.compareTo(nextSunrise) < 0);
    }

    /**
     * Test get next sunset.
     */
    public void testGetNextSunset() {
        System.out.println("getNextSunset");
        Calendar calendar = Calendar.getInstance();
        Calendar nextSunset = DateTimeUtils.getNextSunset(calendar, 40.4131699, -3.6830699);
        assertTrue(calendar.compareTo(nextSunset) < 0);
    }

    /**
     * Test get milis to next sunrise.
     */
    public void testGetMilisToNextSunrise() {
        System.out.println("getMilisToNextSunrise");
        Calendar calendar = Calendar.getInstance();
        long nextSunrise = DateTimeUtils.getMilisToNextSunrise(calendar, 40.4131699, -3.6830699);
        assertTrue(calendar.getTimeInMillis() < nextSunrise);
    }

    /**
     * Test get milis to next sunset.
     */
    public void testGetMilisToNextSunset() {
        System.out.println("getMilisToNextSunset");
        Calendar calendar = Calendar.getInstance();
        long nextSunset = DateTimeUtils.getMilisToNextSunset(calendar, 40.4131699, -3.6830699);
        assertTrue(calendar.getTimeInMillis() < nextSunset);
    }

    /**
     * Test date to utc.
     */
    public void testDateToUTC() {
        System.out.println("dateToUTC");
        String isoDate = "2019-11-11T11:43:01+04:00";
        Calendar dateToUTC = DateTimeUtils.dateToUTC(isoDate);
        assertTrue(dateToUTC.get(Calendar.HOUR_OF_DAY) == 7);
    }

    /**
     * Test time to utc.
     */
    public void testTimeToUTC() {
        System.out.println("timeToUTC");
        String hour = "10";
        String timeZone = "CET";
        String hourUTC = DateTimeUtils.timeToUTC(hour, timeZone);
        assertTrue(Integer.parseInt(hourUTC) == 9);
    }
}
