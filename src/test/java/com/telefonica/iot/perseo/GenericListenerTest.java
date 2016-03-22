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

import com.espertech.esper.client.EventBean;
import com.telefonica.iot.perseo.test.EventBeanMock;
import java.util.HashMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author brox
 */
public class GenericListenerTest {

    public GenericListenerTest() {
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
     * Test of update method, of class GenericListener.
     */
    @Test
    public void testUpdate() {
        System.out.println("update");
        EventBean[] newEvents = new EventBean[0];
        EventBean[] oldEvents = new EventBean[0];
        GenericListener instance = new GenericListener();
        instance.update(newEvents, oldEvents);
        HashMap<String, Object> m = new HashMap();
        m.put("one", "1");
        m.put("two", 2);
        EventBean event = new EventBeanMock(m);
        newEvents = new EventBean[]{event};
        instance.update(newEvents, oldEvents);
    }

}
