/**
* Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
*
* This file is part of perseo-core project.
*
* perseo-core is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
* General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
* option) any later version.
*
* perseo-core is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
* implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
* for more details.
*
* You should have received a copy of the GNU Affero General Public License along with perseo-core. If not, see
* http://www.gnu.org/licenses/.
*
* For those usages not covered by the GNU Affero General Public License please contact with
* iot_support at tid dot es
*/

package es.tid.fiware.perseo;

import com.espertech.esper.client.EPServiceProvider;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brox
 */
@WebServlet(name = "Rules", loadOnStartup = 1, urlPatterns = {"/rules/*"})
public class RulesServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(RulesServlet.class);
    EPServiceProvider epService;

    @Override
    public void init() throws ServletException {
        MDC.put(Constants.CORRELATOR_ID, "n/a");
        MDC.put(Constants.TRANSACTION_ID, "n/a");
        ServletContext sc = getServletContext();
        epService = Utils.initEPService(sc);
        logger.debug("init at rules servlet");
    }

    @Override
    public void destroy() {
        MDC.put(Constants.CORRELATOR_ID, "n/a");
        MDC.put(Constants.TRANSACTION_ID, "n/a");
        Utils.destroyEPService(getServletContext());
        logger.debug("destroy at rules servlet");
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
        Utils.putCorrelatorAndTrans(request);
        PrintWriter out = response.getWriter();
        response.setContentType("application/json;charset=UTF-8");
        logger.info("get rule " + request.getPathInfo());
        String ruleName = request.getPathInfo();
        //request.getPathInfo() returns null or the extra path information
        //that follows the servlet path but precedes the query string and will
        //start with a "/" character. So, we remove it with .substring(1)
        ruleName = ruleName == null ? "/" : ruleName;
        Result r = RulesManager.get(epService, ruleName.substring(1));
        response.setStatus(r.getStatusCode());
        out.println(r.getMessage());
        out.close();

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
        PrintWriter out = response.getWriter();
        response.setContentType("application/json;charset=UTF-8");
        ServletInputStream sis = request.getInputStream();
        byte[] b = new byte[request.getContentLength()];
        sis.read(b, 0, b.length);
        String text = new String(b);
        logger.info("post rule " + text);
        Result r = RulesManager.make(epService, text);
        response.setStatus(r.getStatusCode());
        out.println(r.getMessage());
        out.close();
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
        Utils.putCorrelatorAndTrans(request);
        PrintWriter out = response.getWriter();
        response.setContentType("application/json;charset=UTF-8");
        ServletInputStream sis = request.getInputStream();
        byte[] b = new byte[request.getContentLength()];
        sis.read(b, 0, b.length);
        String text = new String(b);
        logger.info("put rule " + text);
        Result r = RulesManager.updateAll(epService, text);
        response.setStatus(r.getStatusCode());
        out.println(r.getMessage());
        out.close();
    }

    /**
     * Handles the HTTP <code>DELETE</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Utils.putCorrelatorAndTrans(request);
        PrintWriter out = response.getWriter();
        response.setContentType("application/json;charset=UTF-8");
        logger.info("delete rule " + request.getPathInfo());
        String ruleName = request.getPathInfo();
        Result r = RulesManager.delete(epService, ruleName.substring(1));
        response.setStatus(r.getStatusCode());
        out.println(r.getMessage());
        out.close();
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String cntaining servlet description
     */
    @Override
    public String getServletInfo() {
        return "Rules servlet";
    }
}
