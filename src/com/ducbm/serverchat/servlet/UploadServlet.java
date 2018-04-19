/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ducbm.serverchat.servlet;

import com.ducbm.commonutils.AppConfiguration;
import com.ducbm.commonutils.Constants;
import com.ducbm.serverchat.utils.ScannerHandler;
import com.ducbm.serverchat.utils.ScannerResult;
import hapax.Template;
import hapax.TemplateDictionary;
import hapax.TemplateException;
import hapax.TemplateLoader;
import hapax.TemplateResourceLoader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

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
        String uuid = request.getParameter("uuid");
        if (uuid == null) {
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
        } else {
            try {
                TemplateLoader templateLoader = TemplateResourceLoader.create("views/");
                Template template = templateLoader.getTemplate("scanning_page.xtm");
                TemplateDictionary templeDictionary = new TemplateDictionary();
                
                ScannerResult result = ScannerHandler.getInstance().getScanResult(uuid, false);
                templeDictionary.setVariable("sha256", result.getSha256());
                templeDictionary.setVariable("file", result.getFile());
                
                String responseTxt = template.renderToString(templeDictionary);
                response.setStatus(HttpStatus.OK_200);
                response.getWriter().println(responseTxt);
            } catch (TemplateException ex) {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
                LOGGER.error(ex);
            }
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
            
            String uuid = ScannerHandler.getInstance().startScanFileForVirus(uploadDir + fileName);
            response.sendRedirect(request.getServletPath() + "?uuid=" + uuid);
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
            LOGGER.error(e);
        }
    }
    
    private void saveFile(String fileLocation, InputStream fileContent)
            throws Exception {
        File targetFile = new File(fileLocation);
        FileUtils.copyInputStreamToFile(fileContent, targetFile);
        System.gc();
    }
}
