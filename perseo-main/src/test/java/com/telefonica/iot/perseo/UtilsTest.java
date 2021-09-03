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
// import com.espertech.esper.client.ConfigurationOperations;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.common.client.configuration.*;
import com.espertech.esper.common.client.configuration.common.*;
import com.espertech.esper.common.client.configuration.compiler.*;
// import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.runtime.client.EPRuntime;
// import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.runtime.client.EPRuntimeProvider;
// import com.espertech.esper.client.EPStatement;
import com.espertech.esper.runtime.client.EPStatement;
// import com.espertech.esper.client.EventBean;
import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.EPDeploymentService;
import com.espertech.esper.runtime.client.EPDeployment;
import com.espertech.esper.common.client.EPException;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.runtime.client.EPDeployException;
import com.espertech.esper.runtime.client.EPUndeployException;
import com.espertech.esper.common.client.util.StatementProperty;

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
import javax.servlet.ServletContext;
import org.json.JSONObject;
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
public class UtilsTest {
    private static final Logger logger = LoggerFactory.getLogger(UtilsTest.class);
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
        logger.info("initEPService");
        ServletContext sc = new ServletContextMock();
        //EPServiceProvider result = Utils.initEPService(sc);
        EPRuntime result = Utils.initEPService(sc);
        assertEquals(sc.getAttribute("epService"), result);
        //Do not create a new one if it already exists
        //EPServiceProvider result2 = Utils.initEPService(sc);
        EPRuntime result2 = Utils.initEPService(sc);
        assertEquals(result, result2);
    }

    /**
     * Test of destroyEPService method, of class Utils.
     */
    @Test
    public void testDestroyEPService() {
        logger.info("destroyEPService");
        ServletContextMock sc = new ServletContextMock();
        //Empty sc
        Utils.destroyEPService(sc);
        assertEquals(sc.getMap().size(), 0);
        //With a epservice
        Utils.initEPService(sc);
        Utils.destroyEPService(sc);
        assertEquals(sc.getMap().size(), 0);
    }

    /**
     * Test of JSONObject2Map method, of class Utils.
     */
    @Test
    public void testJSONObject2Map() {
        logger.info("JSONObject2Map");
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
        logger.info("Event2JSONObject");
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
        final String epl = Help.ExampleRules()[1];
        final String name = "rule name";
        logger.info("Statement2JSONObject");
        //EPServiceProvider epService = EPServiceProviderManager.getDefaultProvider();
        EPRuntime epService = EPRuntimeProvider.getDefaultRuntime(configuration);
        Map<String, Object> def = new HashMap<String, Object>();
        def.put("id", String.class);
        def.put("type", String.class);
        def.put(Constants.SUBSERVICE_FIELD, String.class);
        def.put(Constants.SERVICE_FIELD, String.class);
        //ConfigurationOperations cfg = epService.getEPAdministrator().getConfiguration();
        com.espertech.esper.common.client.configuration.Configuration cfgCopy = epService.getConfigurationDeepCopy();
        ConfigurationCommon cfg = cfgCopy.getCommon();
        cfg.addEventType("iotEvent", def);
        //EPStatement st = epService.getEPAdministrator().createEPL(epl, name);

        // Deployment for compile newEPL
        com.espertech.esper.common.client.configuration.Configuration configuration = new com.espertech.esper.common.client.configuration.Configuration();
        CompilerArguments arguments = new CompilerArguments(configuration);
        arguments.getPath().add(epService.getRuntimePath());
        EPCompiled epCompiled = null;
        try {
            epCompiled = EPCompilerProvider.getCompiler().compile(epl, arguments);
        } catch (EPCompileException ex) {
            throw new RuntimeException(ex);
        }
        EPDeployment deploymentForEPL;
        try {
            deploymentForEPL = epa.deploy(epCompiled);
        } catch (EPDeployException ex) {
            throw new RuntimeException(ex);
        }

        dId = deploymentForEPL.getDeploymentId();
        EPStatement st = epa.getStatement(dId, ruleName);


        JSONObject result = Utils.Statement2JSONObject(st);
        assertEquals(st.getName(), result.getString("name"));
        //assertEquals(st.getText(), result.getString("text"));
        assertEquals(st.getProperty(StatementProperty.EPL).toString(), result.getString("text"));
        //assertEquals(st.getState(), result.get("state"));
        assertEquals(st.isDestroyed(), result.get("state"));
        //assertEquals(st.getName(), result.getString("name"));
        //assertEquals(st.getTimeLastStateChange(), result.getLong("timeLastStateChange"));
        assertEquals(epService.getDeployment(dId).getLastUpdateDate(), result.getLong("timeLastStateChange"));
    }

    /**
     * Test of DoHTTPPost method, of class Utils.
     */
    @Test
    public void testDoHTTPPost() {
        logger.info("DoHTTPPost");
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
            logger.error(ex.toString());
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
