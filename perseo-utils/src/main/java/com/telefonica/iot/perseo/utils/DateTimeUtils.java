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

import ca.rmen.sunrisesunset.SunriseSunset;
import org.apache.log4j.pattern.DatePatternConverter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import java.util.Calendar;

public class DateTimeUtils {

    public static Calendar getNextSunise(Calendar day, double latitude, double longitude) {

        Calendar[] sunriseSunset = SunriseSunset.getSunriseSunset(day, latitude, longitude);

        Calendar day2 = (Calendar) day.clone();

        if (sunriseSunset[0].get(Calendar.HOUR_OF_DAY) < Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            day2.add(Calendar.DATE, 1);
            sunriseSunset = SunriseSunset.getSunriseSunset(day2, latitude, longitude);
        }

        return sunriseSunset[0];
    }

    public static Calendar getNextSunset(Calendar day, double latitude, double longitude) {

        Calendar[] sunriseSunset = SunriseSunset.getSunriseSunset(day, latitude, longitude);

        Calendar day2 = (Calendar) day.clone();

        if (sunriseSunset[1].get(Calendar.HOUR_OF_DAY) < Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            day2.add(Calendar.DATE, 1);
            sunriseSunset = SunriseSunset.getSunriseSunset(day2, latitude, longitude);
        }

        return sunriseSunset[1];
    }

    public static long getMilisToNextSunise(Calendar day, double latitude, double longitude) {

        Calendar[] sunriseSunset = SunriseSunset.getSunriseSunset(day, latitude, longitude);

        Calendar day2 = (Calendar) day.clone();

        if (sunriseSunset[0].get(Calendar.HOUR_OF_DAY) < Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            day2.add(Calendar.DATE, 1);
            sunriseSunset = SunriseSunset.getSunriseSunset(day2, latitude, longitude);
        }

        return sunriseSunset[0].getTimeInMillis();
    }

    public static long getMilisToNextSunset(Calendar day, double latitude, double longitude) {

        Calendar[] sunriseSunset = SunriseSunset.getSunriseSunset(day, latitude, longitude);

        Calendar day2 = (Calendar) day.clone();

        if (sunriseSunset[1].get(Calendar.HOUR_OF_DAY) < Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            day2.add(Calendar.DATE, 1);
            sunriseSunset = SunriseSunset.getSunriseSunset(day2, latitude, longitude);
        }

        return sunriseSunset[1].getTimeInMillis();
    }

    public static String timeToUTC(String localTime) {
        String dateFormatISO = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
        LocalDateTime localDateTime = LocalDateTime.parse(localTime, DateTimeFormatter.ofPattern(dateFormatISO));
        ZonedDateTime zonedDateTimeUTC = localDateTime.atZone(ZoneOffset.UTC);
        return zonedDateTimeUTC.toOffsetDateTime().toString();
    }

}
