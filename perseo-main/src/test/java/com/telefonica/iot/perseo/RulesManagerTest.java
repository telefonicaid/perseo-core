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

import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.common.client.configuration.*;
import com.espertech.esper.common.client.configuration.common.*;
import com.espertech.esper.common.client.configuration.compiler.*;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPRuntimeProvider;
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
import com.telefonica.iot.perseo.test.Help;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 *
 * @author brox
 */
public class RulesManagerTest {

    EPRuntime epService;
    private static final Logger logger = LoggerFactory.getLogger(RulesManagerTest.class);
    public RulesManagerTest() {
        com.espertech.esper.common.client.configuration.Configuration configuration = new com.espertech.esper.common.client.configuration.Configuration();

        Map<String, Object> def = new HashMap<String, Object>();
        def.put("id", String.class);
        def.put("type", String.class);
        def.put(Constants.SUBSERVICE_FIELD, String.class);
        def.put(Constants.SERVICE_FIELD, String.class);
        configuration.getCommon().addEventType("iotEvent", def);

        epService = EPRuntimeProvider.getDefaultRuntime(configuration);
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
