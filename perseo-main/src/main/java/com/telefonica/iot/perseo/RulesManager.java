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

//import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.runtime.client.EPDeploymentService;
import com.espertech.esper.runtime.client.EPDeployment;
//import com.espertech.esper.client.EPException;
import com.espertech.esper.common.client.EPException;
//import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.runtime.client.EPRuntime;
//import com.espertech.esper.client.EPStatement;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.EPDeployException;
import com.espertech.esper.common.client.util.StatementProperty;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brox
 */
public class RulesManager {

    private static final Logger logger = LoggerFactory.getLogger(RulesManager.class);

    /**
     * Get a rule by name
     *
     * @param epService Esper provider containing rules
     * @param ruleName Name of the rule
     *
     * @return Result object with a code and a JSON response
     */
    //public static synchronized Result get(EPServiceProvider epService, String ruleName) {
    public static synchronized Result get(EPRuntime epService, String ruleName) {
        try {
            logger.debug(String.format("rule asked for: %s", ruleName));
            ruleName = ruleName == null ? "" : ruleName;
            //EPAdministrator epa = epService.getEPAdministrator();
            EPDeploymentService epa = epService.getDeploymentService();

            if (ruleName.length() != 0) {
                JSONArray ja = new JSONArray();
                String[] deploymentIds = epa.getDeployments();
                for (String deploymentId : deploymentIds) {
                    EPStatement st = epa.getStatement(deploymentId, ruleName);
                    if (st != null) {
                        ja.put(Utils.Statement2JSONObject(st));
                    }
                }
                if (ja.length() > 0) {
                    return new Result(HttpServletResponse.SC_OK, ja.toString());
                } else {
                    return new Result(HttpServletResponse.SC_NOT_FOUND,
                                      String.format("{\"error\":\"%s not found\"}%n",
                                                    ruleName));
                }
                // EPStatement st = epa.getStatement(ruleName);
                // if (st == null) {
                //     return new Result(HttpServletResponse.SC_NOT_FOUND,
                //             String.format("{\"error\":\"%s not found\"}%n",
                //                     ruleName));
                // } else {
                //     return new Result(HttpServletResponse.SC_OK,
                //             Utils.Statement2JSONObject(st).toString());
                // }
            } else {
                JSONArray ja = new JSONArray();
                String[] deploymentIds = epa.getDeployments();
                for (String deploymentId : deploymentIds) {
                    EPDeployment deployment = epa.getDeployment(deploymentId);
                    EPStatement[] statements = deployment.getStatements();
                    for (EPStatement st : statements) {
                        logger.debug(String.format("getting rule %s", st.getName()));
                        ja.put(Utils.Statement2JSONObject(st));
                    }
                }
                // String[] sttmntNames = epa.getStatementNames();
                // JSONArray ja = new JSONArray();
                // for (String name : sttmntNames) {
                //     logger.debug(String.format("getting rule %s", name));
                //     EPStatement st = epa.getStatement(name);
                //     ja.put(Utils.Statement2JSONObject(st));
                // }
                return new Result(HttpServletResponse.SC_OK, ja.toString());
            }

        } catch (EPException epe) {
            logger.error(String.format("getting statement %s", epe));
            return new Result(HttpServletResponse.SC_BAD_REQUEST,
                    String.format("{\"error\":%s}%n",
                            JSONObject.valueToString(epe.getMessage())));
        }
    }

