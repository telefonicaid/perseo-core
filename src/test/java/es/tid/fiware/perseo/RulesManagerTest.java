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

package es.tid.fiware.perseo;

import com.espertech.esper.client.ConfigurationOperations;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import es.tid.fiware.perseo.test.Help;
import java.util.HashMap;
import java.util.Map;
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
public class RulesManagerTest {

    EPServiceProvider epService;

    public RulesManagerTest() {
        epService = EPServiceProviderManager.getDefaultProvider();

        Map<String, Object> def = new HashMap<String, Object>();
        def.put("id", String.class);
        def.put("type", String.class);
        def.put(Constants.SERVICE_FIELD, String.class);
        def.put(Constants.TENANT_FIELD, String.class);
        ConfigurationOperations cfg = epService.getEPAdministrator().getConfiguration();
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
        epService.getEPAdministrator().destroyAllStatements();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of get method, of class RulesManager.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        String ruleName = "ccc";
        String epl = Help.ExampleRules[0];
        EPStatement st = epService.getEPAdministrator().createEPL(epl, ruleName);
        Result result = RulesManager.get(epService, ruleName);
        assertEquals(200, result.getStatusCode());
        result = RulesManager.get(epService, "it does not exist, we hope");
        assertEquals(404, result.getStatusCode());
        EPStatement st2 = epService.getEPAdministrator().getStatement(ruleName);
        assertEquals(st, st2);
    }

    /**
     * Test of make method, of class RulesManager.
     */
    @Test
    public void testMake() {
        System.out.println("make");
        String ruleName = "ccc";
        String epl = Help.ExampleRules[0];
        String text = String.format("{\"name\":\"%s\",\"text\":\"%s\"}", ruleName, epl);

        Result result = RulesManager.make(epService, text);
        assertEquals(200, result.getStatusCode());
        EPStatement st = epService.getEPAdministrator().getStatement(ruleName);
        assertEquals(epl, st.getText());
        assertEquals(ruleName, st.getName());
    }

    /**
     * Test of updateAll method, of class RulesManager.
     */
    @Test
    public void testUpdateAll() {
        System.out.println("updateAll");
        String ruleName = "ccc";
        String epl = Help.ExampleRules[0];
        String text = String.format("[{\"name\":\"%s\",\"text\":\"%s\"}]", ruleName, epl);

        Result result = RulesManager.updateAll(epService, text);
        assertEquals(200, result.getStatusCode());
        String[] names = epService.getEPAdministrator().getStatementNames();
        assertEquals(1, names.length);
        EPStatement st = epService.getEPAdministrator().getStatement(ruleName);
        assertEquals(epl, st.getText());
        assertEquals(ruleName, st.getName());

    }

    /**
     * Test of delete method, of class RulesManager.
     */
    @Test
    public void testDelete() {
        System.out.println("delete");
        String ruleName = "ccc";
        String epl = Help.ExampleRules[0];
        EPStatement st = epService.getEPAdministrator().createEPL(epl, ruleName);
        assertNotNull(st);
        Result result = RulesManager.delete(epService, ruleName);
        assertEquals(200, result.getStatusCode());
        EPStatement st2 = epService.getEPAdministrator().getStatement(ruleName);
        assertEquals(null, st2);
    }

}
