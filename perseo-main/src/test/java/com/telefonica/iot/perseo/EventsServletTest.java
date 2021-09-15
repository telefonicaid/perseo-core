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

import com.telefonica.iot.perseo.test.Help;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 *
 * @author brox
 */
public class EventsServletTest {
    private static final Logger logger = LoggerFactory.getLogger(EventsServletTest.class);
    public EventsServletTest() {
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
     * Test of doPost method, of class EventsServlet.
     * @throws java.lang.Exception
     */
    @Test
    public void testDoPost() throws Exception {
        logger.info("doPost");
        Server server = Help.getServer(EventsServlet.class);
        server.start();
        try {
            String url = String.format("http://127.0.0.1:%d", Help.PORT);
            for (String notice : Help.ExampleNotices() ) {
                Help.Res r = Help.sendPost(url, notice);
                assertEquals(200, r.getCode());
            }
            Help.Res r2 = Help.sendPost(url, "<<this is invalid JSON>>");
            assertEquals(400, r2.getCode());
        } finally {
            server.stop();
        }
    }

}
