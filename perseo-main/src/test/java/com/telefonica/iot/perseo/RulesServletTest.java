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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

import org.eclipse.jetty.server.Server;
import com.telefonica.iot.perseo.test.Help;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author brox
 */
public class RulesServletTest {
    private static final Logger logger = LoggerFactory.getLogger(RulesServletTest.class);
    public RulesServletTest() {
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
     * Test of doGet method, of class RulesServlet.
     * @throws java.lang.Exception
     */
    @Test
    public void testDoGet() throws Exception {
        logger.info("doGet");
        Server server = Help.getServer(RulesServlet.class);
        server.start();
        try {
            String url = String.format("http://127.0.0.1:%d/nothing", Help.PORT);
            Help.Res r = Help.doGet(url);
            assertEquals(404, r.getCode());
            url = String.format("http://127.0.0.1:%d/", Help.PORT);
            r = Help.doGet(url);
            assertEquals(200, r.getCode());
        } finally {
            server.stop();
        }
    }

    /**
     * Test of doPost method, of class RulesServlet.
     * @throws java.lang.Exception
     */
    @Test
    public void testDoPost() throws Exception {
        logger.info("doPost");
        Server server = Help.getServer(RulesServlet.class);
        server.start();
        try {
            String url = String.format("http://127.0.0.1:%d", Help.PORT);
            JSONObject jr = new JSONObject();
            jr.put("name", "test doPost rule");
            jr.put("text", Help.ExampleRules()[0]);
            Help.Res r = Help.sendPost(url, jr.toString(2));
            assertEquals(200, r.getCode());
            jr.remove("name");
            r = Help.sendPost(url, jr.toString(2));
            assertEquals(400, r.getCode());
            jr.put("name", "test doPost rule");
            jr.put("text", "<<this is invalid EPL>>");
            r = Help.sendPost(url, jr.toString(2));
            assertEquals(400, r.getCode());
        } finally {
            server.stop();
        }
    }

    /**
     * Test of doPut method, of class RulesServlet.
     * @throws java.lang.Exception
     */
    @Test
    public void testDoPut() throws Exception {
        logger.info("doPut");
        Server server = Help.getServer(RulesServlet.class);
        server.start();
        try {
            String url = String.format("http://127.0.0.1:%d", Help.PORT);
            JSONArray ja = new JSONArray();
            JSONObject jr = new JSONObject();
            jr.put("name", "test doPost rule 1");
            jr.put("text", Help.ExampleRules()[0]);
            ja.put(jr);
            jr = new JSONObject();
            jr.put("name", "test doPost rule 2");
            jr.put("text", Help.ExampleRules()[1]);
            Help.Res r = Help.sendPut(url, ja.toString(2));
            assertEquals(200, r.getCode());
        } finally {
            server.stop();
        }
    }
/**
     * Test of testDoPut method, of class RulesServlet.
     * 
     * It tests a big rule set
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void testDoPutLongSet() throws Exception {
        logger.info("doPutLongSet");
        Server server = Help.getServer(RulesServlet.class);
        server.start();
        try {
            String url = String.format("http://127.0.0.1:%d", Help.PORT);
            String longSet = Help.longRuleSet();
            logger.info(String.format("doPutLongSet set size=%s", longSet.length()));
            Help.Res r = Help.sendPut(url, longSet);
            assertEquals(200, r.getCode());
        } finally {
            server.stop();
        }
    }

    /**
     * Test of doDelete method, of class RulesServlet.
     * @throws java.lang.Exception
     */
    @Test
    public void testDoDelete() throws Exception {
        logger.info("doDelete");
        Server server = Help.getServer(RulesServlet.class);
        server.start();
        try {
            String url = String.format("http://127.0.0.1:%d/nothing", Help.PORT);
            Help.Res r = Help.doDelete(url);
            //assertEquals(200, r.getCode());
            assertEquals(400, r.getCode());
            url = String.format("http://127.0.0.1:%d/", Help.PORT);
            r = Help.doDelete(url);
            assertEquals(404, r.getCode());
        } finally {
            server.stop();
        }
    }

}
