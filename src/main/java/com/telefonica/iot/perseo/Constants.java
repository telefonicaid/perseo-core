/**
* Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
*
* This file is part of perseo-core project.
*
* perseo-core is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
* General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
* option) any later version.
*
* perseo-core is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
* implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
* for more details.
*
* You should have received a copy of the GNU Affero General Public License along with perseo-core. If not, see
* http://www.gnu.org/licenses/.
*
* For those usages not covered by the GNU Affero General Public License please contact with
* iot_support at tid dot es
*/

package com.telefonica.iot.perseo;

/**
 *
 * @author brox
 */
public final class Constants {
    private Constants() { };
    public static final String TENANT_FIELD = "tenant";
    public static final String SUBSERVICE_FIELD = "subservice";
    public static final String CORRELATOR_HEADER = "unica-correlator";
    public static final String TRANSACTION_ID = "transactionId";
    public static final String CORRELATOR_ID = "correlatorId";
    /*
        Name for event stream
    */
    public static final String IOT_EVENT = "iotEvent";
}
