/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ducbm.serverchat.servlet;

import com.ducbm.serverchat.utils.ScannerHandler;
import com.ducbm.serverchat.utils.ScannerResult;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;

/**
 *
 * @author ducbm
 */
public class AjaxServlet extends HttpServlet {
    
    private static final Logger LOGGER =
            LogManager.getLogger(AjaxServlet.class.getCanonicalName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LOGGER.info("AJAX POST Request " + request);
        String uuid = request.getParameter("uuid");
        if (uuid != null) {
            ScannerResult result = ScannerHandler.getInstance().getScanResult(uuid, true);
            response.setStatus(HttpStatus.OK_200);
            response.getWriter().println(result.getResult());
        } else {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
    }
    
}
