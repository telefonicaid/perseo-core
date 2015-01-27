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
 * Reprents an result from an operation. It consists of an numeric code, usually
 * an HTTP status code, and a message, usually a JSON
 *
 * @author brox
 */
public class Result {

    private int statusCode;
    private String message;

    /**
     * Constructor.
     * @param code int code showing the error or success of the operation.
     * Mainly an HTTP statuc code.
     * @parame message string wiht description of the result. Mainly a JSON
     * representation.
     */
    Result(int code, String msg) {
        this.statusCode = code;
        this.message = msg;

    }

    /**
     * @return the statusCode
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * @param statusCode the statusCode to set
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
