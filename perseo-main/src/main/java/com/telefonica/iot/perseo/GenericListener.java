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
* Modified by: Carlos Blanco - Future Internet Consulting and Development Solutions (FICODES)
*/

package com.telefonica.iot.perseo;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.PropertyAccessException;
import com.espertech.esper.runtime.client.UpdateListener;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.EPRuntime;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author brox
 */
public class GenericListener implements UpdateListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericListener.class);
    
     /**
     * Implements method to execute an action when a rule is fired . It makes an
     * HTTP POST to the configured URL sending the JSON representation of the
     * Events fired by the rule. If the activated rule is a timed rule, this
     * method set the correct headers for the request.
     * @param newEvents new events entering the window
     * @param oldEvents old events leaving the window
     */
    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement statement, EPRuntime epruntime) {
        try {          
            for (EventBean event : newEvents) {

                JSONObject jo = Utils.Event2JSONObject(event);
                Map<String, Object> eventMap = Utils.JSONObject2Map(jo);

                // Get Rule Information from TimeRulesStore
                String ruleName = (String) eventMap.get("ruleName");

                // TBD: include service and subservice
                JSONObject rule = TimeRulesStore.getInstance().getRuleInfo(ruleName);
                LOGGER.debug(String.format("Rule name: %s event %s jo %s",
                                           ruleName, event, jo));

                // Alt. if event.getEventType().getName().endsWith("_wrapoutwild_") -> Timed rule?
                if (rule != null) {

                    // Is a timed Rule. Set special headers using rule saved information
                    Utils.setTimerRuleHeaders(rule);
                    // Timed rule is stored in TImesRuleStored with normalized/unique name)
                    // But simple rule name should be used in order to post perseo-fe
                    List<String> name = new ArrayList(Arrays.asList(ruleName.split("@")));
                    jo.put("ruleName", name.get(0));
                    LOGGER.info(String.format("Firing temporal rule: %s with name %s from event: %s",
                                               rule.toString(), ruleName, event));

                } else {
                    LOGGER.info(String.format("Firing Rule with name: %s from Event: %s Jo: %s",
                                              ruleName, event, jo));
                }
                JSONObject errors = jo.optJSONObject("errors");
                if (errors != null) {
                    LOGGER.info(String.format("result errors: %s", errors));
                }
                LOGGER.debug(String.format("result json: %s", jo));

                boolean ok = Utils.DoHTTPPost(Configuration.getActionURL(), jo.toString());
                if (!ok) {
                    LOGGER.error("action post failed");
                }
            }
        } catch (PropertyAccessException pae) {
            LOGGER.error(String.format("doing action %s",pae));
        }
    }
}