    /**
     * Make a new rule from a JSON representation of an object with a name field
     * and a text field
     *
     * @param epService Esper provider containing rules
     * @param text JSON text of the rule
     *
     * @return Result object with a code and a JSON response
     */
    //public static synchronized Result make(EPServiceProvider epService, String text) {
    public static synchronized Result make(EPRuntime epService, String text) {
        try {
            logger.debug(String.format("rule text: %s", text));
            org.json.JSONObject jo = new JSONObject(text);

            logger.debug(String.format("rule as JSONObject: %s", jo));
            String name = jo.optString("name", "");
            logger.info(String.format("post rule: %s",name));
            if ("".equals(name.trim())) {
                return new Result(HttpServletResponse.SC_BAD_REQUEST,
                        "{\"error\":\"missing name\"}");
            }
            String newEpl = jo.optString("text", "");
            if ("".equals(newEpl.trim())) {
                return new Result(HttpServletResponse.SC_BAD_REQUEST,
                        "{\"error\":\"missing text\"}");
            }
            logger.debug(String.format("statement name: %s",name));
            logger.debug(String.format("statement text: %s",newEpl));
         
            EPStatement statement;
            //EPStatement prevStmnt = epService.getEPAdministrator().getStatement(name);
            EPStatement prevStmnt;
            EPDeploymentService epa = epService.getDeploymentService();
            String[] deploymentIds = epa.getDeployments();
            for (String deploymentId : deploymentIds) {
                EPStatement st = epa.getStatement(deploymentId, name);
                if (st != null) {
                    prevStmnt = st;
                }
            }

            // Deployment for compile newEPL
            com.espertech.esper.common.client.configuration.Configuration configuration = new com.espertech.esper.common.client.configuration.Configuration();
            CompilerArguments arguments = new CompilerArguments(configuration);
            arguments.getPath().add(epService.getRuntimePath());
            EPCompiled epCompiled = EPCompilerProvider.getCompiler().compile(newEpl, arguments);
            EPDeployment deploymentForEPL;
            try {
                deploymentForEPL = epService.getDeploymentService().deploy(epCompiled);
            }
            catch (EPDeployException ex) {
                // handle exception here
                throw new RuntimeException(ex);
            }

            if (prevStmnt == null) {
                logger.debug(String.format("found new statement: %s",name));

                //statement = epService.getEPAdministrator().createEPL(newEpl, name);
                statement = epService.getDeploymentService().getStatement(deploymentForEPL.getDeploymentId(), name);
                logger.debug(String.format("statement json: %s", Utils.Statement2JSONObject(statement)));
                statement.addListener(new GenericListener());
            } else {
                //String oldEpl = prevStmnt.getText();
                String oldEpl = prevStmnt.getProperty(StatementProperty.EPL);
                logger.debug(String.format("old epl: %s", oldEpl));
                if (!newEpl.equals(oldEpl)) {
                    logger.debug(String.format("found changed statement: %s",name));
                    //prevStmnt.destroy();
                    epa.undeploy();
                    logger.debug(String.format("deleted statement: %s",name));
                    //statement = epService.getEPAdministrator().createEPL(newEpl, name);
                    statement = epService.getDeploymentService().getStatement(deploymentForEPL.getDeploymentId(), name);
                    
                    logger.debug(String.format("re-created statement: %s", name));
                    logger.debug(String.format("statement json: %s", Utils.Statement2JSONObject(statement)));
                    statement.addListener(new GenericListener());
                } else {
                    logger.debug(String.format("found repeated statement: %s", name));
                    statement = prevStmnt;
                }
            }
            return new Result(HttpServletResponse.SC_OK,
                    Utils.Statement2JSONObject(statement).toString());
        } catch (EPException epe) {
            logger.error(String.format("creating statement %s", epe));
            return new Result(HttpServletResponse.SC_BAD_REQUEST,
                    String.format("{\"error\":%s}%n",
                            JSONObject.valueToString(epe.getMessage())));
        } catch (JSONException je) {
            logger.error(String.format("creating statement %s",je));
            return new Result(HttpServletResponse.SC_BAD_REQUEST,
                    String.format("{\"error\":%s}%n",
                            JSONObject.valueToString(je.getMessage())));
        }
    }

