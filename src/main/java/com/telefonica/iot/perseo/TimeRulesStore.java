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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public final class TimeRulesStore {

    private static TimeRulesStore INSTANCE;
    private static final Logger logger = LoggerFactory.getLogger(EventsServlet.class);
    private HashMap<String, JSONObject> rulesInfo = new HashMap<String, JSONObject>();

    private TimeRulesStore() {
    }

    public static TimeRulesStore getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new TimeRulesStore();
        }

        return INSTANCE;
    }

    /**
     * Save 'timed rules' name, service and subservice for later use of this information when
     * the timed events are launched from GenericListener
     *
     * @param body POST or PUT body with rules data
     */
    public void saveTimeRules(String body) {

        if (body.equals("")) {
            return;
        } else {

            JSONArray jo;
            try {
                // For updateAll requests from perseo-fe
                jo = new JSONArray(body);
            } catch (JSONException e) {
                // For new Rules requests from perseo-fe
                jo = new JSONArray();
                jo.put(new JSONObject(body));
            }

            String strName;
            String ruleText;
            for (int i = 0; i < jo.length(); i++) {

                try {
                    strName = (String) jo.getJSONObject(i).get("name");
                    ruleText = (String) jo.getJSONObject(i).get("text");
                } catch (JSONException e) {
                    // Invalid Rule
                    continue;
                }

                // Only "Timed Rules"
                if (!strName.startsWith("ctxt$") && isTimeRule(ruleText)) {

                    // Current active rule. Save header information by name
                    List<String> ruleName = new ArrayList(Arrays.asList(strName.split("@")));
                    List<String> context = new ArrayList(Arrays.asList(ruleName.get(1).split("/")));
                    jo.getJSONObject(i).put("service", context.get(0));
                    context.remove(0);
                    jo.getJSONObject(i).put("subservice", "/" + String.join("/", context));
                    rulesInfo.put(ruleName.get(0), jo.getJSONObject(i));
                }
            }
        }
    }

    /**
     * Check if a ruleText is a 'timed rule'
     *
     * @param ruleText The rule text
     * @return true if ruleText is a 'timed rule', false otherwise
     */
    private boolean isTimeRule(String ruleText) {
        // Detect timed rules, searching "timer:XX" patterns or Match_Recognize interval patterns
        // http://esper.espertech.com/release-6.1.0/esper-reference/html/match-recognize.html#match-recognize-interval
        // http://esper.espertech.com/release-6.1.0/esper-reference/html/event_patterns.html#pattern-timer-interval
        // http://esper.espertech.com/release-6.1.0/esper-reference/html/event_patterns.html#pattern-timer-at
        // http://esper.espertech.com/release-6.1.0/esper-reference/html/event_patterns.html#pattern-timer-schedule
        return ruleText.toLowerCase().contains("timer:") ||
                (ruleText.toLowerCase().contains("match_recognize") && ruleText.toLowerCase().contains("interval"));
    }

    /**
     * Return all 'timed rules' information saved
     *
     * @return All rules information by name
     */
    public HashMap<String, JSONObject> getAllRulesInfo() {
        return rulesInfo;
    }

    /**
     * Get a specific 'timed rule' information
     *
     * @param ruleName The rule name. Can include optionally '@context...' in the name
     * @return The rule information
     */
    public JSONObject getRuleInfo(String ruleName) {

        if (ruleName == null) {
            return null;
        }
        return rulesInfo.get(ruleName.split("@")[0]);
    }

    /**
     * Delete the information relative to specific 'timed rule'
     *
     * @param ruleName The rule name. Can include optionally '@context...' in the name
     */
    public void removeTimeRule(String ruleName) {
        if (ruleName != null) {
            rulesInfo.remove(ruleName.split("@")[0]);
        }
    }

    /**
     * Delete all the 'timed rules' information
     *
     */
    public void cleanAllRules() {
        rulesInfo = new HashMap<String, JSONObject>();
    }
}
