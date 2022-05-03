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

import com.espertech.esper.runtime.client.*;
import com.espertech.esper.common.client.*;
import com.espertech.esper.common.client.util.StatementProperty;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.option.StatementNameOption;
import com.espertech.esper.compiler.client.option.StatementNameContext;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.common.client.util.EventTypeBusModifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import com.espertech.esper.common.client.configuration.*;
import com.espertech.esper.common.client.configuration.common.*;
import com.espertech.esper.common.client.configuration.compiler.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.logging.log4j.ThreadContext;


import com.telefonica.iot.perseo.utils.DateTimeUtils;

/**
 *
 * @author brox
 */
public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);
    private static final String EPSERV_ATTR_NAME = "epService";

    /**
     * Add an initialized EPServiceProvider to an ServletContext if it is not
     * already
     *
     * @param sc ServleContext to add ESPServiceProvider
     * @return the initialized EPServiceProvider
     */
    public static synchronized EPRuntime initEPService(ServletContext sc) {        
        com.espertech.esper.common.client.configuration.Configuration configuration = new com.espertech.esper.common.client.configuration.Configuration();
        configuration.getCompiler().getExpression().setUdfCache(false);
        configuration.getCompiler().getByteCode().setAllowSubscriber(true);
        configuration.getCompiler().getByteCode().setAccessModifiersPublic();
        configuration.getCompiler().getByteCode().setBusModifierEventType(EventTypeBusModifier.BUS);
        configuration.getRuntime().getThreading().setInternalTimerMsecResolution(com.telefonica.iot.perseo.Configuration.getInternalTimerMsecResolution());
        EPRuntime epService = (EPRuntime) sc.getAttribute(EPSERV_ATTR_NAME);
        if (epService == null) {

            Map<String, Object> def = new HashMap<String, Object>();
            def.put("id", String.class);
            def.put("type", String.class);
            def.put(Constants.SUBSERVICE_FIELD, String.class);
            def.put(Constants.SERVICE_FIELD, String.class);

            configuration.getCommon().addEventType(Constants.IOT_EVENT, def);

            // Add perseo-utils library
            configuration.getCommon().addImport("com.telefonica.iot.perseo.utils.*");

            ConfigurationCompiler cfgCpl = configuration.getCompiler();

            // Add Single row function for perseo-utils functions
            try {
                cfgCpl.addPlugInSingleRowFunction("getNextSunrise",
                        "com.telefonica.iot.perseo.utils.DateTimeUtils",
                        "getNextSunrise", ConfigurationCompilerPlugInSingleRowFunction.ValueCache.DISABLED);
                cfgCpl.addPlugInSingleRowFunction("getNextSunset",
                        "com.telefonica.iot.perseo.utils.DateTimeUtils",
                        "getNextSunset", ConfigurationCompilerPlugInSingleRowFunction.ValueCache.DISABLED);
                cfgCpl.addPlugInSingleRowFunction("getMilisToNextSunrise",
                        "com.telefonica.iot.perseo.utils.DateTimeUtils",
                        "getMilisToNextSunrise", ConfigurationCompilerPlugInSingleRowFunction.ValueCache.DISABLED);
                cfgCpl.addPlugInSingleRowFunction("getMilisToNextSunset",
                        "com.telefonica.iot.perseo.utils.DateTimeUtils",
                        "getMilisToNextSunset", ConfigurationCompilerPlugInSingleRowFunction.ValueCache.DISABLED);
                cfgCpl.addPlugInSingleRowFunction("dateToUTC",
                        "com.telefonica.iot.perseo.utils.DateTimeUtils",
                        "dateToUTC", ConfigurationCompilerPlugInSingleRowFunction.ValueCache.DISABLED);
                cfgCpl.addPlugInSingleRowFunction("timeToUTC",
                        "com.telefonica.iot.perseo.utils.DateTimeUtils",
                        "timeToUTC", ConfigurationCompilerPlugInSingleRowFunction.ValueCache.DISABLED);
                cfgCpl.addPlugInSingleRowFunction("getSecondsToNextSunset",
                        "com.telefonica.iot.perseo.utils.DateTimeUtils",
                        "getSecondsToNextSunset", ConfigurationCompilerPlugInSingleRowFunction.ValueCache.DISABLED);
                cfgCpl.addPlugInSingleRowFunction("getSecondsToNextSunrise",
                        "com.telefonica.iot.perseo.utils.DateTimeUtils",
                        "getSecondsToNextSunrise", ConfigurationCompilerPlugInSingleRowFunction.ValueCache.DISABLED);
            } catch (ConfigurationException e) {
                logger.error(e.getMessage());
            }

            epService = EPRuntimeProvider.getDefaultRuntime(configuration);

            sc.setAttribute(EPSERV_ATTR_NAME, epService);
        }
        return epService;
    }

    /**
     * Delete the EPServiceProvider from the ServletContext
     *
     * @param sc ServleContext to add ESPServiceProvider
     */
    public static synchronized void destroyEPService(ServletContext sc) {
        EPRuntime epService = (EPRuntime) sc.getAttribute(EPSERV_ATTR_NAME);
        if (epService != null) {
            epService.destroy();
            sc.removeAttribute(EPSERV_ATTR_NAME);
        }
    }

    /**
     * Converts a JSONArray to a ArrayList of Object.
     *
     * @param jsonArray JSONArray to convert
     * @return ArrayList of Object
     */
    public static ArrayList<Object> JSONArray2ArrayList(JSONArray jsonArray){
        ArrayList<Object> list = new ArrayList<Object>();
        if (jsonArray != null) {
            int len = jsonArray.length();
            for (int i=0;i<len;i++){
                list.add(jsonArray.get(i));
            }
        }
        return list;
    }

    /**
     * Converts a JSONObject to a map of String to Object. Nested JSONObject are
     * converted to Map too.
     *
     * @param jo JSONObject to convert
     * @return Map with key String an value Object
     */
    public static Map<String, Object> JSONObject2Map(JSONObject jo) {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator it = jo.keys();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object o = jo.get(key);
            if (o instanceof JSONArray) {
                map.put(key, JSONArray2ArrayList((JSONArray) o));
            } else if (o instanceof JSONObject) {
                map.put(key, JSONObject2Map((JSONObject) o));
            } else {
                map.put(key, o == JSONObject.NULL ? null : o);
            }
        }
        return map;
    }

    /**
     * Converts an Event to a JSONObject. In case of error the returned object
     * will containf an 'errors' field with a "map" key-> error generated by the
     * key
     *
     * @param event Esper event
     * @return JSONObject
     */
    public static JSONObject Event2JSONObject(EventBean event) {
        JSONObject jo = new JSONObject();
        Map<String, Object> errors = new HashMap<String, Object>();
        String[] propertyNames = event.getEventType().getPropertyNames();
        for (String propertyName : propertyNames) {
            try {
                Object objProp = event.get(propertyName);
                jo.put(propertyName, objProp == null ? JSONObject.NULL : objProp);
            } catch (JSONException je) {
                errors.put(propertyName, je.getMessage());
                logger.error(je.getMessage());
            }
        }
        if (!errors.isEmpty()) {
            jo.put("errors", errors);
        }
        return jo;
    }

    /**
     * Converts an Esper Statement to a JSONObject. The resulting object
     * contains fields for 'name','text', `state' and 'timeLastStateChange'
     *
     * @param st Esper statement
     * @return JSONObject
     */
    public static JSONObject Statement2JSONObject(EPStatement st, EPDeploymentService epService, String deploymentId) {
        if (st == null) {
            return null;
        }
        JSONObject jo = new JSONObject()
                .put("name", st.getName())
                .put("text", st.getProperty(StatementProperty.EPL))
                .put("state", st.isDestroyed())
                .put("timeLastStateChange", epService.getDeployment(deploymentId).getLastUpdateDate().getTime());
        return jo;
    }

    /**
     * Makes an HTTP POST to an URL sending an body. The URL and body are
     * represented as String.
     *
     * @param urlStr String representation of the URL
     * @param content Styring representation of the body to post
     *
     * @return if the request has been accompished
     */
    
    public static boolean DoHTTPPost(String urlStr, String content) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setDoOutput(true);
            urlConn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            urlConn.setRequestProperty(Constants.CORRELATOR_HEADER, ThreadContext.get(Constants.CORRELATOR_ID));
            urlConn.setRequestProperty(Constants.SERVICE_HEADER, ThreadContext.get(Constants.SERVICE_FIELD));
            urlConn.setRequestProperty(Constants.SUBSERVICE_HEADER, ThreadContext.get(Constants.SUBSERVICE_FIELD));
            urlConn.setRequestProperty(Constants.REALIP_HEADER, ThreadContext.get(Constants.REALIP_FIELD));
            
            OutputStreamWriter printout = new OutputStreamWriter(urlConn.getOutputStream(), Charset.forName("UTF-8"));
            printout.write(content);
            printout.flush();
            printout.close();

            int code = urlConn.getResponseCode();
            String message = urlConn.getResponseMessage();
            logger.debug(String.format("action http response %s %s",code,message));
            if (code / 100 == 2) {
                InputStream input = urlConn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                for (String line; (line = reader.readLine()) != null;) {
                    logger.info(String.format("action response body: %s", line));
                }
                input.close();
                return true;

            } else {
                logger.error(String.format("action response is not OK: %s %s",code,message));
                InputStream error = urlConn.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(error));
                for (String line; (line = reader.readLine()) != null;) {
                    logger.error(String.format("action error response body: %s", line));
                }
                error.close();
                return false;
            }
        } catch (MalformedURLException me) {
            logger.error(String.format("exception MalformedURLException: %s",me));
            return false;
        } catch (IOException ioe) {
            logger.error(String.format("exception IOException: %s",ioe));
            return false;
        }
    }

    /**
     * Capture correlator and generate
     *
     * @param req HttpServletRequest incomming request
     */
    public static void putCorrelatorAndTrans(HttpServletRequest req) {
        String correlatorId = req.getHeader(Constants.CORRELATOR_HEADER);
        String transId = UUID.randomUUID().toString();
        if (correlatorId == null) {
            correlatorId = transId;
        }
        ThreadContext.put(Constants.TRANSACTION_ID, transId);
        ThreadContext.put(Constants.CORRELATOR_ID, correlatorId);
        String service = req.getHeader(Constants.SERVICE_HEADER);
        if (service == null) {
            service = "?";
        }
        ThreadContext.put(Constants.SERVICE_FIELD, service);
       
        String subservice = req.getHeader(Constants.SUBSERVICE_HEADER);
        if (subservice == null) {
            subservice = "?";
        }
        ThreadContext.put(Constants.SUBSERVICE_FIELD, subservice);
        
        {
            String realIP = req.getHeader(Constants.REALIP_HEADER);

            if (realIP == null) {
                realIP = req.getRemoteAddr();
            }
            ThreadContext.put(Constants.REALIP_FIELD, realIP);
        }
    }

    /**
     * Return body as a String
     *
     * @param request HttpServletRequest incomming request
     * @return String (body of the request)
     * @throws java.io.IOException
     *
     */
    public static String getBodyAsString(HttpServletRequest request) throws IOException {
        logger.debug(String.format("request.getCharacterEncoding() %s", request.getCharacterEncoding()));
        if (request.getCharacterEncoding() == null) {
            request.setCharacterEncoding("UTF-8");
        }
        StringBuilder sb = new StringBuilder();
        BufferedReader br = request.getReader();
        String read = br.readLine();
        while (read != null) {
            sb.append(read);
            read = br.readLine();
        }
        return sb.toString();
    }

    /**
     * It implements a method that, following the same mechanism as putCorrelatorAndTrans, uses org.slf4j.MDC
     * to store the necessary headers, which will be necessary for the request in pursuit of activation of
     * an action. The same thread will use the MDC to extract this data.
     * @param rule JSON with the rule information
     */
    public static void setTimerRuleHeaders(JSONObject rule) {

        String id = UUID.randomUUID().toString();
        ThreadContext.put(Constants.TRANSACTION_ID, id);
        ThreadContext.put(Constants.CORRELATOR_ID, id);
        String service = (String) rule.get("service");
        if (service == null) {
            service = "?";
        }
        ThreadContext.put(Constants.SERVICE_FIELD, service);

        String subservice = (String) rule.get("subservice");
        if (subservice == null) {
            subservice = "?";
        }
        ThreadContext.put(Constants.SUBSERVICE_FIELD, subservice);

        ThreadContext.put(Constants.REALIP_FIELD, "Perseo-core-timer-rule");
    }

    /**
     * Validate an URL
     *
     * @param url The url to validate
     * @return True if the url is valid false if is not
     *
     */
    public static Boolean isValidURL(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /*
     * A name resolver
     */
    private static class MyStatementNameResolver implements StatementNameOption {
        private String name;
        public MyStatementNameResolver(String n) {
            this.name = n;
        }
        public String getValue(StatementNameContext env) {
            return this.name;
        }
    }

    /*
     * A method that can replace createEPL calls of Esper-7
     */
    public static EPDeployment compileDeploy(EPRuntime runtime, String epl, String name) {

        try {
            // Obtain a copy of the engine configuration
            com.espertech.esper.common.client.configuration.Configuration configuration = runtime.getConfigurationDeepCopy();
            configuration.getCompiler().getByteCode().setAllowSubscriber(true);
            configuration.getCompiler().getByteCode().setAccessModifiersPublic();
            configuration.getCompiler().getByteCode().setBusModifierEventType(EventTypeBusModifier.BUS);
            Map<String, Object> def = new HashMap<String, Object>();
            def.put("id", String.class);
            def.put("type", String.class);
            def.put(Constants.SUBSERVICE_FIELD, String.class);
            def.put(Constants.SERVICE_FIELD, String.class);
            configuration.getCommon().addEventType("iotEvent", def);

            // Build compiler arguments
            CompilerArguments args = new CompilerArguments(configuration);

            args.getOptions().setStatementName(new MyStatementNameResolver(name));

            // Make the existing EPL objects available to the compiler
            args.getPath().add(runtime.getRuntimePath());

            // Compile
            EPCompiled compiled = EPCompilerProvider.getCompiler().compile(epl, args);

            // Return the deployment
            return runtime.getDeploymentService().deploy(compiled);
        }
        catch (Exception ex) {
            throw new EPException(ex.getMessage());
        }
    }

    /*
     * A method that can replace getStatement calls of Esper-7:
     */
    public static EPStatement getStatementFromDeployService(EPDeploymentService epDeployService, String ruleName) {
        try {
            EPStatement st = null;
            String[] deploymentIds = epDeployService.getDeployments();
            for (String deploymentId : deploymentIds) {
                st = epDeployService.getStatement(deploymentId, ruleName);
                if (st != null) {
                    break;
                }
            }
            return st;
        }
        catch (Exception ex) {
            throw new EPException(ex.getMessage());
        }
    }

}
