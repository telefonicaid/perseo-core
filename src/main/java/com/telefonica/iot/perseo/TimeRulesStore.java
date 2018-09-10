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

    public void saveTimeRules(String body) {

        if (body.equals("")) {
            return;
        } else {

            JSONArray jo;
            try {
                // For updateAll requests
                jo = new JSONArray(body);
            } catch (JSONException e) {
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

                // Only "Timer Rules"
                if (!strName.startsWith("ctxt$") && isTimeRule(ruleText)) {
                    // Curent active rule. save all headers by name
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

    private boolean isTimeRule(String ruleText) {
        // Detect Timer rules, searching "timer:XX" patterns or Match_Recognize interval patterns
        // http://esper.espertech.com/release-5.1.0/esper-reference/html/match-recognize.html#match-recognize-interval
        // http://esper.espertech.com/release-5.5.0/esper-reference/html/event_patterns.html#pattern-timer-interval
        // http://esper.espertech.com/release-5.5.0/esper-reference/html/event_patterns.html#pattern-timer-at
        // http://esper.espertech.com/release-5.5.0/esper-reference/html/event_patterns.html#pattern-timer-schedule
        return ruleText.toLowerCase().contains(" timer:") ||
                (ruleText.toLowerCase().contains(" match_recognize") && ruleText.toLowerCase().contains("interval"));
    }

    public HashMap<String, JSONObject> getAllRulesInfo() {
        return rulesInfo;
    }

    public JSONObject getRuleInfo(String ruleName) {

        if (ruleName == null) {
            return null;
        }
        return rulesInfo.get(ruleName.split("@")[0]);
    }

    public void removeTimeRule(String ruleName) {
        if (ruleName != null) {
            rulesInfo.remove(ruleName.split("@")[0]);
        }
    }

    public void cleanAllRules() {
        rulesInfo = new HashMap<String, JSONObject>();
    }
}
