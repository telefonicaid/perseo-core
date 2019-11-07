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

import com.telefonica.iot.perseo.test.ServletContextMock;
import com.telefonica.iot.perseo.test.EventBeanMock;
import com.espertech.esper.client.ConfigurationOperations;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.telefonica.iot.perseo.test.Help;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author brox
 */
public class UtilsTest {

    public UtilsTest() {
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
     * Test of initEPService method, of class Utils.
     */
    @Test
    public void testInitEPService() {
        System.out.println("initEPService");
        ServletContext sc = new ServletContextMock();
        EPServiceProvider result = Utils.initEPService(sc);
        assertEquals(sc.getAttribute("epService"), result);
        //Do not create a new one if it already exists
        EPServiceProvider result2 = Utils.initEPService(sc);
        assertEquals(result, result2);

    }

    /**
     * Test of destroyEPService method, of class Utils.
     */
    @Test
    public void testDestroyEPService() {
        System.out.println("destroyEPService");
        ServletContextMock sc = new ServletContextMock();
        //Empty sc
        Utils.destroyEPService(sc);
        assertEquals(sc.map.size(), 0);
        //With a epservice
        Utils.initEPService(sc);
        Utils.destroyEPService(sc);
        assertEquals(sc.map.size(), 0);
    }

    /**
     * Test of JSONObject2Map method, of class Utils.
     */
    @Test
    public void testJSONObject2Map() {
        System.out.println("JSONObject2Map");
        HashMap<String, Object> m = new HashMap();
        m.put("one", "1");
        m.put("two", 2);
        JSONObject jo = new JSONObject(m);
        Map<String, Object> result = Utils.JSONObject2Map(jo);
        assertEquals(m, result);
    }

    /**
     * Test of Event2JSONObject method, of class Utils.
     */
    @Test
    public void testEvent2JSONObject() {
        System.out.println("Event2JSONObject");
        HashMap<String, Object> m = new HashMap();
        m.put("one", "1");
        m.put("two", 2);
        EventBean event = new EventBeanMock(m);
        JSONObject result = Utils.Event2JSONObject(event);
        assertEquals(result.get("one"), "1");
        assertEquals(result.get("two"), 2);
    }

    /**
     * Test of Statement2JSONObject method, of class Utils.
     */
    @Test
    public void testStatement2JSONObject() {
        final String epl = Help.ExampleRules[1];
        final String name = "rule name";
        System.out.println("Statement2JSONObject");
        EPServiceProvider epService = EPServiceProviderManager.getDefaultProvider();
        Map<String, Object> def = new HashMap<String, Object>();
        def.put("id", String.class);
        def.put("type", String.class);
        def.put(Constants.SUBSERVICE_FIELD, String.class);
        def.put(Constants.SERVICE_FIELD, String.class);
        ConfigurationOperations cfg = epService.getEPAdministrator().getConfiguration();
        cfg.addEventType("iotEvent", def);
        EPStatement st = epService.getEPAdministrator().createEPL(epl, name);
        JSONObject result = Utils.Statement2JSONObject(st);
        assertEquals(st.getName(), result.getString("name"));
        assertEquals(st.getText(), result.getString("text"));
        assertEquals(st.getState(), result.get("state"));
        assertEquals(st.getName(), result.getString("name"));
        assertEquals(st.getTimeLastStateChange(), result.getLong("timeLastStateChange"));
    }

    /**
     * Test of DoHTTPPost method, of class Utils.
     */
    @Test
    public void testDoHTTPPost() {
        System.out.println("DoHTTPPost");
        InetSocketAddress address = new InetSocketAddress(Help.PORT);
        HttpServer httpServer = null;
        try {
            httpServer = HttpServer.create(address, 0);
            HttpHandler handler = new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    byte[] response = "OK\n".getBytes();
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK,
                            response.length);
                    exchange.getResponseBody().write(response);
                    exchange.close();
                }
            };
            httpServer.createContext("/path", handler);
            httpServer.start();
            boolean result = Utils.DoHTTPPost(
                    String.format("http://localhost:%d/path", Help.PORT), "xxxxx");
            assertEquals(true, result);
            result = Utils.DoHTTPPost(
                    String.format("http://localhost:%d/notexist", Help.PORT), "xxxxx");
            assertEquals(false, result);
            result = Utils.DoHTTPPost("<<this is an invalid URL>>", "xxxxx");
            assertEquals(false, result);

        } catch (IOException ex) {
            Logger.getLogger(UtilsTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.toString());
        } finally {
            if (httpServer != null) {
                httpServer.stop(0);
            }
        }

    }

    /**
     * Test of isValidURL method, of class Utils.
     */
    @Test
    public void testURLValidator() {

        assertEquals(Utils.isValidURL("https://valid.url/forTest"), true);
        assertEquals(Utils.isValidURL("invalid.url_forTest"), false);
    }
}