    /**
     * Add all rules from a JSON representation of an array of objects with a
     * name field and a text field. If the rule exists with the same EPL text, it
     * is not re-created. Pre-existing rules not in the passed array and older
     * than a threshold (maxAge) will be deleted
     *
     * @param epService Esper provider containing rules
     * @param text JSON text of the rule
     *
     * @return Result object with a code and a JSON response
     */
    //public static synchronized Result updateAll(EPServiceProvider epService, String text) {
    public static synchronized Result updateAll(EPRuntime epService, String text) {
        try {
            long maxAge = com.telefonica.iot.perseo.Configuration.getMaxAge();
            logger.debug(String.format("rule block text: %s",text));
            org.json.JSONArray ja = new JSONArray(text);
            logger.debug(String.format("rules as JSONArray: %s", ja));
            logger.info(String.format("put rules+contexts: %s", ja.length()));
            Map<String, String> newOnes = new LinkedHashMap<String, String>();
            Set<String> oldOnesNames = new HashSet<String>();

            long now = System.currentTimeMillis();
            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);
                String name = jo.optString("name", "");
                if ("".equals(name.trim())) {
                    return new Result(HttpServletResponse.SC_BAD_REQUEST,
                            "{\"error\":\"missing name\"}");
                }
                String newEpl = jo.optString("text", "");
                if ("".equals(newEpl.trim())) {
                    return new Result(HttpServletResponse.SC_BAD_REQUEST,
                            "{\"error\":\"missing text\"}");
                }
                newOnes.put(name, newEpl);
            }

            //oldOnesNames.addAll(Arrays.asList(epService.getEPAdministrator().getStatementNames()));
            ArrayList<String> stNames = new ArrayList<String>();
            EPDeploymentService epa = epService.getDeploymentService();
            String[] deploymentIds = epa.getDeployments();
            for (String deploymentId : deploymentIds) {
                EPDeployment deployment = epa.getDeployment(deploymentId);
                EPStatement[] statements = deployment.getStatements();
                for (EPStatement st : statements) {
                    stNames.add(st.getName());
                }
            }
            oldOnesNames.addAll(Arrays.asList(stNames));



