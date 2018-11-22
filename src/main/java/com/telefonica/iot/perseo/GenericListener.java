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

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.client.UpdateListener;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author brox
 */
public class GenericListener implements UpdateListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericListener.class);
    
     /**
     * Implements method to execute an action whe a rule is fired . It makes an
     * HTTP POST to the configured URL sending the JSON representation of the
     * Events fired by the rule. If the activated rule is a timed rule, this
     * method set the correct headers for the request.
     * @param newEvents new events entering the window
     * @param oldEvents old events leaving the window
     */
    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents) {
        try {
            HashMap<String, JSONObject> rules = TimeRulesStore.getInstance().getAllRulesInfo();
            for (EventBean event : newEvents) {

                JSONObject jo = Utils.Event2JSONObject(event);
                Map<String, Object> eventMap = Utils.JSONObject2Map(jo);

                // Get Rule Information from TimeRulesStore
                JSONObject rule = TimeRulesStore.getInstance().getRuleInfo((String) eventMap.get("ruleName"));

                // Alt. if event.getEventType().getName().endsWith("_wrapoutwild_") -> Timed rule?
                if (rule != null) {

                    // Is a timed Rule. Set special headers using rule saved information
                    Utils.setTimerRuleHeaders(rule);
                    LOGGER.info("Firing temporal rule: " + event);

                } else {

                    LOGGER.info("Firing Rule: " + event);
                }

                LOGGER.debug("result errors: " + jo.optJSONObject("errors"));
                LOGGER.debug("result json: " + jo);

                boolean ok = Utils.DoHTTPPost(Configuration.getActionURL(), jo.toString());
                if (!ok) {
                    LOGGER.error("action post failed");
                }
            }
        } catch (PropertyAccessException pae) {
            LOGGER.error("doing action " + pae);
        }
    }
}
