/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ducbm.serverchat.servlet;

import com.ducbm.commonutils.AppConfiguration;
import com.ducbm.commonutils.Constants;
import com.ducbm.servercheckvirus.remote.RPCClient;
import com.ducbm.servercheckvirus.remote.RPCClientImpl;
import hapax.Template;
import hapax.TemplateDictionary;
import hapax.TemplateException;
import hapax.TemplateLoader;
import hapax.TemplateResourceLoader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.eclipse.jetty.http.HttpStatus;

/**
 *
 * @author ducbm
 */
public class UploadServlet extends HttpServlet {
    
    private static final Logger LOGGER =
            LogManager.getLogger(UploadServlet.class.getCanonicalName());
    
    private final String uploadDir;
    
    public UploadServlet() {
        uploadDir = AppConfiguration.getConfigInstance()
                .getString(Constants.CONFIG_ATTR_SERVER_UPLOAD_DIR);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LOGGER.info("GET Request: " + request.toString());
        try {
            TemplateLoader templateLoader = TemplateResourceLoader.create("views/");
            Template template = templateLoader.getTemplate("index.xtm");
            TemplateDictionary templeDictionary = new TemplateDictionary();
            String responseTxt = template.renderToString(templeDictionary);
            
            response.setStatus(HttpStatus.OK_200);
            response.getWriter().println(responseTxt);
        } catch (TemplateException e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
            LOGGER.error(e);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LOGGER.info("POST Request: " + request.toString());
        try {
            Part filePart = request.getPart("file");
            String fileName = Paths.get(filePart.getSubmittedFileName())
                    .getFileName().toString();
            InputStream fileContent = filePart.getInputStream();
            saveFile(uploadDir + fileName, fileContent);
            String scanRes = scanFileForVirus(uploadDir + fileName);
            
            response.setStatus(HttpStatus.OK_200);
            response.getWriter().println(scanRes);
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
            LOGGER.error(e);
        }
    }
    
    private void saveFile(String fileLocation, InputStream fileContent)
            throws Exception {
        File targetFile = new File(fileLocation);
        FileUtils.copyInputStreamToFile(fileContent, targetFile);
    }
    
    private String scanFileForVirus(String fileLocation) {
        try {
            RPCClient client = new RPCClientImpl();
            String scanResult = client.scanFileForVirus(fileLocation);
            return scanResult;
        } catch (IOException | TimeoutException e) {
            LOGGER.error(e);
            // show error flag
            return "{status: 0}";
        }
    }
}
