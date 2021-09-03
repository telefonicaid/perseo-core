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

//import com.espertech.esper.client.ConfigurationOperations;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.common.client.configuration.*;
import com.espertech.esper.common.client.configuration.common.*;
import com.espertech.esper.common.client.configuration.compiler.*;
//import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.runtime.client.EPRuntime;
//import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.runtime.client.EPRuntimeProvider;
//import com.espertech.esper.client.EPStatement;
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

    //EPServiceProvider epService;
    EPRuntime epService;
    private static final Logger logger = LoggerFactory.getLogger(RulesManagerTest.class);
    public RulesManagerTest() {
        //epService = EPServiceProviderManager.getDefaultProvider();
        epService = EPRuntimeProvider.getDefaultRuntime();

        Map<String, Object> def = new HashMap<String, Object>();
        def.put("id", String.class);
        def.put("type", String.class);
        def.put(Constants.SUBSERVICE_FIELD, String.class);
        def.put(Constants.SERVICE_FIELD, String.class);
        //ConfigurationOperations cfg = epService.getEPAdministrator().getConfiguration();
        ConfigurationCommon cfg = epService.getConfigurationDeepCopy().getCommon();

        cfg.addEventType("iotEvent", def);
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        //epService.getEPAdministrator().destroyAllStatements();
        epService.getDeploymentService().undeployAll();
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
        //EPStatement st = epService.getEPAdministrator().createEPL(epl, ruleName);

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

        String dId = deploymentForEPL.getDeploymentId();
        EPStatement st = epa.getStatement(dId, ruleName);
        Result result = RulesManager.get(epService, ruleName);
        assertEquals(200, result.getStatusCode());
        result = RulesManager.get(epService, "it does not exist, we hope");
        assertEquals(404, result.getStatusCode());


        //EPStatement st2 = epService.getEPAdministrator().getStatement(ruleName);
        EPStatement st2 = null;
        String[] deploymentIds = epa.getDeployments();
        dId = null;
        for (String deploymentId : deploymentIds) {
            st2 = epa.getStatement(deploymentId, ruleName);
            if (st2 != null) {
                dId = deploymentId;
                break;
            }
        }

        assertEquals(st, st2);
    }

    /**
     * Test of make method, of class RulesManager.
     */
    @Test
    public void testMake() {
        logger.info("make");
        String ruleName = "ccc";
        String epl = Help.ExampleRules()[0];
        String text = String.format("{\"name\":\"%s\",\"text\":\"%s\"}", ruleName, epl);

        Result result = RulesManager.make(epService, text);
        assertEquals(200, result.getStatusCode());
        //EPStatement st = epService.getEPAdministrator().getStatement(ruleName);

        EPDeploymentService epa = epService.getDeploymentService();

        EPStatement st = null;
        String[] deploymentIds = epa.getDeployments();
        String dId = null;
        for (String deploymentId : deploymentIds) {
            st = epa.getStatement(deploymentId, ruleName);
            if (st != null) {
                dId = deploymentId;
                break;
            }
        }

        assertEquals(epl, st.getProperty(StatementProperty.EPL).toString());
        assertEquals(ruleName, st.getName());
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

        //String[] names = epService.getEPAdministrator().getStatementNames();

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

        //EPStatement st = epService.getEPAdministrator().getStatement(ruleName);
        EPStatement st = null;
        String[] deploymentIds = epa.getDeployments();
        String dId = null;
        for (String deploymentId : deploymentIds) {
            st = epa.getStatement(deploymentId, ruleName);
            if (st != null) {
                dId = deploymentId;
                break;
            }
        }

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

        //EPStatement st = epService.getEPAdministrator().createEPL(epl, ruleName);

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
        String dId = deploymentForEPL.getDeploymentId();
        EPStatement st = epa.getStatement(dId, ruleName);

        assertNotNull(st);
        Result result = RulesManager.delete(epService, ruleName);
        assertEquals(200, result.getStatusCode());

        //EPStatement st2 = epService.getEPAdministrator().getStatement(ruleName);

        EPStatement st2 = null;
        String[] deploymentIds = epa.getDeployments();
        dId = null;
        for (String deploymentId : deploymentIds) {
            st2 = epa.getStatement(deploymentId, ruleName);
            if (st2 != null) {
                dId = deploymentId;
                break;
            }
        }
        assertEquals(null, st2);
    }
}