            for (String n : newOnes.keySet()) {
                String newEpl = newOnes.get(n);
                if (!oldOnesNames.contains(n)) {
                    logger.debug(String.format("found new statement: %s", n));

                    // Deployment for compile newEPL
                    Configuration configuration = new Configuration();
                    CompilerArguments arguments = new CompilerArguments(configuration);
                    arguments.getPath().add(epService.getRuntimePath());
                    EPCompiled epCompiled = EPCompilerProvider.getCompiler().compile(newEpl, arguments);
                    EPDeployment deploymentForEPL;
                    try {
                        deploymentForEPL = epService.getDeploymentService().deploy(epCompiled);
                    }
                    catch (EPDeployException ex) {
                        // handle exception here
                        throw new RuntimeException(ex);
                    }

                    //EPStatement statement = epService.getEPAdministrator().createEPL(newEpl, n);
                    EPStatement statement = epService.getDeploymentService().getStatement(deploymentForEPL.getDeploymentId(), n);
                    logger.debug(String.format("statement json: %s", Utils.Statement2JSONObject(statement)));
                    statement.addListener(new GenericListener());
                } else {
                    //EPStatement prevStmnt = epService.getEPAdministrator().getStatement(n);
                    EPStatement prevStmnt;
                    EPDeploymentService epa = epService.getDeploymentService();
                    String[] deploymentIds = epa.getDeployments();
                    for (String deploymentId : deploymentIds) {
                        EPStatement st = epa.getStatement(deploymentId, n);
                        if (st != null) {
                            prevStmnt = st;
                        }
                    }

                    //String oldEPL = prevStmnt.getText();
                    String oldEPL = prevStmnt.getProperty(StatementProperty.EPL);
                    if (!oldEPL.equals(newOnes.get(n))) {
                        logger.debug(String.format("found changed statement: %s", n));
                        //prevStmnt.destroy();
                        epa.undeploy();
                        logger.debug(String.format("deleted statement: %s", n));
                        //EPStatement statement = epService.getEPAdministrator().createEPL(newEpl, n);
                        EPStatement statement = epService.getDeploymentService().getStatement(deploymentForEPL.getDeploymentId(), n);
                        logger.debug(String.format("re-created statement: %s" ,n));
                        logger.debug(String.format("statement json: %s", Utils.Statement2JSONObject(statement)));
                        statement.addListener(new GenericListener());
                    } else {
                        logger.debug(String.format("identical statement: %s", n));
                    }
                    oldOnesNames.remove(n);
                }
            }
            //Delete oldOnes if they are old enough
            for (String o : oldOnesNames) {
                //EPStatement prevStmnt = epService.getEPAdministrator().getStatement(o);
                EPStatement prevStmnt;
                EPDeploymentService epa = epService.getDeploymentService();
                String[] deploymentIds = epa.getDeployments();
                for (String deploymentId : deploymentIds) {
                    EPStatement st = epa.getStatement(deploymentId, o);
                    if (st != null) {
                        prevStmnt = st;
                    }
                }

                logger.debug(String.format("unexpected statement: %s", o));
                if (prevStmnt.getTimeLastStateChange() < now - maxAge) {
                    logger.debug(String.format("unexpected statement, too old: %s", o));
                    //prevStmnt.destroy();
                    epa.undeploy();
                    logger.debug(String.format("deleted garbage statement: %s", o));
                }
            }
            return new Result(HttpServletResponse.SC_OK, "{}");
        } catch (EPException epe) {
            logger.error(String.format("creating statement %s", epe));
            return new Result(HttpServletResponse.SC_BAD_REQUEST,
                    String.format("{\"error\":%s}%n",
                            JSONObject.valueToString(epe.getMessage())));
        } catch (JSONException je) {
            logger.error(String.format("creating statement %s", je));
            return new Result(HttpServletResponse.SC_BAD_REQUEST,
                    String.format("{\"error\":%s}%n",
                            JSONObject.valueToString(je.getMessage())));
        }
    }

    /**
     * Delete a rule by name. It does not return an error if the rule does not
     * exist
     *
     * @param epService Esper provider containing rules
     * @param ruleName Name of the rule
     *
     * @return Result object with a code and a JSON response
     */
    //public static synchronized Result delete(EPServiceProvider epService, String ruleName) {
    public static synchronized Result delete(EPRuntime epService, String ruleName) {            
        try {
            ruleName = ruleName == null ? "" : ruleName;
            logger.debug(String.format("delete rule: %s" ,ruleName));
            //EPAdministrator epa = epService.getEPAdministrator();
            EPDeploymentService epa = epService.getDeploymentService();

            if (ruleName.length() != 0) {
                //EPStatement st = epa.getStatement(ruleName);
                EPStatement st = null;
                String[] deploymentIds = epa.getDeployments();
                for (String deploymentId : deploymentIds) {
                    EPStatement currentst = epa.getStatement(deploymentId, ruleName);
                    if (currentst != null) {
                        st = currentst;
                        //st.destroy();
                        epa.undeploy();
                    }
                }

                //Allow to delete inexistent rule
                if (st != null) {
                    logger.debug(String.format("deleted statement: %s", ruleName));
                    return new Result(HttpServletResponse.SC_OK,
                            Utils.Statement2JSONObject(st).toString());
                } else {
                    logger.debug(String.format("asked for deleting inexistent statement: %s",ruleName));
                    return new Result(HttpServletResponse.SC_OK, "{}");
                }

            } else {
                return new Result(HttpServletResponse.SC_NOT_FOUND, "{\"error\":\"not rule specified for deleting\"}");
            }

        } catch (EPException epe) {
            logger.error(String.format("deleting statement %s", epe));
            return new Result(HttpServletResponse.SC_BAD_REQUEST,
                    String.format("{\"error\":%s}%n",
                            JSONObject.valueToString(epe.getMessage())));
        }
    }
}
