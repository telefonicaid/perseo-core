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

import com.espertech.esper.runtime.client.EPRuntime;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.UUID;
import javax.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.ThreadContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.encoder.Encode;

/**
 *
 * @author brox
 */
@WebServlet(name = "Events", loadOnStartup = 2, urlPatterns = {"/events"})
public class EventsServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(EventsServlet.class);
    private static EPRuntime epService;

    @Override
    public void init() {
        ThreadContext.put("id", UUID.randomUUID().toString());
        ThreadContext.put(Constants.CORRELATOR_ID, "n/a");
        ThreadContext.put(Constants.TRANSACTION_ID, "n/a");
        ServletContext sc = getServletContext();
        epService = Utils.initEPService(sc);
        logger.debug("init at events servlet");

    }

    @Override
    public void destroy() {
        Utils.destroyEPService(getServletContext());
        ThreadContext.put(Constants.CORRELATOR_ID, "n/a");
        ThreadContext.put(Constants.TRANSACTION_ID, "n/a");
        logger.debug("destroy at events servlet");
        ThreadContext.clearMap();
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Utils.putCorrelatorAndTrans(request);
        logger.debug("events doPost");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");        
        try {
            String eventText = Utils.getBodyAsString(request);
            logger.info(String.format("incoming event: %s", eventText));
            org.json.JSONObject jo = new JSONObject(eventText);
            logger.debug(String.format("event as JSONObject: %s", jo));
            Map<String, Object> eventMap = Utils.JSONObject2Map(jo);
            logger.debug(String.format("event as map: %s" , eventMap));
            epService.getEventService().sendEventMap(eventMap, Constants.IOT_EVENT);

            logger.debug(String.format("event was sent: %s", eventMap));
        } catch (Exception je) {
            try {
                logger.error(String.format("error: %s" ,je));
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getOutputStream().print(String.format("{\"error\":\"%s\"}%n", Encode.forHtmlContent(je.getMessage())));
            } catch (IOException exception) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }           
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Events server";
    }
}
