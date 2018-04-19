/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ducbm.serverchat;

import com.ducbm.commonutils.AppConfiguration;
import com.ducbm.commonutils.Constants;
import com.ducbm.serverchat.servlet.AjaxServlet;
import com.ducbm.serverchat.servlet.UploadServlet;
import javax.servlet.MultipartConfigElement;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 *
 * @author ducbm
 */
public class Application {
    
    public static void main(String[] args) throws Exception {
        
        int serverChatPort = AppConfiguration.getConfigInstance().getInt(Constants.CONFIG_ATTR_SERVER_UPLOAD_PORT);
        String serverUploadEndpoint = AppConfiguration.getConfigInstance().getString(Constants.CONFIG_ATTR_SERVER_UPLOAD_ENDPOINT);
        String serverUploadAppEndpoint = AppConfiguration.getConfigInstance().getString(Constants.CONFIG_ATTR_SERVER_UPLOAD_APP_ENDPOINT);
        
        Server server = new Server(serverChatPort);
        ServletContextHandler handler = new ServletContextHandler(server, serverUploadEndpoint);

        ServletHolder fileUploadServletHolder = new ServletHolder(new UploadServlet());
        fileUploadServletHolder.getRegistration().setMultipartConfig(new MultipartConfigElement(""));
        handler.addServlet(fileUploadServletHolder, serverUploadAppEndpoint);
        handler.addServlet(AjaxServlet.class, serverUploadAppEndpoint + "/ajax");

        server.setHandler(handler);
        server.start();
        server.join();
    }
}
