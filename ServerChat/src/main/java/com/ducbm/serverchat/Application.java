/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ducbm.serverchat;

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
    
    public static final int SERVER_CHAT_PORT = 7100;
    public static final String SERVER_CHAT_ENDPOINT = "/";
    public static final String APP_CHAT_ENDPOINT = "/upload";
    
    public static void main(String[] args) throws Exception {
        Server server = new Server(SERVER_CHAT_PORT);
        ServletContextHandler handler = new ServletContextHandler(server, SERVER_CHAT_ENDPOINT);

        ServletHolder fileUploadServletHolder = new ServletHolder(new UploadServlet());
        fileUploadServletHolder.getRegistration().setMultipartConfig(new MultipartConfigElement(""));
        handler.addServlet(fileUploadServletHolder, APP_CHAT_ENDPOINT);

        server.setHandler(handler);
        server.start();
        server.join();
    }
}
