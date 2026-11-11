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
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.common.client.configuration.*;
import com.espertech.esper.common.client.configuration.common.*;
import com.espertech.esper.common.client.configuration.compiler.*;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPRuntimeProvider;
import com.espertech.esper.runtime.client.EPStatement;
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
import org.json.JSONArray;
import java.util.ArrayList;
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
        EPRuntime result = Utils.initEPService(sc);
        assertEquals(sc.getAttribute("epService"), result);
        //Do not create a new one if it already exists
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
        m.put("nulo", JSONObject.NULL);
        m.put("array", new JSONArray().put(1).put(2).put(3));
        JSONObject jo = new JSONObject(m);
        Map<String, Object> result = Utils.JSONObject2Map(jo);
        assertEquals(result.get("one"), "1");
        assertEquals(result.get("two"), 2);
        assertEquals(result.get("nulo"), null);
        ArrayList<Integer> arrList = new ArrayList<Integer>();
        arrList.add(1);
        arrList.add(2);
        arrList.add(3);
        assertEquals(result.get("array"), arrList);
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
        m.put("nulo", null);
        EventBean event = new EventBeanMock(m);
        JSONObject result = Utils.Event2JSONObject(event);
        assertEquals(result.get("one"), "1");
        assertEquals(result.get("two"), 2);
        assertEquals(result.get("nulo"), null);
    }

    /**
     * Test of Statement2JSONObject method, of class Utils.
     */
    @Test
    public void testStatement2JSONObject() {
        final String epl = Help.ExampleRules()[1];
        final String name = "rule name";
        logger.info("Statement2JSONObject");
        com.espertech.esper.common.client.configuration.Configuration configuration = new com.espertech.esper.common.client.configuration.Configuration();

        Map<String, Object> def = new HashMap<String, Object>();
        def.put("id", String.class);
        def.put("type", String.class);
        def.put(Constants.SUBSERVICE_FIELD, String.class);
        def.put(Constants.SERVICE_FIELD, String.class);
        configuration.getCommon().addEventType("iotEvent", def);

        EPRuntime epService = EPRuntimeProvider.getDefaultRuntime(configuration);

        EPDeployment deployment = Utils.compileDeploy(epService, epl, name);
        EPStatement st = deployment.getStatements()[0];

        EPDeploymentService epa = epService.getDeploymentService();

        JSONObject result = Utils.Statement2JSONObject(st, epa, deployment.getDeploymentId());
        assertEquals(st.getName(), result.getString("name"));
        assertEquals(epl, result.getString("text"));
        assertEquals(st.isDestroyed(), result.get("state"));
        assertEquals(epa.getDeployment(deployment.getDeploymentId()).getLastUpdateDate().getTime(), result.getLong("timeLastStateChange"));
    }

    @Test
    public void testCompileDeployWithDollarNames() throws EPUndeployException {
        Configuration configuration = new Configuration();
        Map<String, Object> definition = new HashMap<String, Object>();
        definition.put("id", String.class);
        definition.put("type", String.class);
        definition.put(Constants.SUBSERVICE_FIELD, String.class);
        definition.put(Constants.SERVICE_FIELD, String.class);
        configuration.getCommon().addEventType("iotEvent", definition);
        EPRuntime runtime = EPRuntimeProvider.getRuntime("compileDeployDollarNames", configuration);
        String contextName = "ctxt$alcobendas$inmotica";
        String ruleName = "rule$alcobendas$inmotica";
        try {
            EPDeployment contextDeployment = Utils.compileDeploy(runtime,
                    "create context " + contextName + " partition by service from "
                    + "iotEvent(service=\"alcobendas\" and subservice=\"/inmotica\")", contextName);
            EPDeployment ruleDeployment = Utils.compileDeploy(runtime,
                    "context " + contextName + " select * from iotEvent", ruleName);
            EPDeploymentService deployments = runtime.getDeploymentService();
            EPStatement statement = ruleDeployment.getStatements()[0];

            assertEquals(contextName, contextDeployment.getStatements()[0].getName());
            assertEquals(ruleName, statement.getName());
            assertSame(contextDeployment.getStatements()[0],
                    Utils.getStatementFromDeployService(deployments, contextName));
            assertSame(statement, Utils.getStatementFromDeployService(deployments, ruleName));

            ArrayList<EventBean> received = new ArrayList<EventBean>();
            statement.addListener((newEvents, oldEvents, source, service) -> received.add(newEvents[0]));
            Map<String, Object> event = new HashMap<String, Object>();
            event.put("id", "sensor1");
            event.put("type", "sensor");
            event.put(Constants.SERVICE_FIELD, "alcobendas");
            event.put(Constants.SUBSERVICE_FIELD, "/inmotica");
            runtime.getEventService().sendEventMap(event, "iotEvent");

            assertEquals(1, received.size());
            assertEquals("sensor1", received.get(0).get("id"));

            deployments.undeploy(statement.getDeploymentId());
            deployments.undeploy(contextDeployment.getDeploymentId());
            assertNull(Utils.getStatementFromDeployService(deployments, ruleName));
            assertNull(Utils.getStatementFromDeployService(deployments, contextName));
        } finally {
            runtime.destroy();
        }
    }

    @Test
    public void testCompileDeployWithFloatThreshold() {
        Configuration configuration = new Configuration();
        configuration.getCommon().getTransientConfiguration().put(
            com.espertech.esper.common.client.util.ClassForNameProvider.NAME, new PerseoClassForNameProvider());
        Map<String, Object> definition = new HashMap<String, Object>();
        definition.put("id", String.class);
        definition.put("type", String.class);
        definition.put(Constants.SUBSERVICE_FIELD, String.class);
        definition.put(Constants.SERVICE_FIELD, String.class);
        configuration.getCommon().addEventType("iotEvent", definition);
        EPRuntime runtime = EPRuntimeProvider.getRuntime("compileDeployFloatThreshold", configuration);
        try {
            Utils.compileDeploy(runtime, "create context ctxt$alcobendas$dumps partition by service from "
                    + "iotEvent(service='alcobendas' and subservice='/dumps')", "ctxt$alcobendas$dumps");
            String epl = "context ctxt$alcobendas$dumps select *, \"alarm_threshold_04\" as ruleName, "
                    + "\"alert_fillingLevel\" as alert_attrib, \"fillingLevel\" AS attribute, "
                    + "current_timestamp().format(\"yyyy-MM-dd'T'HH:mm:ss.SSSX\") as newActuationTime, "
                    + "location? as loc, municipality? as mun, TimeInstant? as timeinstant "
                    + "from iotEvent(type = 'WasteContainer' and service='alcobendas' "
                    + "AND subservice='/dumps' AND udpl_flowName?='user_rule') "
                    + "where coalesce(cast(fillingLevel?, float), 0)!=1 "
                    + "AND cast(fillingLevel?, float) >= 0.89";
            EPStatement statement = Utils.compileDeploy(runtime, epl, "alarm_threshold_04").getStatements()[0];
            assertEquals(epl, Utils.getStatementEpl(statement));
            ArrayList<String> received = new ArrayList<String>();
            statement.addListener((newEvents, oldEvents, source, service) ->
                    received.add((String) newEvents[0].get("id")));

            Map<String, Object> event = new HashMap<String, Object>();
            event.put("type", "WasteContainer");
            event.put(Constants.SERVICE_FIELD, "alcobendas");
            event.put(Constants.SUBSERVICE_FIELD, "/dumps");
            event.put("udpl_flowName", "user_rule");
            Object[] levels = {0.88d, 0.9d, 0.95f, "0.99", 1.0d, null};
            for (int index = 0; index < levels.length; index++) {
                event.put("id", "level" + index);
                event.put("fillingLevel", levels[index]);
                runtime.getEventService().sendEventMap(event, "iotEvent");
            }
            event.put("id", "missing");
            event.remove("fillingLevel");
            runtime.getEventService().sendEventMap(event, "iotEvent");

            assertEquals(java.util.Arrays.asList("level1", "level2"), received);
        } finally {
            runtime.destroy();
        }
    }

    @Test
    public void testCompileDeployWithPatternFloatThreshold() {
        Configuration configuration = new Configuration();
        configuration.getCommon().getTransientConfiguration().put(
            com.espertech.esper.common.client.util.ClassForNameProvider.NAME, new PerseoClassForNameProvider());
        RecordingExceptionHandlerFactory.errors.clear();
        configuration.getRuntime().getExceptionHandling().addClass(RecordingExceptionHandlerFactory.class);
        Map<String, Object> definition = new HashMap<String, Object>();
        definition.put("id", String.class);
        definition.put("type", String.class);
        definition.put(Constants.SUBSERVICE_FIELD, String.class);
        definition.put(Constants.SERVICE_FIELD, String.class);
        configuration.getCommon().addEventType("iotEvent", definition);
        EPRuntime runtime = EPRuntimeProvider.getRuntime("compileDeployPatternFloatThreshold", configuration);
        try {
            Utils.compileDeploy(runtime, "create context ctxt$pattern$ partition by service from "
                    + "iotEvent(service='pattern' and subservice='/')", "ctxt$pattern$");
            for (String property : java.util.Arrays.asList("temperature", "temperature__metadata__accuracy")) {
                String epl = "context ctxt$pattern$ select ev.id as id from pattern "
                        + "[every ev=iotEvent(cast(cast(" + property + "?,String),Float)>1.5 "
                        + "and type='entity')]";
                EPDeployment deployment = Utils.compileDeploy(runtime, epl, property);
                ArrayList<String> received = new ArrayList<String>();
                deployment.getStatements()[0].addListener((newEvents, oldEvents, source, service) ->
                        received.add((String) newEvents[0].get("id")));
                Map<String, Object> event = new HashMap<String, Object>();
                event.put("type", "entity");
                event.put(Constants.SERVICE_FIELD, "pattern");
                event.put(Constants.SUBSERVICE_FIELD, "/");
                Object[] values = {1.0d, 2.0d, "3.0", null, "", "  ", "4.0"};
                for (int index = 0; index < values.length; index++) {
                    event.put("id", "value" + index);
                    event.put(property, values[index]);
                    runtime.getEventService().sendEventMap(event, "iotEvent");
                }
                assertEquals(java.util.Arrays.asList("value1", "value2", "value6"), received);
                assertTrue(RecordingExceptionHandlerFactory.errors.toString(),
                    RecordingExceptionHandlerFactory.errors.isEmpty());
                try {
                    runtime.getDeploymentService().undeploy(deployment.getDeploymentId());
                } catch (EPUndeployException ex) {
                    throw new AssertionError(ex);
                }
            }
        } finally {
            runtime.destroy();
        }
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
    public void testDoHTTPPostFailureHandling() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicInteger droppedRequests = new java.util.concurrent.atomic.AtomicInteger();
        server.createContext("/empty", exchange -> {
            exchange.getRequestBody().readAllBytes();
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });
        server.createContext("/error", exchange -> {
            exchange.getRequestBody().readAllBytes();
            exchange.sendResponseHeaders(503, -1);
            exchange.close();
        });
        server.createContext("/drop", exchange -> {
            droppedRequests.incrementAndGet();
            exchange.getRequestBody().readAllBytes();
            exchange.close();
        });
        server.createContext("/slow", exchange -> {
            try {
                release.await(5, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } finally {
                exchange.close();
            }
        });
        server.start();
        String base = "http://127.0.0.1:" + server.getAddress().getPort();
        try {
            assertTrue(Utils.DoHTTPPost(base + "/empty", "{}", 200, 200));
            assertFalse(Utils.DoHTTPPost(base + "/error", "{}", 200, 200));
            assertFalse(Utils.DoHTTPPost(base + "/drop", "{}", 200, 200));
            assertEquals(1, droppedRequests.get());
            assertFalse(Utils.DoHTTPPost(base + "/slow", "{}", 200, 200));
        } finally {
            release.countDown();
            server.stop(0);
        }
    }

    @Test
    public void testURLValidator() {
        assertEquals(Utils.isValidURL("https://valid.url/forTest"), true);
        assertEquals(Utils.isValidURL("invalid.url_forTest"), false);
    }

    public static class RecordingExceptionHandlerFactory
            implements com.espertech.esper.common.client.hook.exception.ExceptionHandlerFactory {
        static final ArrayList<Throwable> errors = new ArrayList<Throwable>();

        @Override
        public com.espertech.esper.common.client.hook.exception.ExceptionHandler getHandler(
                com.espertech.esper.common.client.hook.exception.ExceptionHandlerFactoryContext context) {
            return failure -> errors.add(failure.getThrowable());
        }
    }
}
