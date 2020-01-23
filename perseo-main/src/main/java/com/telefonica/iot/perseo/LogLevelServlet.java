/**
* Copyright 2016 Telefonica Investigación y Desarrollo, S.A.U
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.owasp.encoder.Encode;


/**
 *
 * @author brox
 */
@WebServlet(name = "LogeLevel", loadOnStartup = 2, urlPatterns = {"/admin/log"})
public class LogLevelServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(LogLevelServlet.class);
    
    private static final Map<String, Level> levels = new HashMap<String, Level>();
    
    private static final Object mutex = new Object();
    
    static {
        levels.put("DEBUG", Level.DEBUG);
        levels.put("INFO", Level.INFO);
        levels.put("WARN", Level.WARN);
        levels.put("WARNING", Level.WARN);
        levels.put("ERROR", Level.ERROR);
        levels.put("FATAL", Level.FATAL);
    }
    
    /**
     * Handles the HTTP <code>PUT</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String levelName = request.getParameter("level");
            logger.info(String.format("changing log level to %s",levelName));
            Level level = levels.get(levelName);
            if (level == null) {
                logger.error(String.format("invalid log level: %s",levelName));
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getOutputStream().print("{\"errorMessage\":\"invalid log level\"}");
                return;
            }
            synchronized (mutex) {
                LogManager.getRootLogger().setLevel(level);
            }
            response.setStatus(HttpServletResponse.SC_OK);      
        } catch (IOException e) {
        	logger.error("IOException in log level");
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	return;
        }
    	
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.debug("getting log level");
        synchronized (mutex) {
             try {
                String currentLevel = LogManager.getRootLogger().getLevel().toString();
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_OK);
                response.getOutputStream().print(String.format("{\"level\":\"%s\"}",Encode.forHtmlContent(currentLevel)));
            } catch (IOException e) {
                logger.error("IOException in log level");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
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
        return "Change log levet at runtime";
    }
}

