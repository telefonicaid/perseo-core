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

import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.EPDeploymentService;
import com.espertech.esper.runtime.client.EPDeployment;
import com.espertech.esper.runtime.client.EPUndeployException;
import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.util.StatementProperty;
import com.telefonica.iot.perseo.test.Help;
import com.telefonica.iot.perseo.test.ServletContextMock;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 *
 * @author brox
 */
public class RulesManagerTest {

    EPRuntime epService;
    private static final Logger logger = LoggerFactory.getLogger(RulesManagerTest.class);
    public RulesManagerTest() {
        epService = Utils.initEPService(new ServletContextMock());
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        try {
            epService.getDeploymentService().undeployAll();
        } catch (EPUndeployException ex) {
            throw new RuntimeException(ex);
        }
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of get method, of class RulesManager.
     */
    @Test
    public void testGet() {
        EPDeploymentService epa = epService.getDeploymentService();

        logger.info("get");
        String ruleName = "ccc";
        String epl = Help.ExampleRules()[0];

        EPDeployment deployment = Utils.compileDeploy(epService, epl, ruleName);
        EPStatement st = deployment.getStatements()[0];

        Result result = RulesManager.get(epService, ruleName);
        assertEquals(200, result.getStatusCode());
        result = RulesManager.get(epService, "it does not exist, we hope");
        assertEquals(404, result.getStatusCode());

        EPStatement st2 = Utils.getStatementFromDeployService(epa, ruleName);

        assertEquals(st, st2);
    }

    /**
     * Test of make method, of class RulesManager.
     */
    @Test
    public void testMake() {
        logger.info("make");
        Integer i = new Integer(0);
        for (String epl : Help.ExampleRules() ) {
            String ruleName = "ccc" + i.toString();
            logger.info("make ruleName: " + ruleName + " epl: " + epl);
            String text = String.format("{\"name\":\"%s\",\"text\":\"%s\"}", ruleName, epl);

            Result result = RulesManager.make(epService, text);
            assertEquals(200, result.getStatusCode());

            EPDeploymentService epa = epService.getDeploymentService();
            EPStatement st = Utils.getStatementFromDeployService(epa, ruleName);

            assertEquals(epl, st.getProperty(StatementProperty.EPL).toString());
            assertEquals(ruleName, st.getName());
            i++;
        }
    }

    @Test
    public void testMakeRejectsFullyQualifiedRuntimeAccessFromEpl() {
        String ruleName = "security_test_runtime_access";
        String epl = "select java.lang.Runtime.getRuntime() as runtime_ref "
            + "from pattern [every e=iotEvent]";
        JSONObject rule = new JSONObject()
            .put("name", ruleName)
            .put("text", epl);

        Result result = RulesManager.make(epService, rule.toString());
        assertEquals(
                     "The fully qualified Runtime access should be rejected",
                     400,
                     result.getStatusCode()
                     );
        assertNull(Utils.getStatementFromDeployService(
            epService.getDeploymentService(), ruleName));
    }

    @Test
    public void testMakeRejectsRuntimeAliasAccessFromEpl() {
        String ruleName = "security_test_runtime_access";
        String epl = "select Runtime.getRuntime() as runtime_ref "
            + "from pattern [every e=iotEvent]";
        JSONObject rule = new JSONObject()
            .put("name", ruleName)
            .put("text", epl);

        Result result = RulesManager.make(epService, rule.toString());
        assertEquals(
                     "The Runtime alias access should be rejected",
                     400,
                     result.getStatusCode()
                     );
        assertNull(Utils.getStatementFromDeployService(
            epService.getDeploymentService(), ruleName));
    }

    @Test
    public void testAllowsDocumentedMapAndIntegerUsageFromEpl() {
        String epl = "select Integer.toHexString(cast(cast(value?,java.util.Map)"
            + ".get('lastIrrigationTimeReceived'),int)) as irrigationTimeHex "
            + "from iotEvent";
        EPDeployment deployment = Utils.compileDeploy(epService, epl, "documented_java_usage");
        EPStatement statement = deployment.getStatements()[0];
        ArrayList<Object> results = new ArrayList<Object>();
        statement.addListener((newEvents, oldEvents, source, runtime) -> {
            for (EventBean event : newEvents) {
                results.add(event.get("irrigationTimeHex"));
            }
        });

        Map<String, Object> irrigationData = new HashMap<String, Object>();
        irrigationData.put("lastIrrigationTimeReceived", 255);
        Map<String, Object> inputEvent = new HashMap<String, Object>();
        inputEvent.put("id", "documented-java-usage-event");
        inputEvent.put("type", "DocumentedJavaUsage");
        inputEvent.put(Constants.SERVICE_FIELD, "test-service");
        inputEvent.put(Constants.SUBSERVICE_FIELD, "/test");
        inputEvent.put("value", irrigationData);

        epService.getEventService().sendEventMap(inputEvent, Constants.IOT_EVENT);

        assertEquals(1, results.size());
        assertEquals("ff", results.get(0));
    }

    @Test
    public void testClassForNameProviderAllowsConfiguredClasses() throws ClassNotFoundException {
        PerseoClassForNameProvider provider = new PerseoClassForNameProvider();
        String[] allowedClasses = {
            "java.lang.String",
            "java.lang.Boolean",
            "java.lang.Byte",
            "java.lang.Short",
            "java.lang.Integer",
            "java.lang.Long",
            "java.lang.Float",
            "java.lang.Double",
            "java.lang.Number",
            "java.lang.Character",
            "java.lang.Math",
            "java.util.Collection",
            "java.util.List",
            "java.util.Set",
            "java.util.Map",
            "java.time.ZoneId",
            "com.telefonica.iot.perseo.utils.DateTimeUtils"
        };

        for (String className : allowedClasses) {
            assertNotNull(className, provider.classForName(className));
        }
    }

    @Test
    public void testClassForNameProviderRejectsUnlistedClasses() {
        PerseoClassForNameProvider provider = new PerseoClassForNameProvider();
        String[] deniedClasses = {
            "java.lang.Runtime",
            "java.lang.ProcessBuilder",
            "java.lang.System",
            "java.lang.Class",
            "java.lang.ClassLoader",
            "java.lang.reflect.Method",
            "java.io.File",
            "java.util.HashMap",
            "com.example.UnlistedClass",
            "",
            "   "
        };

        for (String className : deniedClasses) {
            try {
                provider.classForName(className);
                fail("Expected class to be rejected: " + className);
            } catch (ClassNotFoundException expected) {
                // Expected for every class outside the allowlist.
            }
        }

        try {
            provider.classForName(null);
            fail("Expected null class name to be rejected");
        } catch (ClassNotFoundException expected) {
            // Expected for a null class name.
        }
    }

        /**
         * Test of updateAll method, of class RulesManager.
         */
    @Test
    public void testUpdateAll() {
        logger.info("updateAll");
        String ruleName = "ccc";
        String epl = Help.ExampleRules()[0];
        String text = String.format("[{\"name\":\"%s\",\"text\":\"%s\"}]", ruleName, epl);

        Result result = RulesManager.updateAll(epService, text);
        assertEquals(200, result.getStatusCode());

        EPDeploymentService epa = epService.getDeploymentService();

        ArrayList<String> names = new ArrayList<String>();
        String[] deploymentIds = epa.getDeployments();
        for (String deploymentId : deploymentIds) {
            EPDeployment deployment = epa.getDeployment(deploymentId);
            EPStatement[] statements = deployment.getStatements();
            for (EPStatement st : statements) {
                names.add(st.getName());
            }
        }

        assertEquals(1, names.size());

        EPStatement st = Utils.getStatementFromDeployService(epa, ruleName);

        assertEquals(epl, st.getProperty(StatementProperty.EPL).toString());
        assertEquals(ruleName, st.getName());
    }

    /**
     * Test of delete method, of class RulesManager.
     */
    @Test
    public void testDelete() {
        logger.info("delete");
        String ruleName = "ccc";
        String epl = Help.ExampleRules()[0];

        EPDeploymentService epa = epService.getDeploymentService();

        EPDeployment deployment = Utils.compileDeploy(epService, epl, ruleName);
        EPStatement st = deployment.getStatements()[0];

        assertNotNull(st);

        Result result = RulesManager.delete(epService, ruleName);
        assertEquals(200, result.getStatusCode());

        EPStatement st2 = Utils.getStatementFromDeployService(epa, ruleName);

        assertEquals(null, st2);
    }
}
