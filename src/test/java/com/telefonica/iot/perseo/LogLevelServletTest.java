/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
 * 
* This file is part of perseo-core project.
 * 
* perseo-core is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 * 
* perseo-core is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
* You should have received a copy of the GNU General Public License along with
 * perseo-core. If not, see http://www.gnu.org/licenses/.
 * 
* For those usages not covered by the GNU General Public License please contact
 * with iot_support at tid dot es
 */
package com.telefonica.iot.perseo;

import com.telefonica.iot.perseo.test.Help;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author brox
 */
public class LogLevelServletTest {

    public LogLevelServletTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of doPut method, of class LogLevelServlet.
     * with valid levels
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testDoPutOK() throws Exception {
        System.out.println("doPut log level valid");
        Server server = Help.getServer(LogLevelServlet.class);
        server.start();
        try {
            String[] levels = {"DEBUG", "INFO", "WARN", "WARNING", "ERROR", "FATAL"};
            for (String level : levels) {
                String url = String.format("http://127.0.0.1:%d?level=%s", Help.PORT, level);
                Help.Res r = Help.sendPut(url, "");
                assertEquals(200, r.code);
            }
        } finally {
            server.stop();
        }
    }
     /**
     * Test of doPut method, of class LogLevelServlet.
     * with invalid levels
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testDoPutBad() throws Exception {
        System.out.println("doPut log level invalid");
        Server server = Help.getServer(LogLevelServlet.class);
        server.start();
        try {
            String[] levels = {"", "info", "WONDERFUL", "x"};
            for (String level : levels) {
                String url = String.format("http://127.0.0.1:%d/admin/log?level=%s", Help.PORT, level);
                Help.Res r = Help.sendPut(url, "");
                assertEquals(400, r.code);
            }
        } finally {
            server.stop();
        }
    }
}
