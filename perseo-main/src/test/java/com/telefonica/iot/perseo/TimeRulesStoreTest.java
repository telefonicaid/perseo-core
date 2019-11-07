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
*
* Created by: Carlos Blanco - Future Internet Consulting and Development Solutions (FICODES)
*/

package com.telefonica.iot.perseo;

import java.util.HashMap;

import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author cblanco
 */
public class TimeRulesStoreTest {

    private TimeRulesStore instance = TimeRulesStore.getInstance();

    public TimeRulesStoreTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        instance.cleanAllRules();
    }

    @After
    public void tearDown() {
        instance.cleanAllRules();
    }

    /**
     * Test of getAllRulesInfo empty
     */
    @Test
    public void testGetEmptyRuleMap() {

        HashMap<String, JSONObject> emptyHasmap = new HashMap<String, JSONObject>();
        HashMap<String, JSONObject> result = instance.getAllRulesInfo();
        System.out.println("testing ------- " + result);
        assertEquals(result, emptyHasmap);
    }

    /**
     * Test save two timed rules
     */
    @Test
    public void testAddRules() {

        // save 2 timed rules
        JSONObject body = new JSONObject();
        String ruleName = "timer_test_rule1@ttestservice1/test/timerRule1";
        String ruleText = "context ctxt$ttestservice1$test$timerRule1 select \"timer_test_rule1\" as ruleName, *, current_timestamp() as currentTS from pattern [every timer:interval(30 sec)]";
        body.put("name", ruleName);
        body.put("text", ruleText);
        instance.saveTimeRules(body.toString());
        JSONObject body2 = new JSONObject();
        String ruleName2 = "timer_test_rule2@ttestservice2/test/timerRule2";
        String ruleText2 = "context ctxt$ttestservice2$test$timerRule2 select \"timer_test_rule2\" as ruleName, *, current_timestamp() as currentTS from pattern [every timer:interval(90 sec)]";
        body2.put("name", ruleName2);
        body2.put("text", ruleText2);
        instance.saveTimeRules(body2.toString());

        HashMap<String, JSONObject> result = instance.getAllRulesInfo();

        assertEquals(2, result.size());
        JSONObject r1 = result.get("timer_test_rule1");
        JSONObject r2 = result.get("timer_test_rule2");
        assertNotNull(r1);
        assertNotNull(r2);
        assertEquals(r1.get("service"), "ttestservice1");
        assertEquals(r2.get("service"), "ttestservice2");
        assertEquals(r1.get("subservice"), "/test/timerRule1");
        assertEquals(r2.get("subservice"), "/test/timerRule2");
        assertEquals(r1.get("name"), ruleName);
        assertEquals(r2.get("name"), ruleName2);
        assertEquals(r1.get("text"), ruleText);
        assertEquals(r2.get("text"), ruleText2);
    }

    /**
     * Test remove a timed rule
     */
    @Test
    public void testRemoveRule() {

        // save 2 timed rules
        JSONObject body = new JSONObject();
        String ruleName = "timer_test_rule1@ttestservice1/test/timerRule1";
        String ruleText = "context ctxt$ttestservice1$test$timerRule1 select \"timer_test_rule1\" as ruleName, *, current_timestamp() as currentTS from pattern [every timer:interval(30 sec)]";
        body.put("name", ruleName);
        body.put("text", ruleText);
        instance.saveTimeRules(body.toString());
        JSONObject body2 = new JSONObject();
        String ruleName2 = "timer_test_rule2@ttestservice2/test/timerRule2";
        String ruleText2 = "context ctxt$ttestservice2$test$timerRule2 select \"timer_test_rule2\" as ruleName, *, current_timestamp() as currentTS from pattern [every timer:interval(90 sec)]";
        body2.put("name", ruleName2);
        body2.put("text", ruleText2);
        instance.saveTimeRules(body2.toString());


        HashMap<String, JSONObject> result1 = instance.getAllRulesInfo();
        assertEquals(2, result1.size());

        instance.removeTimeRule("timer_test_rule1");
        HashMap<String, JSONObject> result = instance.getAllRulesInfo();

        assertEquals(1, result.size());
        JSONObject r1 = result.get("timer_test_rule1");
        JSONObject r2 = result.get("timer_test_rule2");
        assertNull(r1);
        assertNotNull(r2);
        assertEquals(r2.get("service"), "ttestservice2");
        assertEquals(r2.get("subservice"), "/test/timerRule2");
        assertEquals(r2.get("name"), ruleName2);
        assertEquals(r2.get("text"), ruleText2);
    }

    /**
     * Test remove all timed rules
     */
    @Test
    public void testCleanAllRules() {

        // save 2 timed rules
        JSONObject body = new JSONObject();
        String ruleName = "timer_test_rule1@ttestservice1/test/timerRule1";
        String ruleText = "context ctxt$ttestservice1$test$timerRule1 select \"timer_test_rule1\" as ruleName, *, current_timestamp() as currentTS from pattern [every timer:interval(30 sec)]";
        body.put("name", ruleName);
        body.put("text", ruleText);
        instance.saveTimeRules(body.toString());
        JSONObject body2 = new JSONObject();
        String ruleName2 = "timer_test_rule2@ttestservice2/test/timerRule2";
        String ruleText2 = "context ctxt$ttestservice2$test$timerRule2 select \"timer_test_rule2\" as ruleName, *, current_timestamp() as currentTS from pattern [every timer:interval(90 sec)]";
        body2.put("name", ruleName2);
        body2.put("text", ruleText2);
        instance.saveTimeRules(body2.toString());

        instance.cleanAllRules();
        HashMap<String, JSONObject> result = instance.getAllRulesInfo();

        assertEquals(0, result.size());
    }
}


